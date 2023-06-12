package com.hania.vidsaver;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;
import com.yausername.youtubedl_android.mapper.VideoInfo;

import java.io.File;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;
import kotlin.jvm.functions.Function3;


public class download extends AppCompatActivity {


    NotificationManager notificationManager;

    private boolean downloading = false;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final String processId = "MyDlProcess";
    private String videoLocation;
    EditText etUrl;
    public String[] name;


    private final Function3<Float, Long, String, Unit> callback = (progress, o2, line) -> {
        runOnUiThread(() -> {

                    show("downloading...", (long) progress.floatValue());

                    if (line.contains("Destination:")) {
                        name = line.split("Destination: ");
                        videoLocation = name[1];
                    }
                }
        );
        return Unit.INSTANCE;
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(v -> startDownload());
        etUrl = findViewById(R.id.et_url);
        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);




        String patha = "/storage/emulated/0/Download/vidSave/🕊𝗟𝗼𝗹𝗹𝘆𝘄𝗼𝗼𝗱 🕊 on Reels ｜.f816010386624789a-1.m4a";
        Button open = findViewById(R.id.button2);





        open.setOnClickListener(v -> {
//            String filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setDataAndType(Uri.parse(filepath), "file/*");
//
//            // Start the activity to open the download directory.
//            startActivity(intent);


            startActivity(new Intent(this, videos.class));
        });

    }


    private void startDownload() {
        if (downloading) {
            Toast.makeText(this, "cannot start download. a download is already in progress", Toast.LENGTH_LONG).show();
            return;
        }


        String url = etUrl.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            etUrl.setError("error");
            return;
        }

        YoutubeDLRequest request = new YoutubeDLRequest(url);
        File youtubeDLDir = getDownloadLocation();
        File config = new File(youtubeDLDir, "config.txt");



        if (config.exists()) {
            request.addOption("--config-location", config.getAbsolutePath());
        } else {
            request.addOption("--no-mtime");
            request.addOption("--downloader", "libaria2c.so");
            request.addOption("--external-downloader-args", "aria2c:\"--summary-interval=1\"");
            request.addOption("-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best");
            request.addOption("-o", youtubeDLDir.getAbsolutePath() + "/%(title)s.%(ext)s");
        }


        downloading = true;
        Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().execute(request, processId, callback))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(youtubeDLResponse -> {
                    finishDownload();
                    downloading = false;
                }, e -> {
                    Log.e("users", e.getMessage());
                    onError(e.getMessage());
                    downloading = false;
                });
        compositeDisposable.add(disposable);

    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    @NonNull
    private File getDownloadLocation() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File youtubeDLDir = new File(downloadsDir, "vidSave");
        if (!youtubeDLDir.exists()) youtubeDLDir.mkdir();
        return youtubeDLDir;
    }


    private static final String CHANNEL_ID = "download_progress";

    public void show(String title, long progress) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Download Progress",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Shows a notification for download progress");
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(download.this, CHANNEL_ID)
                .setSmallIcon(R.drawable.download)
                .setContentTitle(title)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setProgress(100, (int) progress, false)
                .build();

        notificationManager.notify(0, notification);
    }


    private void finishDownload() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Download complete")
                .setContentText("Click to open")
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setSmallIcon(R.drawable.download)
                .setContentIntent(getPendingIntent())
                .build();

        notificationManager.notify(0, notification);
    }


    private void onError(String error) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Error occurred")
                .setContentText(error)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setSmallIcon(R.drawable.error)
                .build();

        notificationManager.notify(0, notification);
    }

    private PendingIntent getPendingIntent() {
        Log.d("users", videoLocation);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoLocation));
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }
}
