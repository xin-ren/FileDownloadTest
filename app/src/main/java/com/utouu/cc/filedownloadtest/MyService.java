package com.utouu.cc.filedownloadtest;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;

/**
 * Create by 任新 on 2017/1/17 14:12
 * Function：
 * Desc：
 */
public class MyService extends Service {

    public static final String TAG = "MyService";
    private Long mTaskId;
    private MyService.MyBinder mBinder = new MyService.MyBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() executed");
    }


    //定义广播接受者，接收下载状态
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //检查下载状态
            checkDownloadStatus();
        }
    };

    //检查下载状态
    private void checkDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        //筛选下载任务，传入任务id，可变参数
        query.setFilterById(mTaskId);
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_PAUSED:
                    Log.i(TAG, "------> 下载暂停");
                case DownloadManager.STATUS_PENDING:
                    Log.i(TAG, "------> 下载延迟");
                case DownloadManager.STATUS_RUNNING:
                    Log.i(TAG, "------> 正在下载");
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    Log.i(TAG, "------> 下载完成");
                    //下载完成安装APK
                    String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + "rrrr";
                    installAPK(new File(downloadPath));
                    Log.i(TAG, "------> 文件路径：" + downloadPath);
                    break;
                case DownloadManager.STATUS_FAILED:
                    Log.i(TAG, "------> 下载失败");
                    break;
            }
        }
    }

    //下载到本地后执行安装
    private void installAPK(File file) {
        if (!file.exists()) return;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse("file://" + file.toString());
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        //在服务中开启activity必须设置flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        Log.i(TAG, "安装成功");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() executed");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() executed");
        unregisterReceiver(receiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class MyBinder extends Binder {
        //使用系统下载器下载
        public void downloadAPK(String versionUrl, String versionName) {
            //创建下载任务
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(versionUrl));
            request.setAllowedOverRoaming(false); //漫游网络是否可以下载

            //设置文件类型，可以在下载完后直接打开该文件
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(versionUrl));
            request.setMimeType(mimeString);

            //在通知栏中显示，默认的就是显示
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            request.setVisibleInDownloadsUi(true);

            //sdcard的目录下的download文件夹，必须设置
            request.setDestinationInExternalPublicDir("/download/", versionName);
            //也可以自己设置下载路径
            //request.setDestinationInExternalFilesDir();

            //获取下载管理器
            DownloadManager downLoadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            //将下载加入到下载的请求对列
            //加入下载队列后会给该任务返回一个long型的id,通过该id可以取消任务，重启任务等等
            mTaskId = downLoadManager.enqueue(request);

            //注册广播接受者，监听下载状态
            registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
    }
}