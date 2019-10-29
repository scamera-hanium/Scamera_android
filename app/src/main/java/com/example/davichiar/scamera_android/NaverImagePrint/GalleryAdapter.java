package com.example.davichiar.scamera_android.NaverImagePrint;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import com.example.davichiar.scamera_android.R;

public class GalleryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<NaverImageItem> imageItems;
    Context context;

    public GalleryAdapter(ArrayList<NaverImageItem> imageItems, Context context) {
        this.imageItems = imageItems;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.image, parent, false);
        viewHolder = new MyItemHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (imageItems.get(position) != null) {
            Ion.with(context).load(imageItems.get(position).getLink()).withBitmap()
                    .placeholder(R.mipmap.placeholder).intoImageView(((MyItemHolder) holder).mImg);
        }
    }

    @Override
    public int getItemCount() {
        return imageItems.size();
    }

    class MyItemHolder extends RecyclerView.ViewHolder {
        ImageView mImg;
        public MyItemHolder(View itemView) {
            super(itemView);

            mImg = (ImageView) itemView.findViewById(R.id.item_img);
        }
    }
}
