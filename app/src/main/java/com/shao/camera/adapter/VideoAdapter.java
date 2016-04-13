package com.shao.camera.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
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
public class VideoAdapter extends RecyclerUniversalAdapter<String> {
    /**
     * 用户选择的图片，存储为图片的完整路径
     */
    private List<String> mSelectedImage = new LinkedList<String>();
    /**
     * 文件夹路径
     */
    private String mDirPath;
    private OnVideoClickLinstener onClick;

    public VideoAdapter(Context context, List<String> mDatas, int itemLayoutId,
                        String dirPath, List<String> mSelectedImage) {
        super(context, mDatas, itemLayoutId);
        this.mDirPath = dirPath;
        this.mSelectedImage = mSelectedImage;
    }

    /**
     * 选中图片
     */
    public List<String> setChoose(List<String> mSelectedImage, String path, int positation, int Max, View view) {
        this.mSelectedImage = mSelectedImage;
        if (mSelectedImage.contains(path))
            mSelectedImage.remove(path);
        else {//判断有没有达到上限
            if (mSelectedImage.size() < Max)
                mSelectedImage.add(path);
            else
                Snackbar.make(view, "已达到选择上限(" + Max + "段)", Snackbar.LENGTH_SHORT).show();
        }
        notifyItemChanged(positation);
        return mSelectedImage;
    }

    @Override
    public void setDate(RecycleViewHolder holder, final String imageInfo, final int position) {
        //设置no_pic
        holder.setImageResource(R.id.id_item_image, R.drawable.pictures_no);
        //设置no_selected
        holder.setImageResource(R.id.id_item_select, R.drawable.picture_unselected);
        //设置图片
//        holder.setImageByUrl(R.id.id_item_image, imageInfo.getMimeType());

        ImageView mImageView = holder.getView(R.id.id_item_image);
        ImageView mSelect = holder.getView(R.id.id_item_select);
        mImageView.setImageBitmap(getVideoThumbnail(context.getContentResolver(), mDirPath + "/" + imageInfo));
        mImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (null != onClick)
                    onClick.setOnVideoChooseClick(mDirPath + "/" + imageInfo, position);
            }
        });
        mSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != onClick)
                    onClick.setOnChooseClick(mDirPath + "/" + imageInfo, position);
            }
        });

        /**
         * 已经选择过的图片，显示出选择过的效果
         */
        if (mSelectedImage.contains(imageInfo)) {
            mSelect.setImageResource(R.drawable.picture_selected);
            mImageView.setColorFilter(Color.parseColor("#77000000"));
        } else {
            mSelect.setImageResource(R.drawable.picture_unselected);
            mImageView.setColorFilter(Color.parseColor("#00000000"));
        }
    }

    public void setOnVideoClickLinstener(OnVideoClickLinstener onClick) {
        this.onClick = onClick;
    }

    public interface OnVideoClickLinstener {

        void setOnChooseClick(String path, int positation);

        void setOnVideoChooseClick(String path, int positation);
    }

    public Bitmap getVideoThumbnail(ContentResolver cr, String path) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        String whereClause = MediaStore.Video.Media.DATA + " = '"
                + path + "'";
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Cursor cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Video.Media._ID}, whereClause, null, null);

        if (cursor == null || cursor.getCount() == 0) {
            return ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
        }
        cursor.moveToFirst();
        String videoId = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));  //image id in image table.s

        if (videoId == null) {
            return null;
        }
        cursor.close();
        long videoIdLong = Long.parseLong(videoId);
        bitmap = MediaStore.Video.Thumbnails.getThumbnail(cr, videoIdLong, MediaStore.Images.Thumbnails.MINI_KIND, options);

        return bitmap;
    }

}
