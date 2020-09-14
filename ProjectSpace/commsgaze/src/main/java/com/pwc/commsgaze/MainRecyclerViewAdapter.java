package com.pwc.commsgaze;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
            Log.d(TAG,"View " +textView.getText() +" is selected");

        }
    }

}
