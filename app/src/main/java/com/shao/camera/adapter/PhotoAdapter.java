package com.shao.camera.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.ImageView;

import com.shao.camera.R;
import com.shao.camera.view.listview.RecycleViewHolder;
import com.shao.camera.view.listview.RecyclerUniversalAdapter;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Swain1 on 16/4/12.
 * changeDate at 16/4/12
 */
public class PhotoAdapter extends RecyclerUniversalAdapter<String> {
    /**
     * 用户选择的图片，存储为图片的完整路径
     */
    private List<String> mSelectedImage = new LinkedList<String>();
    /**
     * 文件夹路径
     */
    private String mDirPath;
    private OnPhotoClickLinstener onClick;

    public PhotoAdapter(Context context, List<String> mDatas, int itemLayoutId,
                        String dirPath, List<String> mSelectedImage) {
        super(context, mDatas, itemLayoutId);
        this.mDirPath = dirPath;
        this.mSelectedImage = mSelectedImage;
    }

    /**
     * 选中图片
     */
    public List<String> setChoose(List<String> mSelectedImage, String item, int positation, int Max, View view) {
        this.mSelectedImage = mSelectedImage;
        if (mSelectedImage.contains(mDirPath + "/" + item))
            mSelectedImage.remove(mDirPath + "/" + item);
        else {//判断有没有达到上限
            if (mSelectedImage.size() < Max)
                mSelectedImage.add(mDirPath + "/" + item);
            else
                Snackbar.make(view, "已达到选择上限(" + Max + "张)", Snackbar.LENGTH_SHORT).show();
        }
        notifyItemChanged(positation);
        return mSelectedImage;
    }


    @Override
    public void setDate(RecycleViewHolder holder, final String item, final int position) {
        //设置no_pic
        holder.setImageResource(R.id.id_item_image, R.drawable.pictures_no);
        //设置no_selected
        holder.setImageResource(R.id.id_item_select,
                R.drawable.picture_unselected);
        //设置图片
        holder.setImageByUrl(R.id.id_item_image, mDirPath + "/" + item);

        final ImageView mImageView = holder.getView(R.id.id_item_image);
        final ImageView mSelect = holder.getView(R.id.id_item_select);

        mImageView.setColorFilter(null);
        //设置ImageView的点击事件
        mImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (null != onClick)
                    onClick.setOnPhotoChooseClick(item, position);
            }
        });
        mSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != onClick)
                    onClick.setOnChooseClick(item, position);
            }
        });

        /**
         * 已经选择过的图片，显示出选择过的效果
         */
        if (mSelectedImage.contains(mDirPath + "/" + item)) {
            mSelect.setImageResource(R.drawable.picture_selected);
            mImageView.setColorFilter(Color.parseColor("#77000000"));
        } else {
            mSelect.setImageResource(R.drawable.picture_unselected);
            mImageView.setColorFilter(Color.parseColor("#00000000"));
        }

    }

    public void setOnPhotoClickLinstener(OnPhotoClickLinstener onClick) {
        this.onClick = onClick;
    }

    public interface OnPhotoClickLinstener {

        void setOnChooseClick(String item, int positation);

        void setOnPhotoChooseClick(String item, int positation);
    }

}
