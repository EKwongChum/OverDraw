package com.example.erkang.overdraw;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by erkang on 2017/7/26.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private Context context;
    private int remainder;
    private int amount;
    public MyAdapter(Context context, int amount) {
        this.context = context;
        this.amount = amount;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recyclerview, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        remainder = position % 4 + 1;
        switch (remainder) {
            case 1:
                setItemContent(holder,R.drawable.infernal_affairs_1,R.string.dialog_1);
                break;
            case 2:
                setItemContent(holder,R.drawable.infernal_affairs_2,R.string.dialog_2);
                break;
            case 3:
                setItemContent(holder,R.drawable.infernal_affairs_3,R.string.dialog_3);
                break;
            case 4:
                setItemContent(holder,R.drawable.infernal_affairs_4,R.string.dialog_4);
                break;
        }
    }
    private void setItemContent(MyViewHolder holder,int drawId,int stringId) {
        holder.iv.setImageResource(drawId);
        holder.tv.setText(stringId);
    }
    @Override
    public int getItemCount() {
        return amount;
    }
    class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv;
        private TextView tv;
        public MyViewHolder(View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.item_iv);
            tv = itemView.findViewById(R.id.item_tv);
        }
    }
}