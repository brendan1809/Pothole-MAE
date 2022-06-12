package com.pothole.my;

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList;
import android.net.Uri
import android.graphics.BitmapFactory

import android.graphics.Bitmap
import com.squareup.picasso.Picasso
import java.io.InputStream
import java.net.URL


class PotholeAdapter(private val potholeList : ArrayList<PotholeClass>) : RecyclerView.Adapter<PotholeAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item,parent,false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = potholeList[position]
        Picasso.get().load(currentItem.image).into(holder.imageTitle)
        holder.addressText.text = currentItem.address
    }

    override fun getItemCount(): Int {
        return potholeList.size
    }


    class ViewHolder(itemView : View): RecyclerView.ViewHolder(itemView){
        val imageTitle:ImageView = itemView.findViewById(R.id.image_title)
        val addressText:TextView = itemView.findViewById(R.id.address_text)
    }
}
