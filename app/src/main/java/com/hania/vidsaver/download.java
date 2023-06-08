package com.hania.vidsaver;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLRequest;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;
import kotlin.jvm.functions.Function3;


public class download extends AppCompatActivity {


    private EditText etUrl;
    TextView textView;

    private boolean downloading = false;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    NotificationManager notificationManager;

    private String processId = "MyDlProcess";
    public String downLocation;
    public String[] name;


    private final Function3<Float, Long, String, Unit> callback = (progress, o2, line) -> {
        runOnUiThread(() -> {
            if(line.contains("Destination")){
                name= line.split("vidSave/");

                Log.d("users", String.valueOf(progress.floatValue())+"/n"+ name[1]);
            }
                }
        );
        return Unit.INSTANCE;
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        etUrl = findViewById(R.id.etUrls);
        Button downloadButton = findViewById(R.id.button);
        downloadButton.setOnClickListener(v -> startDownload());
        notificationManager  = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);


    }


    private void startDownload() {
        if (downloading) {
            Toast.makeText(download.this, "cannot start download. a download is already in progress", Toast.LENGTH_LONG).show();
            return;
        }


        String url = etUrl.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, "enter a url", Toast.LENGTH_SHORT).show();
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
                .subscribe(youtubeDLResponse -> downloading = false, e -> {
                    textView.setText(e.getMessage());
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

    public void show(String title, String text, long progress) {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

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
                .setContentText(text)
                .setProgress(100, (int) progress, false)
                .build();

        notificationManager.notify(0, notification);
    }




    private void updateNotification(long progress) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Download Progress",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Shows a notification for download progress");
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Downloading  " + String.valueOf(progress)+ "%")
                .setProgress(100, (int) progress, false)
                .setSmallIcon(R.drawable.download)
                .build();

        notificationManager.notify(0, notification);

    }

    private void finishDownload() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Download complete")
                .setContentText("Click to open")
                .setSmallIcon(R.drawable.download)
                .setContentIntent(getPendingIntent(this, Uri.parse("file:///path/to/downloaded/file")))
                .build();

        notificationManager.notify(0, notification);
    }

    private PendingIntent getPendingIntent(Context context, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

}
