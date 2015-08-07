package com.photoselector.ui;

/**
 * 
 * @author Aizaz AZ
 *
 */
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.photoselector.R;
import com.photoselector.model.PhotoModel;
import com.polites.GestureImageView;

public class PhotoPreview extends LinearLayout implements OnClickListener {

    private ProgressBar pbLoading;
    private GestureImageView ivContent;
    private OnClickListener l;

    public PhotoPreview(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.view_photopreview, this,
                true);

        pbLoading = (ProgressBar) findViewById(R.id.pb_loading_vpp);
        ivContent = (GestureImageView) findViewById(R.id.iv_content_vpp);
        ivContent.setOnClickListener(this);
    }

    public PhotoPreview(Context context, AttributeSet attrs, int defStyle) {
        this(context);
    }

    public PhotoPreview(Context context, AttributeSet attrs) {
        this(context);
    }

    public void loadImage(PhotoModel photoModel) {
        String url = photoModel.getOriginalPath();
        // if (!url.startsWith("http")) {
        // url = "file://" + url;
        // }
        Glide.with(getContext()).load(url).crossFade().override(800, 800)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception arg0, String arg1,
                            Target<GlideDrawable> arg2, boolean arg3) {
                        pbLoading.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable arg0,
                            String arg1, Target<GlideDrawable> arg2,
                            boolean arg3, boolean arg4) {
                        pbLoading.setVisibility(View.GONE);
                        return false;
                    }
                }).into(ivContent);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        this.l = l;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_content_vpp && l != null)
            l.onClick(ivContent);
    };

}
