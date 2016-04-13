package com.shao.camera.imageloader;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.shao.camera.R;
import com.shao.camera.model.ImageInfo;
import com.shao.camera.view.BasePopupWindowForListView;
import com.shao.camera.adapter.CommonAdapter;
import com.shao.camera.view.listview.ViewHolder;

import java.util.List;


public class ListVideoDirPopupWindow extends BasePopupWindowForListView<ImageInfo> {
    private ListView mListDir;

    public ListVideoDirPopupWindow(int width, int height, List<ImageInfo> datas, View convertView) {
        super(convertView, width, height, true, datas);
    }

    @Override
    public void initViews() {
        mListDir = (ListView) findViewById(R.id.id_list_dir);
        mListDir.setAdapter(new CommonAdapter<ImageInfo>(context, mDatas,
                R.layout.list_dir_item) {
            @Override
            public void convert(ViewHolder helper, ImageInfo item) {
                helper.setText(R.id.id_dir_item_name, item.getName());
                helper.setImageByUrl(R.id.id_dir_item_image, item.getMimeType());
                helper.setText(R.id.id_dir_item_count, item.getCount() + "个");
            }
        });
    }

    public interface OnVideoDirSelected {
        void selectedVideo(ImageInfo floder);
    }

    private OnVideoDirSelected mVideoDirSelected;

    public void setVideoDirSelected(OnVideoDirSelected mImageDirSelected) {
        this.mVideoDirSelected = mImageDirSelected;
    }

    @Override
    public void initEvents() {
        mListDir.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (null != mVideoDirSelected) {
                    mVideoDirSelected.selectedVideo(mDatas.get(position));
                }
            }
        });
    }

    @Override
    public void init() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void beforeInitWeNeedSomeParams(Object... params) {
        // TODO Auto-generated method stub
    }

}
