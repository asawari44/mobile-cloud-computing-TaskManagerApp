package com.example.usertaskmanagement;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.text.CollationElementIterator;
import java.util.ArrayList;

public class RecyclerForFiles extends RecyclerView.Adapter<RecyclerForFiles.ViewHolder> {

    private ArrayList<Pictures> mData;
    private LayoutInflater mInflater;
    private RecyclerForFiles.ItemClickListener mClickListener;
    private String date;
    private String url;

    Context context;
    private String type;
    ;


    // data is passed into the constructor
    RecyclerForFiles(Context context,ArrayList<Pictures> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;

    }

    // inflates the row layout from xml when needed
    @Override
    public RecyclerForFiles.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.files_holder_row, parent, false);
        return new RecyclerForFiles.ViewHolder(view);
    }

    // binds
    //the data to the TextView in each row
    @Override
    public void onBindViewHolder(RecyclerForFiles.ViewHolder holder, int position) {
        Pictures pic = mData.get(position);
        date = pic.getCreatedTime();
        url = pic.getUrl();
        type = pic.getType();

        Log.d("msg21",url);
        holder.myTextView.setText(date);


        if (type == "pdf"){
            holder.myTextView2.setText("attachment_"+position+".pdf");
            holder.MypictureView.setImageResource(R.drawable.pdf);
        }else if (type == "audio"){
            holder.myTextView2.setText("attachment_"+position+".mp3");
            holder.MypictureView.setImageResource(R.drawable.audio);
        }
        
        
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView myTextView2;
        TextView myTextView;
        ImageView MypictureView;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = (TextView) itemView.findViewById(R.id.date);
            myTextView2 = (TextView) itemView.findViewById(R.id.name);
            MypictureView = (ImageView) itemView.findViewById(R.id.mypic);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    //String getItem(int id) {
    //   return mData.get(id);
    //}

    // allows clicks events to be caught
    void setClickListener(RecyclerForFiles.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
