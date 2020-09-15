package com.pwc.explore.eyegaze.sparseflowSelection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.pwc.explore.R;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> implements Runnable{

    private List<String> mItemList;
    private Context mContext;
    int selectedItem;
    private OnItemListener onItemListener;
    double itemWidth;
    double itemHeight;
    Class[] activities = {Element1.class,Element2.class};

    public ItemAdapter(List<String> itemList, Context context, OnItemListener onItemListener){
        this.mItemList = itemList;
        this.mContext = context;
        this.onItemListener = onItemListener;
        selectedItem =-1;
    }


    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new ItemViewHolder(view, onItemListener);
    }

    public void select(int position) {
        int previousItem = selectedItem;
        selectedItem = position;
        notifyItemChanged(previousItem);
        notifyItemChanged(position);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        String item = mItemList.get(position);
        holder.textView.setText(item);

        int left = dpToPx(24);
        int top = dpToPx(12);
        int right = dpToPx(24);
        int bottom = dpToPx(12);
        holder.cardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.colorPrimaryLight));
        if (selectedItem == position) {
            holder.cardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.selected));
        }

        int spanCount =2;
        itemWidth = holder.itemView.getWidth();
        itemHeight = holder.itemView.getHeight();

        boolean isFirst2Items = position <spanCount;
        boolean isLast2Items = position > getItemCount() - spanCount-1;

        if( isFirst2Items){
            top = dpToPx(80);

        }
        if(isLast2Items){
            bottom = dpToPx(6);
        }

        boolean isLeftSide = (position +1) % spanCount !=0;
        boolean isRightSide = !isLeftSide;

        if (isLeftSide){
            right = dpToPx(12);
        }
        if(isRightSide){
            left = dpToPx(12);
        }


        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) holder.cardView.getLayoutParams();
        layoutParams.setMargins(left, top, right, bottom);

        holder.cardView.setLayoutParams(layoutParams);
    }

    private int dpToPx(int dp){
        float px = dp * mContext.getResources().getDisplayMetrics().density;
        return (int) px;
    }


    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    @Override
    public void run() {

    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        OnItemListener onItemListener;
        private CardView cardView;
        private TextView textView;

        ItemViewHolder(View itemView, OnItemListener onItemListener){
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            textView = itemView.findViewById(R.id.text_view);
            this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemListener.onItemClick(getAdapterPosition());
        }
        public void select(){
        }
    }

    public interface OnItemListener{
        void onItemClick(int position);
    }

}
