package com.pwc.commsgaze;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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
    private int deviceHeight;
    private int deviceWidth;
    private int fixedDimension;

    MainRecyclerViewAdapter(DisplayMetrics displayMetrics,int fixedDimension){
        /*TODO to consider for vertical orientation as well*/
        deviceHeight = displayMetrics.heightPixels  ;
        deviceWidth = displayMetrics.widthPixels;
        Log.d(TAG,"Device Height "+ deviceHeight);
        Log.d(TAG,"Device Width "+ deviceWidth);
        this.fixedDimension = fixedDimension;

    }

    @NonNull
    @Override
    public MainRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_mainadapter_item,parent,false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MainRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.textView.setText(contents.get(position).getWord());
        Uri imageUri = Uri.fromFile(new File(contents.get(position).getImageDirPath()));
        int suitableDimension = deviceWidth/(fixedDimension+2);
        Glide.with(holder.itemView.getContext())
                .load(imageUri)
                .apply(new RequestOptions().override(suitableDimension,suitableDimension))
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
        CardView cardView;

        public void setAudioDirPath(String audioDirPath) {
            this.audioDirPath = audioDirPath;
        }

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            imageView = itemView.findViewById(R.id.imageView_main_adapter);
            textView = itemView.findViewById(R.id.textView_main_adapter);
            cardView = itemView.findViewById(R.id.cardView_main_adapter);
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