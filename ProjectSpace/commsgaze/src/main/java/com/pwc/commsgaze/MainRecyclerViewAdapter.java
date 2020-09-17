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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.ViewHolder> {
    /*TODO use data from Room Database instead of temporary data*/
    public final static String TAG = "MainRecyclerViewAdapter";
    private String[] data;
    private MutableLiveData<ArrayList<ViewHolder>> mutableLiveDataViewHolders;


    public LiveData<ArrayList<ViewHolder>> getAllBoundedViewHolders() {
        return (LiveData<ArrayList<ViewHolder>>) mutableLiveDataViewHolders;
    }


    @NonNull
    @Override
    public MainRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_main_adapter,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        if(mutableLiveDataViewHolders.getValue()!=null) {
            ArrayList<ViewHolder> viewHolderSet = mutableLiveDataViewHolders.getValue();
            viewHolderSet.add(viewHolder);
            mutableLiveDataViewHolders.setValue(viewHolderSet);
        }
        Log.d(TAG,"Adding to boundedViewHolder " + mutableLiveDataViewHolders.getValue().toString());
        return viewHolder;
    }



    public MainRecyclerViewAdapter(String[] data){
        this.data = data;
        mutableLiveDataViewHolders = new MutableLiveData<>();
        mutableLiveDataViewHolders.setValue(new ArrayList<ViewHolder>());

    }

    @Override
    public void onBindViewHolder(@NonNull MainRecyclerViewAdapter.ViewHolder holder, int position) {
        /*Idea of infinite scroll for recyclerview from https://stackoverflow.com/questions/51482227/recyclerview-infinite-scroll-in-both-directions*/
        int realPos = position % data.length;
        holder.textView.setText(data[realPos]);
        holder.imageView.setImageResource(R.drawable.img_recyclerview_sample);
    }

    @Override
    public int getItemCount() {
       return (data == null) ? 0 : Integer.MAX_VALUE;
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
