package com.shao.camera.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.shao.camera.R;
import com.shao.camera.activity.PhotoAndVideoActivity;

import java.io.File;


/**
 * Created by Swain1 on 16/4/11.
 * changeDate at 16/4/11
 */
public class CameraDialog extends Dialog implements View.OnClickListener {
    private Activity context;
    private Button btPhoto, btVideo, btCarema, btPackup;
    private String capturePath = null;

    public CameraDialog(Context context) {
        this(context, R.style.CustomDialog);
        this.context = (Activity) context;

    }

    public CameraDialog(Context context, int themeResId) {
        super(context, themeResId);
        this.context = (Activity) context;
    }

    protected CameraDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = (Activity) context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_layout_camera);
        btPhoto = (Button) findViewById(R.id.bt_photo);
        btVideo = (Button) findViewById(R.id.bt_video);
        btCarema = (Button) findViewById(R.id.bt_carema);
        btPackup = (Button) findViewById(R.id.bt_packup);
        btPhoto.setOnClickListener(this);
        btVideo.setOnClickListener(this);
        btCarema.setOnClickListener(this);
        btPackup.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btPhoto) {
            Intent intent = new Intent(context, PhotoAndVideoActivity.class);
            intent.putExtra("type", "0");
            context.startActivityForResult(intent, 1001);
        } else if (v == btVideo) {
            Intent intent = new Intent(context, PhotoAndVideoActivity.class);
            intent.putExtra("type", "1");
            context.startActivityForResult(intent, 1002);
        } else if (v == btCarema)
            getImageFromCamera(1003);
        else if (v == btPackup)
            getVideoForCaremas(1004);
    }

    /**
     * 开启相机
     *
     * @param actionCode 请求码
     */
    protected void getImageFromCamera(int actionCode) {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            Intent getImageByCamera = new Intent("android.media.action.IMAGE_CAPTURE");
            //获取保存的路径
            String out_file_path = getSDPath();
            File dir = new File(out_file_path);
            //给成员变量赋值
            capturePath = getSDPath() + "/" + System.currentTimeMillis() + ".jpg";
            if (!dir.exists()) {
                dir.mkdirs();
            }

            getImageByCamera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(capturePath)));
            getImageByCamera.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            context.startActivityForResult(getImageByCamera, actionCode);
        } else {
            Toast.makeText(context, "请确认已经插入SD卡", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 获取存储路径，可以写在FileUtils中
     */
    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }

    public void getVideoForCaremas(int actionCode) {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            Intent getVideoByCamera = new Intent("android.media.action.VIDEO_CAPTURE");
            //获取保存的路径
            String out_file_path = getSDPath();
            File dir = new File(out_file_path);
            //给成员变量赋值
            capturePath = getSDPath() + "/" + System.currentTimeMillis() + ".mp4";
            if (!dir.exists()) {
                dir.mkdirs();
            }

            getVideoByCamera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(capturePath)));
            getVideoByCamera.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            getVideoByCamera.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 20 * 1000);
            context.startActivityForResult(getVideoByCamera, actionCode);
        } else {
            Toast.makeText(context, "请确认已经插入SD卡", Toast.LENGTH_LONG).show();
        }

    }

}
