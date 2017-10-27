package com.nauka2000.nauka2000.Adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.nauka2000.nauka2000.Classes.CircleTransform;
import com.nauka2000.nauka2000.R;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    Context context;
    OnItemClickListener clickListener;
    private ArrayList PostList;

    public PostAdapter(Context context) {
        this.context = context;
    }

    public PostAdapter(ArrayList<HashMap<String, String>> PostList) {
        this.PostList = PostList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view;

        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_post, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder ViewHolder, int i) {
        Map<String, String> hashmap;
        hashmap = (Map<String, String>) PostList.get(i);

        ViewHolder.title.setText(hashmap.get("title"));
        ViewHolder.excerpt.setText(hashmap.get("excerpt"));
        ViewHolder.date.setText(hashmap.get("date"));

        if(hashmap.get("thumbnail").isEmpty() || hashmap.get("thumbnail") == null){
            ViewHolder.imagetumb.setImageResource(R.drawable.icon);
        }
        else{
            Context imageContext = ViewHolder.imagetumb.getContext();
            Picasso.with(imageContext)
                    .load(hashmap.get("thumbnail"))
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .placeholder(R.drawable.load_image)
                    .error(R.drawable.error_load_image)
                    .fit()
                    .transform(new CircleTransform())
                    .into(ViewHolder.imagetumb);
        }
    }

    @Override
    public int getItemCount() {
        return PostList == null ? 0 : PostList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView cardItemLayout;
        TextView title;
        TextView excerpt;
        TextView date;
        ImageView imagetumb;

        public ViewHolder(View itemView) {
            super(itemView);

            cardItemLayout = (CardView) itemView.findViewById(R.id.post_cardlist_item);
            title = (TextView) itemView.findViewById(R.id.title);
            excerpt = (TextView) itemView.findViewById(R.id.excerpt);
            date = (TextView) itemView.findViewById(R.id.date);
            imagetumb = (ImageView) itemView.findViewById(R.id.imageTumb);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(v, getAdapterPosition());
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }
}
