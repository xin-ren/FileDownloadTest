package com.utouu.cc.filedownloadtest;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by 任新 on 2017/1/17 10:14.
 * Function: 下载文件
 * Desc:
 */
public class MainActivity extends AppCompatActivity {

    private MyService.MyBinder myBinder;
    private String mFileName = "rrrr";
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (MyService.MyBinder) service;
            myBinder.downloadAPK("http://www.apk.anzhi.com/Anzhi_2665180_2723304_2.apk?src=/data3/apk/201612/30/7e84d7466ab34ec95689ebd9e8048721_94105100.apk", mFileName);
        }
    };

    private Button mBtnDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       /* //下载链接
        String downloadUrl = "";
        String filName = "";
        //创建下载任务
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        //指定下载路径和下载文件名
        request.setDestinationInExternalPublicDir("/snow/",filName);
        //获取下载管理器
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        //将下载任务加入下载队列，否则不会进行下载
        downloadManager.enqueue(request);*/

        mBtnDownload = (Button) findViewById(R.id.btn_download);
        mBtnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(MainActivity.this, MyService.class);
                bindService(startIntent, connection, BIND_AUTO_CREATE);
                startService(startIntent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent stopIntent = new Intent(this, MyService.class);
        stopService(stopIntent);
        unbindService(connection);
    }

}
