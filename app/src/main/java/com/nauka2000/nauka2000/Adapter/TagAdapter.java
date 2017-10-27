package com.nauka2000.nauka2000.Adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nauka2000.nauka2000.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {
    Context context;
    OnItemClickListener clickListener;
    private ArrayList TagList;

    public TagAdapter(Context context) {
        this.context = context;
    }

    public TagAdapter(ArrayList<HashMap<String, String>> TagList) {
        this.TagList = TagList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view;

        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_tag, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder ViewHolder, int i) {
        Map<String, String> hashmap;
        hashmap = (Map<String, String>) TagList.get(i);

        ViewHolder.title.setText(hashmap.get("name"));
        ViewHolder.desc.setText(hashmap.get("desc"));
        ViewHolder.count.setText(hashmap.get("count"));
    }

    @Override
    public int getItemCount() {
        return TagList == null ? 0 : TagList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView cardItemLayout;
        TextView title;
        TextView desc;
        TextView count;

        public ViewHolder(View itemView) {
            super(itemView);

            cardItemLayout = (CardView) itemView.findViewById(R.id.tag_cardlist_item);
            title = (TextView) itemView.findViewById(R.id.title);
            desc = (TextView) itemView.findViewById(R.id.desc);
            count = (TextView) itemView.findViewById(R.id.count);
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
