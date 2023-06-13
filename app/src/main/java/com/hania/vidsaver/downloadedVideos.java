package com.hania.vidsaver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class downloadedVideos extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<File> videos = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloaded_videos);


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get all videos in the vidSaver folder
        File vidSaverFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "vidSave");
        if (vidSaverFolder.exists() && vidSaverFolder.isDirectory()) {
            File[] files = vidSaverFolder.listFiles();
            for (File file : files) {
                if (file.getName().endsWith(".mp4")) {
                    videos.add(file);
                }
            }
        }

        // Create a recycler view adapter and set it to the recycler view
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, videos);
        recyclerView.setAdapter(adapter);

    }



    private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private List<File> videos;
        private Context context;

        public RecyclerViewAdapter(Context context, List<File> videos) {
            this.videos = videos;
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.video_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            File video = videos.get(position);
            holder.videoName.setText(video.getName());
            holder.videoThumbnail.setImageBitmap(getVideoThumbnail(video));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "Playing video: " + video.getName(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return videos.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {

            public TextView videoName;
            public ImageView videoThumbnail;

            public ViewHolder(View itemView) {
                super(itemView);

                videoName = itemView.findViewById(R.id.videoName);
                videoThumbnail = itemView.findViewById(R.id.videoThumbnail);
            }
        }
    }

    private Bitmap getVideoThumbnail(File video) {
        // Get the video thumbnail
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(video.getPath());
        Bitmap thumbnail = retriever.getFrameAtTime(0);

        // Return the thumbnail
        return thumbnail;
    }
}