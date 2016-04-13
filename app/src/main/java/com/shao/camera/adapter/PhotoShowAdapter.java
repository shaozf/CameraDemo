package com.shao.camera.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.bm.library.PhotoView;

import java.io.File;
import java.util.List;

/**
 * Created by Swain1 on 16/4/13.
 * changeDate at 16/4/13
 */
public class PhotoShowAdapter extends PagerAdapter {
    private List<String> mList;
    private Context context;
    private String path;

    public PhotoShowAdapter(Context context, List<String> mList, String path) {
        this.context = context;
        this.mList = mList;
        this.path = path;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        PhotoView view = new PhotoView(context);
        view.enable();
        view.setImageBitmap(getDiskBitmap(path + "/" + mList.get(position)));
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    private Bitmap getDiskBitmap(String pathString) {
        Bitmap bitmap = null;
        try {
            File file = new File(pathString);
            if (file.exists()) {
                bitmap = BitmapFactory.decodeFile(pathString);
            }
        } catch (Exception e) {
            Log.e("ERROR", "读取本地图片失败");
        }

        return bitmap;
    }
}
