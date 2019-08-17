package com.example.e_commerce.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_commerce.Interface.ItemClickListener;
import com.example.e_commerce.R;

public class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{

    public TextView textProductDescription, textProductName, textProductPrice;
    public ImageView productImageView;
    public ItemClickListener itemClickListener;


    public ProductViewHolder(@NonNull View itemView)
    {
        super(itemView);
        productImageView = (ImageView) itemView.findViewById(R.id.product_image);
        textProductDescription = (TextView) itemView.findViewById(R.id.product_description);
        textProductName = (TextView) itemView.findViewById(R.id.product_name_items_layout);
        textProductPrice = (TextView) itemView.findViewById(R.id.product_price);

    }

    public void setItemClickListener(ItemClickListener listener)
    {
        itemClickListener = listener;
    }

    @Override
    public void onClick(View view)
    {
        itemClickListener.onClick(view, getAdapterPosition(), false);
    }
}
