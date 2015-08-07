package com.photoselector.ui;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;

import com.photoselector.R;
import com.photoselector.model.PhotoModel;
import com.photoselector.ui.PhotoItem.onItemClickListener;
import com.photoselector.ui.PhotoItem.onPhotoItemCheckedListener;

/**
 * 
 * @author Aizaz AZ
 * 
 */

public class PhotoSelectorAdapter extends MBaseAdapter<PhotoModel> {

    private int itemWidth;
    private int horizentalNum = 3;
    private onPhotoItemCheckedListener listener;
    private LayoutParams itemLayoutParams;
    private onItemClickListener mCallback;
    private OnClickListener cameraListener;
    private final int TAKE_CAMERA = 0;
    private final int PHOTO = 1;

    private PhotoSelectorAdapter(Context context, ArrayList<PhotoModel> models) {
        super(context, models);
    }

    public PhotoSelectorAdapter(Context context, ArrayList<PhotoModel> models,
            int screenWidth, onPhotoItemCheckedListener listener,
            onItemClickListener mCallback, OnClickListener cameraListener) {
        this(context, models);
        setItemWidth(screenWidth);
        this.listener = listener;
        this.mCallback = mCallback;
        this.cameraListener = cameraListener;
    }

    /** 设置每一个Item的宽高 */
    public void setItemWidth(int screenWidth) {
        int horizentalSpace = context.getResources().getDimensionPixelSize(
                R.dimen.sticky_item_horizontalSpacing);
        this.itemWidth = (screenWidth - (horizentalSpace * (horizentalNum - 1)))
                / horizentalNum;
        this.itemLayoutParams = new LayoutParams(itemWidth, itemWidth);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TAKE_CAMERA;
        } else {
            return PHOTO;
        }
    }

    @Override
    public int getCount() {
        return super.getCount() > 0 ? super.getCount() + 1 : 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PhotoItem item = null;
        if (convertView == null || !(convertView instanceof PhotoItem)) {
            item = new PhotoItem(context, listener);
            item.setLayoutParams(itemLayoutParams);
            convertView = item;
        } else {
            item = (PhotoItem) convertView;
        }
        switch (getItemViewType(position)) {
        case TAKE_CAMERA:
            item.setCheckBoxVisibility(false);
            item.setCameraItem();
            item.setOnClickListener(cameraListener);
        break;
        case PHOTO:
            item.setCheckBoxVisibility(true);
            item.setImageDrawable(models.get(position - 1));
            item.setSelected(models.get(position - 1).isChecked());
            item.setOnClickListener(null);
            item.setOnClickListener(mCallback, position - 1);
        break;
        default:
        break;
        }
        return convertView;
    }
}
