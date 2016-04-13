package com.shao.camera.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.shao.camera.R;
import com.shao.camera.utils.BitmapCompressUtils;
import com.shao.camera.utils.FileUtils;
import com.shao.camera.view.CameraDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CameraDialog cameraDialog = new CameraDialog(this);
        cameraDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bm = null;
        if (requestCode == 1004) {
            if (null == data)
                return;
            Uri originalUri = data.getData();
            Toast.makeText(MainActivity.this, originalUri+"", Toast.LENGTH_SHORT).show();

        } else if (requestCode == 1003) {
            if (data == null)
                return;
            Uri originalUri = data.getData();
            try {
                //显得到bitmap图片这里开始的第二部分，获取图片的路径：
                String path = FileUtils.getRealFilePath(MainActivity.this, originalUri);
                bm = getBitmap(path);
                System.out.println("----像素密度值---" + bm.getDensity());
                bm = BitmapCompressUtils.imageZoom(bm, 310.00);
//                    img.setImageBitmap(bm);
//                    //将bitmap放至数组中，意在bitmap的大小（与实际读取的原文件要大）
//                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                    bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//                    byte[] b = baos.toByteArray();
//                    //将字节换成KB
//                    double mid = b.length / 1024;
//                    tv.setText("存储路径" + path + "\n图片大小" + mid);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 通过路径获取Bitmap对象
     *
     * @param path
     * @return
     */
    public static Bitmap getBitmap(String path) {
        Bitmap bm = null;
        InputStream is = null;
        try {
            File outFilePath = new File(path);
            //判断如果当前的文件不存在时，创建该文件一般不会不存在
            if (!outFilePath.exists()) {
                boolean flag = false;
                try {
                    flag = outFilePath.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("---创建文件结果----" + flag);
            }
            is = new FileInputStream(outFilePath);
            bm = BitmapFactory.decodeStream(is);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bm;
    }
}
