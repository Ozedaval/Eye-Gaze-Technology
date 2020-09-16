package com.pwc.commsgaze;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.ViewHolder> {
    /*TODO use data from Room Database instead of temporary data*/
    public final static String TAG = "MainRecyclerViewAdapter";
    private String[] data;
    private MutableLiveData<Set<ViewHolder>> mutableLiveDataViewHolders;
    private List<View> itemViewList = new ArrayList<>();
    int selectedItem;
    Context context;
    ViewHolder viewHolder;
    public LiveData<Set<ViewHolder>> getAllBoundedViewHolders() {
        return (LiveData<Set<ViewHolder>>) mutableLiveDataViewHolders;
    }


    @NonNull
    @Override
    public MainRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_main_adapter,parent,false);
        viewHolder = new ViewHolder(view,this.context);


        if(mutableLiveDataViewHolders.getValue()!=null) {
            Set<ViewHolder> viewHolderSet = mutableLiveDataViewHolders.getValue();
            viewHolderSet.add(viewHolder);
            mutableLiveDataViewHolders.setValue(viewHolderSet);
        }
        Log.d(TAG,"Adding to boundedViewHolder " + mutableLiveDataViewHolders.getValue().toString());
        return viewHolder;
    }


    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
      /*  boundedViewHolder.remove(holder);*/
    }

    public MainRecyclerViewAdapter(String[] data, Context context){
        this.data = data;
        mutableLiveDataViewHolders = new MutableLiveData<>();
        mutableLiveDataViewHolders.setValue(new HashSet<ViewHolder>());
        this.context=context;
        this.selectedItem=1073741823;

    }
    public void selectionEffect(int position){
        int previousItem = selectedItem;
        selectedItem = position;
        notifyItemChanged(previousItem);
        notifyItemChanged(position);

    }

    @Override
    public void onBindViewHolder(@NonNull MainRecyclerViewAdapter.ViewHolder holder, final int position) {
        /*Idea of infinite scroll for recyclerview from https://stackoverflow.com/questions/51482227/recyclerview-infinite-scroll-in-both-directions*/
        int realPos = position % data.length;
        holder.textView.setText(data[realPos]);
        holder.imageView.setImageResource(R.drawable.img_recyclerview_sample);

        //TODO I don't know why the colour has not been updated even though in "selectionEffect(int position)" above notify it..
        //This is the only issue
        holder.imageView.setBackgroundColor(Color.TRANSPARENT);

        if (selectedItem == position) {
            holder.imageView.setBackgroundColor(Color.parseColor("#ffff00"));

        }



    }

    @Override
    public int getItemCount() {
       return (data == null) ? 0 : Integer.MAX_VALUE;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener {
        public ImageView imageView;
        public TextView textView;
        HashMap<View,Boolean> selected = new HashMap<>();

        Context context;

        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView_main_adapter);
            textView = itemView.findViewById(R.id.textView_main_adapter);
            this.context=context;
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            Log.d(TAG,"View " + textView.getText() +" is selected");
//            Log.d(TAG,"View " + getAdapterPosition() +" is selected");

            File externalFileDir = v.getContext().getExternalFilesDir(null);
            // for selection effect //

//
//            if(selected.get(v)==null){
//                v.setBackgroundColor(Color.parseColor("#ffff00"));
//                selected.put(v,true);
//            }
//            else{
//                if(selected.get(v)==true){
//                    v.setBackground(ContextCompat.getDrawable(context, R.drawable.border_recyclerview_item));
//                    selected.put(v,false);
//                }
//                else{
//                    v.setBackgroundColor(Color.parseColor("#ffff00"));
//                    selected.put(v,true);
//                }
//            }
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
