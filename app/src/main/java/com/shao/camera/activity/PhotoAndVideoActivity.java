package com.shao.camera.activity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.shao.camera.R;
import com.shao.camera.adapter.PhotoAdapter;
import com.shao.camera.adapter.VideoAdapter;
import com.shao.camera.imageloader.ListImageDirPopupWindow;
import com.shao.camera.imageloader.ListVideoDirPopupWindow;
import com.shao.camera.model.ImageInfo;
import com.shao.camera.utils.ListSheft;
import com.shao.camera.view.listview.MyRecycleView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class PhotoAndVideoActivity extends AppCompatActivity implements View.OnClickListener, ListImageDirPopupWindow.OnImageDirSelected, ListVideoDirPopupWindow.OnVideoDirSelected {
    private ImageView ivBack;
    private TextView tvTitle, tvCount, tvList;
    private Toolbar toobar;
    private Button btSure;
    private MyRecycleView mvView;
    private boolean type = false;
    private ProgressDialog mProgressDialog;
    private int MAX_CHOOSE = 9;
    private String tvTitleName;
    private String tvCountNum;
    private String[] photoStr = new String[]{"image/jpeg", "image/png"};

    String[] thumbColumns = new String[]{
            MediaStore.Video.Thumbnails.DATA,
            MediaStore.Video.Thumbnails.VIDEO_ID
    };

    //存储文件夹中的图片数量
    private int mPicsSize, mVideoSize;
    //图片数量最多的文件夹
    private File mImgDir, mVideoDir;
    //所有的图片
    private List<String> mImgs, mVideos;

    private PhotoAdapter mAdapter;
    private VideoAdapter vAdapter;
    //临时的辅助类，用于防止同一个文件夹的多次扫描
    private HashSet<String> mDirPaths = new HashSet<String>();

    /**
     * 扫描拿到所有的图片文件夹
     */
    private List<ImageInfo> mImageInfos = new ArrayList<>();
    /**
     * 扫描拿到所有的图片文件夹
     */
    private List<ImageInfo> mVedioInfos = new ArrayList<>();
    /**
     * 用户选择的图片/视频，存储为图片的完整路径
     */
    private List<String> mSelectedImage = new LinkedList<>();


    int totalCount = 0;

    private int mScreenHeight;

    private ListImageDirPopupWindow mListImageDirPopupWindow;
    private ListVideoDirPopupWindow mListVideoDirPopupWindow;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            mProgressDialog.dismiss();
            if (msg.what == 0x110) {
                data2View();
                initListDirPopupWindw();
            } else {
                video2View();
                initVideoListDirPopupWindw();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        initView();
        getAppPermission();
        if (!getAppPermission()) {
            finish();
            return;
        }
        addDate();
    }


    /**
     * 判断用户权限
     */
    private boolean getAppPermission() {
        PackageManager pkm = getPackageManager();
        if (pkm.checkPermission("android.permission.WRITE_EXTERNAL_STORAGE", "com.shao.camera") != PackageManager.PERMISSION_GRANTED || pkm.checkPermission("android.permission.READ_EXTERNAL_STORAGE", "com.shao.camera") != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "请允许应用读取存储空间权限", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * 添加数据
     */
    private void addDate() {
        if (type)
            addPhoto();
        else
            addVideo();
    }

    /**
     * 获取视频
     */
    private void addVideo() {
        getVideo();
        initEvent();
    }

    /**
     * 获取照片
     */
    private void addPhoto() {
        getImages();
        initEvent();
    }

    /**
     * 初始化
     */
    private void initView() {
        Intent intent = getIntent();
        toobar = (Toolbar) findViewById(R.id.toolbar);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvList = (TextView) findViewById(R.id.tv_list);
        tvCount = (TextView) findViewById(R.id.tv_count);
        mvView = (MyRecycleView) findViewById(R.id.mv_view);
        btSure = (Button) findViewById(R.id.bt_sure);
        btSure.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        tvTitle.setOnClickListener(this);
        setSupportActionBar(toobar);
        String type = intent.getStringExtra("type");
        if (type.equals("0")) {
            tvTitle.setText("照片 ");
            this.type = true;
        } else {
            tvTitle.setText("视频");
            this.type = false;
        }
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        mScreenHeight = outMetrics.heightPixels;

    }

    @Override
    public void onClick(View v) {
        if (v == ivBack || v == tvTitle) {
            this.finish();
        } else if (v == btSure) {
            Log.d("", "");
        }
    }

    /**
     * 利用ContentProvider扫描手机中的图片，此方法在运行在子线程中 完成图片的扫描，最终获得jpg最多的那个文件夹
     */
    private void getImages() {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "暂无外部存储", Toast.LENGTH_SHORT).show();
            return;
        }
        // 显示进度条
        mProgressDialog = ProgressDialog.show(this, null, "正在搜索...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                String firstImage = null;
                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = PhotoAndVideoActivity.this
                        .getContentResolver();
                Cursor mCursor = mContentResolver.query(mImageUri, null,
                        MediaStore.Images.Media.MIME_TYPE + "=? or "
                                + MediaStore.Images.Media.MIME_TYPE + "=?", photoStr,
                        MediaStore.Images.Media.DATE_MODIFIED);
                assert mCursor != null;
                while (mCursor.moveToNext()) {
                    String path = mCursor.getString(mCursor
                            .getColumnIndex(MediaStore.Images.Media.DATA));
                    // 拿到第一张图片的路径
                    if (firstImage == null)
                        firstImage = path;
                    // 获取该图片的父路径名
                    File parentFile = new File(path).getParentFile();
                    if (parentFile == null)
                        continue;
                    if (parentFile.list() == null)
                        continue;
                    String dirPath = parentFile.getAbsolutePath();
                    ImageInfo imageInfo;
                    // 利用一个HashSet防止多次扫描同一个文件夹（不加这个判断，图片多起来还是相当恐怖的~~）
                    if (mDirPaths.contains(dirPath)) {
                        continue;
                    } else {
                        mDirPaths.add(dirPath);
                        // 初始化ImageInfo
                        imageInfo = new ImageInfo();
                        imageInfo.setDir(dirPath);
                        imageInfo.setFirstImagePath(path);
                    }

                    int picSize = parentFile.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String filename) {
                            return filename.endsWith(".jpg") || filename.endsWith(".png") || filename.endsWith(".jpeg");
                        }
                    }).length;
                    totalCount = picSize;
                    imageInfo.setCount(picSize);
                    mImageInfos.add(imageInfo);
                    if (picSize > mPicsSize) {
                        mPicsSize = picSize;
                        mImgDir = parentFile;
                        tvCountNum = mVideoSize + "";
                    }
                }
                mCursor.close();
                // 扫描完成，辅助的HashSet也就可以释放内存了
                mDirPaths = null;
                // 通知Handler扫描图片完成
                mHandler.sendEmptyMessage(0x110);
            }
        }).start();

    }

    /**
     * 利用ContentProvider扫描手机中的视频，此方法在运行在子线程中
     */
    private void getVideo() {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "暂无外部存储", Toast.LENGTH_SHORT).show();
            return;
        }
        // 显示进度条
        mProgressDialog = ProgressDialog.show(this, null, "正在搜索...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver mContentResolver = PhotoAndVideoActivity.this
                        .getContentResolver();
                Cursor mCursor;
                mCursor = mContentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        null, null, null, null);
                assert mCursor != null;
                while (mCursor.moveToNext()) {
                    String path = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    File parentFile = new File(path).getParentFile();
                    if (parentFile == null)
                        continue;
                    if (parentFile.list() == null)
                        continue;
                    String dirPath = parentFile.getAbsolutePath();
                    ImageInfo info;
                    if (mDirPaths.contains(dirPath)) {
                        continue;
                    } else {
                        mDirPaths.add(dirPath);
                        info = getVideoInfo(mCursor);
                        info.setDir(dirPath);
                        getBitmap(mCursor, info, mContentResolver);
                    }

                    int videoSize = parentFile.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String filename) {
                            return (filename.endsWith(".3gp") || filename.endsWith(".mp4"));
                        }
                    }).length;
                    info.setCount(videoSize);
                    mVedioInfos.add(info);
                    if (videoSize > mVideoSize) {
                        mVideoSize = videoSize;
                        mVideoDir = parentFile;
                        tvCountNum = mVideoSize + "";
                    }
                }
                mCursor.close();
                // 扫描完成，辅助的HashSet也就可以释放内存了
                mDirPaths = null;
                // 通知Handler扫描图片完成
                mHandler.sendEmptyMessage(0x120);
            }
        }).start();
    }

    /**
     * 获取视频信息
     */
    private ImageInfo getVideoInfo(Cursor mCursor) {
        ImageInfo info = new ImageInfo();
        info.setFilePath(mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));
        info.setMimeType(mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)));
        info.setTitle(mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)));
        return info;
    }

    /**
     * 获取视频第一帧图片用来显示缩略图
     */
    private void getBitmap(Cursor mCursor, ImageInfo info, ContentResolver mContentResolver) {
        int id = mCursor.getInt(mCursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
        String selection = MediaStore.Video.Thumbnails.VIDEO_ID + "=?";
        String[] selectionArgs = new String[]{id + ""};
        Cursor thumbCursor = mContentResolver.query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, thumbColumns, selection, selectionArgs, null);
        assert thumbCursor != null;
        if (thumbCursor.moveToFirst()) {
            info.setMimeType(thumbCursor.getString(thumbCursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA)));
        }
    }

    /**
     * 设置视频信息
     */
    private void video2View() {
        if (mVideoDir == null) {
            Toast.makeText(getApplicationContext(), "搜索不到手机中的视频...",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mVideos = Arrays.asList(mVideoDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return (filename.endsWith(".3gp") || filename.endsWith(".mp4"));
            }
        }));
        createVideoAdapter();
        setBottomTextView(tvCountNum + "个", mVideoDir.getName());
    }

    /**
     * 初始化adapter
     */
    private void createVideoAdapter() {
        vAdapter = new VideoAdapter(getApplicationContext(), mVideos, R.layout.grid_item, mVideoDir.getAbsolutePath(), mSelectedImage);
        mvView.setAdapter(vAdapter);
        mvView.setAdapterGridViewVertical(3);
        mvView.canNotLoad();
        vAdapter.setOnVideoClickLinstener(onVideoChoose);
    }

    /**
     * 为View绑定数据
     */
    private void data2View() {
        if (mImgDir == null) {
            Toast.makeText(getApplicationContext(), "搜索不到手机中的照片...",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mImgs = Arrays.asList(mImgDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return (filename.endsWith(".jpg") || filename.endsWith(".png") || filename.endsWith(".jpeg"));
            }
        }));
        /**
         * 可以看到文件夹的路径和图片的路径分开保存，极大的减少了内存的消耗；
         */
        createAdapter();
        setBottomTextView(tvCountNum + "张", mImgDir.getName());
    }

    /**
     * 设置底部显示内容
     */
    private void setBottomTextView(String text, String name) {
        tvCountNum = text;
        tvTitleName = name;
        tvCount.setText(tvCountNum);
        tvList.setText(tvTitleName);
        btSure.setText("选择 (" + mSelectedImage.size() + "/" + MAX_CHOOSE + ")");
    }


    /**
     * 初始化展示文件夹的popupWindw
     */
    private void initListDirPopupWindw() {
        mListImageDirPopupWindow = new ListImageDirPopupWindow(ViewGroup.LayoutParams.MATCH_PARENT, (int) (mScreenHeight * 0.7),
                mImageInfos, LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.list_dir, null));
        mListImageDirPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setWindowBackground(1.0f);
            }
        });
        // 设置选择文件夹的回调
        mListImageDirPopupWindow.setOnImageDirSelected(PhotoAndVideoActivity.this);
    }

    /**
     * 初始化展示文件夹的popupWindw
     */
    private void initVideoListDirPopupWindw() {
        mListVideoDirPopupWindow = new ListVideoDirPopupWindow(ViewGroup.LayoutParams.MATCH_PARENT, (int) (mScreenHeight * 0.7),
                mVedioInfos, LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.list_dir, null));
        mListVideoDirPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setWindowBackground(1.0f);
            }
        });
        // 设置选择文件夹的回调
        mListVideoDirPopupWindow.setVideoDirSelected(PhotoAndVideoActivity.this);
    }

    /**
     * 设置选择的图片
     */
    @Override
    public void selected(ImageInfo floder) {
        mImgDir = new File(floder.getDir());
        mImgs = Arrays.asList(mImgDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return (filename.endsWith(".jpg") || filename.endsWith(".png") || filename.endsWith(".jpeg"));

            }
        }));
        createAdapter();
        setBottomTextView(floder.getCount() + "张", floder.getName().replace("/", ""));
        mListImageDirPopupWindow.dismiss();

    }

    /**
     * 设置视频选择
     */
    @Override
    public void selectedVideo(ImageInfo floder) {
        mVideoDir = new File(floder.getFilePath());
        mVideos = Arrays.asList(mVideoDir.getParentFile().list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return (filename.endsWith(".3gp") || filename.endsWith(".mp4"));
            }
        }));
        mVideoDir = mVideoDir.getParentFile();//缩放图和视频不是在一个文件夹下 所以需要多返回一层
        createVideoAdapter();
        setBottomTextView(floder.getCount() + "个", floder.getName().replace("/", ""));
        mListVideoDirPopupWindow.dismiss();
    }

    /**
     * 创建adapter
     */
    private void createAdapter() {
        mAdapter = new PhotoAdapter(getApplicationContext(), mImgs, R.layout.grid_item, mImgDir.getAbsolutePath(), mSelectedImage);
        mvView.setAdapter(mAdapter);
        mvView.setAdapterGridViewVertical(3);
        mvView.canNotLoad();
        mAdapter.setOnPhotoClickLinstener(onChoose);
    }

    /**
     * 为底部的布局设置点击事件，弹出popupWindow
     */
    private void initEvent() {
        tvList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type)
                    createPopupwindow();
                else
                    createVideoPopupwindow();
            }
        });
    }

    /**
     * 创建popupwindow
     */
    private void createPopupwindow() {
        mListImageDirPopupWindow
                .setAnimationStyle(R.style.anim_popup_dir);
        mListImageDirPopupWindow.showAsDropDown(tvList, 0, 0);
        setWindowBackground(0.3f);
    }

    /**
     * 创建popupwindow
     */
    private void createVideoPopupwindow() {
        mListVideoDirPopupWindow.setAnimationStyle(R.style.anim_popup_dir);
        mListVideoDirPopupWindow.showAsDropDown(tvList, 0, 0);
        setWindowBackground(0.3f);
    }

    /**
     * 设置窗口背景颜色
     */
    private void setWindowBackground(float f) {
        // 设置背景颜色变暗
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = f;
        getWindow().setAttributes(lp);
    }

    PhotoAdapter.OnPhotoClickLinstener onChoose = new PhotoAdapter.OnPhotoClickLinstener() {
        @Override
        public void setOnChooseClick(String item, int positation) {
            mSelectedImage = mAdapter.setChoose(mSelectedImage, item, positation, MAX_CHOOSE, tvList);
            setBottomTextView(tvCountNum, tvTitleName);
        }

        @Override
        public void setOnPhotoChooseClick(String item, int positation) {
            startActivity(new Intent(PhotoAndVideoActivity.this, PhotoShowActivity.class).putExtra("positation", positation).putExtra("list", ListSheft.SceneList2String(mImgs)).putExtra("path", mImgDir.getAbsolutePath()));

        }
    };
    VideoAdapter.OnVideoClickLinstener onVideoChoose = new VideoAdapter.OnVideoClickLinstener() {
        @Override
        public void setOnChooseClick(String path, int positation) {
            mSelectedImage = vAdapter.setChoose(mSelectedImage, path, positation, MAX_CHOOSE, tvTitle);
            setBottomTextView(tvCountNum, tvTitleName);
        }

        @Override
        public void setOnVideoChooseClick(String path, int positation) {
            startActivity(new Intent(PhotoAndVideoActivity.this, VideoShowActivity.class).putExtra("url", path));
        }


    };


}
