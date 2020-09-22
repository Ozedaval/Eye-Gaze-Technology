package com.pwc.commsgaze;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;

public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.ViewHolder> {
    /*TODO use data from Room Database instead of temporary data*/
    public final static String TAG = "MainRecyclerViewAdapter";
    private String[] data;




    @NonNull
    @Override
    public MainRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_main_adapter,parent,false);
        return new ViewHolder(view);
    }



    public MainRecyclerViewAdapter(String[] data){
        this.data = data;


    }

    @Override
    public void onBindViewHolder(@NonNull MainRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.textView.setText(data[position]);
        holder.imageView.setImageResource(R.drawable.img_recyclerview_sample);
    }

    @Override
    public int getItemCount() {
        return data.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener {
        public ImageView imageView;
        public TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            imageView = itemView.findViewById(R.id.imageView_main_adapter);
            textView = itemView.findViewById(R.id.textView_main_adapter);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG,"View " + textView.getText() +" is selected");
            File externalFileDir = v.getContext().getExternalFilesDir(null);

            /*Sample is being used here*/
            File sampleAudioFilePath = new File(externalFileDir,"audio_topic1_sample1.wav");
            Uri audioUri = Uri.fromFile(sampleAudioFilePath);
            final MediaPlayer  mediaPlayer = new MediaPlayer();

            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
            try {
                mediaPlayer.setDataSource(v.getContext(), audioUri);
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mediaPlayer.start();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
