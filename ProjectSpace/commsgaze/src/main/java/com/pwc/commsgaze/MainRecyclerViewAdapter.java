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
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.pwc.commsgaze.database.Content;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.ViewHolder> {
    public final static String TAG = "MainRecyclerViewAdapter";
    private List<Content> contents;


    @NonNull
    @Override
    public MainRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_main_adapter,parent,false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MainRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.textView.setText(contents.get(position).getWord());
        Uri imageUri = Uri.fromFile(new File(contents.get(position).getImageDirPath()));
        Glide.with(holder.itemView.getContext())
                .load(imageUri)
                .apply(new RequestOptions().override(250, 250))
                .into(holder.imageView);
        holder.setAudioDirPath(contents.get(position).getAudioDirPath());

    }

    void setContents(List<Content> contents){
        this.contents = contents;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if(contents != null)
        return contents.size();
        return  0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
         ImageView imageView;
         TextView textView;
         String audioDirPath;

        public void setAudioDirPath(String audioDirPath) {
            this.audioDirPath = audioDirPath;
        }

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            imageView = itemView.findViewById(R.id.imageView_main_adapter);
            textView = itemView.findViewById(R.id.textView_main_adapter);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG,"View " + textView.getText() +" is selected");

            File audioFilePath = new File(audioDirPath);
            Uri audioUri = Uri.fromFile(audioFilePath);
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