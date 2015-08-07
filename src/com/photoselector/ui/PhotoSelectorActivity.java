package com.photoselector.ui;

/**
 * 
 * @author Aizaz AZ
 *
 */
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.photoselector.R;
import com.photoselector.domain.PhotoSelectorDomain;
import com.photoselector.model.AlbumModel;
import com.photoselector.model.PhotoModel;
import com.photoselector.ui.PhotoItem.onItemClickListener;
import com.photoselector.ui.PhotoItem.onPhotoItemCheckedListener;
import com.photoselector.util.AnimationUtil;
import com.photoselector.util.CommonUtils;

/**
 * @author Aizaz AZ
 * 
 */
public class PhotoSelectorActivity extends Activity implements
        onItemClickListener, onPhotoItemCheckedListener, OnItemClickListener,
        OnClickListener {

    public static final int SINGLE_IMAGE = 1;
    public static final String KEY_MAX = "key_max";
    private int MAX_IMAGE;

    public static final int REQUEST_PHOTO = 0;
    private static final int REQUEST_CAMERA = 1;

    public static String RECCENT_PHOTO = null;

    private GridView gvPhotos;
    private ListView lvAblum;
    private Button btnOk;
    private TextView tvAlbum, tvPreview, tvTitle;
    private PhotoSelectorDomain photoSelectorDomain;
    private PhotoSelectorAdapter photoAdapter;
    private AlbumAdapter albumAdapter;
    private RelativeLayout layoutAlbum;
    private ArrayList<PhotoModel> selected;
    private TextView tvNumber;
    private boolean checked;
    File mediaFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            RECCENT_PHOTO = getResources().getString(
                    R.string.photo_selector_recent_photos);
            requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
            setContentView(R.layout.activity_photoselector);

            if (getIntent().getExtras() != null) {
                MAX_IMAGE = getIntent().getIntExtra(KEY_MAX, 10);
            }
            photoSelectorDomain = new PhotoSelectorDomain(
                    getApplicationContext());

            tvTitle = (TextView) findViewById(R.id.tv_title_lh);
            gvPhotos = (GridView) findViewById(R.id.gv_photos_ar);
            lvAblum = (ListView) findViewById(R.id.lv_ablum_ar);
            btnOk = (Button) findViewById(R.id.btn_right_lh);
            tvAlbum = (TextView) findViewById(R.id.tv_album_ar);
            tvPreview = (TextView) findViewById(R.id.tv_preview_ar);
            layoutAlbum = (RelativeLayout) findViewById(R.id.layout_album_ar);
            tvNumber = (TextView) findViewById(R.id.tv_number);
            selected = (ArrayList<PhotoModel>) getIntent()
                    .getSerializableExtra("photos");
            if (selected.size() != 0) {
                tvPreview.setEnabled(true);
                tvNumber.setText("(" + selected.size() + "/" + MAX_IMAGE + ")");
                tvPreview.setText(getString(R.string.photo_selector_preview)
                        + "(" + selected.size() + "/" + MAX_IMAGE + ")");
            }
            btnOk.setOnClickListener(this);
            tvAlbum.setOnClickListener(this);
            tvPreview.setOnClickListener(this);

            photoAdapter = new PhotoSelectorAdapter(getApplicationContext(),
                    new ArrayList<PhotoModel>(),
                    CommonUtils.getWidthPixels(this), this, this,
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            catchPicture();
                        }
                    });
            gvPhotos.setAdapter(photoAdapter);

            albumAdapter = new AlbumAdapter(getApplicationContext(),
                    new ArrayList<AlbumModel>());
            lvAblum.setAdapter(albumAdapter);
            lvAblum.setOnItemClickListener(this);

            findViewById(R.id.bv_back_lh).setOnClickListener(this); // 返回

            photoSelectorDomain.getReccent(reccentListener); // 更新最近照片
            photoSelectorDomain.updateAlbum(albumListener); // 跟新相册信息
        } else {
            Toast.makeText(this, "SD卡不可用", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_right_lh)
            ok(); // 选完照片
        else if (v.getId() == R.id.tv_album_ar)
            album();
        else if (v.getId() == R.id.tv_preview_ar)
            priview();
        else if (v.getId() == R.id.bv_back_lh) finish();
    }

    /** 拍照 */
    private void catchPicture() {
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (getIntent().getBooleanExtra("userFront", false)) {
                intent.putExtra("android.intent.extras.CAMERA_FACING", 1); // 调用前置摄像头
            }
            Uri uri = getIntent().getExtras().getParcelable("photoUri");
            if (uri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            } else {
                uri = getUri(this);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            }
            intent.addFlags(0x3000000);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "SD卡不可用", Toast.LENGTH_LONG).show();
        }
    }

    public static Uri getUri(Context context) {

        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "Camera");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");
        return Uri.fromFile(mediaFile);

        // if (Environment.MEDIA_MOUNTED.equals(Environment
        // .getExternalStorageState())) {
        // SimpleDateFormat timeStampFormat = new SimpleDateFormat(
        // "yyyy_MM_dd_HH_mm_ss");
        // String filename = timeStampFormat.format(new Date());
        // ContentValues values = new ContentValues();
        // values.put(MediaColumns.TITLE, filename);
        // return context.getContentResolver().insert(
        // MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        // } else {
        // return null;
        // }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            Uri uri = null;
            if (data != null && data.getData() != null) {
                uri = data.getData();
            }
            PhotoModel photoModel = new PhotoModel(CommonUtils.query(
                    getApplicationContext(), uri));
            // selected.clear();
            // //--keep all
            // selected photos
            // tvNumber.setText("(0)");
            // //--keep all
            // selected photos
            // ///////////////////////////////////////////////////////////////////////////////////////////
            if (selected.size() > MAX_IMAGE) {
                Toast.makeText(
                        this,
                        String.format(
                                getString(R.string.photo_selector_max_img_limit_reached),
                                MAX_IMAGE), Toast.LENGTH_SHORT).show();
                photoModel.setChecked(false);
                photoAdapter.notifyDataSetChanged();
            } else {
                if (!selected.contains(photoModel)) {
                    selected.add(photoModel);
                }
                ok();
            }
        }
    }

    /** 完成 */
    private void ok() {
        if (selected.size() > MAX_IMAGE) {
            Toast.makeText(
                    this,
                    String.format(
                            getString(R.string.photo_selector_max_img_limit_reached),
                            MAX_IMAGE), Toast.LENGTH_SHORT).show();
        } else {
            Intent data = new Intent();
            Bundle bundle = new Bundle();
            bundle.putSerializable("photos", selected);
            data.putExtras(bundle);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    /** 预览照片 */
    private void priview() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("photos", selected);
        CommonUtils.launchActivity(this, PhotoPreviewActivity.class, bundle);
    }

    private void album() {
        if (layoutAlbum.getVisibility() == View.GONE) {
            popAlbum();
        } else {
            hideAlbum();
        }
    }

    /** 弹出相册列表 */
    private void popAlbum() {
        layoutAlbum.setVisibility(View.VISIBLE);
        new AnimationUtil(getApplicationContext(), R.anim.translate_up_current)
                .setLinearInterpolator().startAnimation(layoutAlbum);
    }

    /** 隐藏相册列表 */
    private void hideAlbum() {
        new AnimationUtil(getApplicationContext(), R.anim.translate_down)
                .setLinearInterpolator().startAnimation(layoutAlbum);
        layoutAlbum.setVisibility(View.GONE);
    }

    /** 清空选中的图片 */
    private void reset() {
        selected.clear();
        tvNumber.setText("");
        tvPreview.setEnabled(false);
    }

    @Override
    /** 点击查看照片 */
    public void onItemClick(int position) {
        Bundle bundle = new Bundle();
        if (tvAlbum.getText().toString().equals(RECCENT_PHOTO))
            bundle.putInt("position", position - 1);
        else
            bundle.putInt("position", position);
        bundle.putString("album", tvAlbum.getText().toString());
        CommonUtils.launchActivity(this, PhotoPreviewActivity.class, bundle);
    }

    @Override
    /** 照片选中状态改变之后 */
    public boolean onCheckedChanged(PhotoModel photoModel,
            CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (!selected.contains(photoModel)) selected.add(photoModel);
            if (selected.size() > MAX_IMAGE) {
                selected.remove(photoModel);
                buttonView.toggle();
                checked = false;
                Toast.makeText(
                        this,
                        String.format(
                                getString(R.string.photo_selector_max_img_limit_reached),
                                MAX_IMAGE), Toast.LENGTH_SHORT).show();
            } else {
                checked = true;
            }
            tvPreview.setEnabled(true);
        } else {
            checked = false;
            if (selected.contains(photoModel)) selected.remove(photoModel);
        }
        tvNumber.setText("(" + selected.size() + "/" + MAX_IMAGE + ")");
        tvPreview.setText(getString(R.string.photo_selector_preview) + "("
                + selected.size() + "/" + MAX_IMAGE + ")");

        if (selected.isEmpty()) {
            tvPreview.setEnabled(false);
            tvNumber.setText("");
            tvPreview.setText(getString(R.string.photo_selector_preview));
        }
        return checked;
    }

    @Override
    public void onBackPressed() {
        if (layoutAlbum.getVisibility() == View.VISIBLE) {
            hideAlbum();
        } else
            super.onBackPressed();
    }

    @Override
    /** 相册列表点击事件 */
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        AlbumModel current = (AlbumModel) parent.getItemAtPosition(position);
        for (int i = 0; i < parent.getCount(); i++) {
            AlbumModel album = (AlbumModel) parent.getItemAtPosition(i);
            if (i == position)
                album.setCheck(true);
            else
                album.setCheck(false);
        }
        albumAdapter.notifyDataSetChanged();
        hideAlbum();
        tvAlbum.setText(current.getName());
        // tvTitle.setText(current.getName());

        // 更新照片列表
        if (current.getName().equals(RECCENT_PHOTO))
            photoSelectorDomain.getReccent(reccentListener);
        else
            photoSelectorDomain.getAlbum(current.getName(), reccentListener); // 获取选中相册的照片
    }

    /** 获取本地图库照片回调 */
    public interface OnLocalReccentListener {
        public void onPhotoLoaded(List<PhotoModel> photos);
    }

    /** 获取本地相册信息回调 */
    public interface OnLocalAlbumListener {
        public void onAlbumLoaded(List<AlbumModel> albums);
    }

    private OnLocalAlbumListener albumListener = new OnLocalAlbumListener() {
        @Override
        public void onAlbumLoaded(List<AlbumModel> albums) {
            albumAdapter.update(albums);
        }
    };

    private OnLocalReccentListener reccentListener = new OnLocalReccentListener() {
        @Override
        public void onPhotoLoaded(List<PhotoModel> photos) {
            for (PhotoModel model : photos) {
                if (selected.contains(model)) {
                    model.setChecked(true);
                }
            }
            photoAdapter.update(photos);
            gvPhotos.smoothScrollToPosition(0); // 滚动到顶端
            // reset(); //--keep selected photos

        }
    };
}
