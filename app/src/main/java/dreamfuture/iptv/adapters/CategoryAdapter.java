package dreamfuture.iptv.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dreamfuture.iptv.MainActivity;
import dreamfuture.iptv.R;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyHolder> {
    private MainActivity parent;
    public List<String> categories;
    public int selected = -1;

    public class MyHolder extends RecyclerView.ViewHolder {
        public RelativeLayout rlContainer;
        public TextView txtCategory;
        public TextView txtVod;
        public ImageView ivBackground;

        public MyHolder(View v) {
            super(v);

            rlContainer = (RelativeLayout)v.findViewById(R.id.rlContainer);
            txtCategory = (TextView)v.findViewById(R.id.txtCategory);
            txtVod = (TextView) v.findViewById(R.id.txtVod);
            ivBackground = (ImageView)v.findViewById(R.id.ivBackground);
        }
    }

    public CategoryAdapter(Activity activity, List<String> a) {
        parent = (MainActivity)activity;
        categories = new ArrayList<>();
        categories.addAll(a);
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new MyHolder(inflatedView);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    @Override
    public void onBindViewHolder(final MyHolder holder, final int position) {

        RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)holder.rlContainer.getLayoutParams();
        param.height = parent.height/10;
        holder.rlContainer.setLayoutParams(param);

        holder.txtCategory.setText(categories.get(position));
        int k = 0;
        if (parent.vod_flag){
            for (int i = 0; i < parent.vod_channels.size(); i++) {
                if (categories.get(position).equals(parent.vod_channels.get(i).group_title)) {
                    k++;
                }
            }
        }
        else {
            if (categories.get(position).equals("All Channels"))
                k = parent.epg_channels.size() + parent.vod_channels.size();
            if (categories.get(position).equals("Video on demand"))
                k = parent.vod_channels.size();
            else {
                for (int i = 2; i < parent.epg_channels.size(); i++) {
                    if (categories.get(position).equals(parent.epg_channels.get(i).group_title)) {
                        k++;
                    }
                }
            }
        }
        holder.txtVod.setText(k+"");
        holder.txtCategory.setTextSize(parent.height/40/parent.screen_density);
        holder.txtVod.setTextSize(parent.height/50/parent.screen_density);
        if(selected == position) {
            holder.ivBackground.setImageResource(R.drawable.grid_item_border);

            holder.rlContainer.setBackgroundColor(parent.getResources().getColor(android.R.color.holo_blue_dark));
        } else {
            holder.ivBackground.setImageBitmap(null);
            holder.rlContainer.setBackgroundColor(parent.getResources().getColor(android.R.color.transparent));
        }
        holder.rlContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selected == position){
                    parent.init();
                }
                else {
                    parent.current_group = categories.get(position);
                    parent.txtAllChannels.setText(parent.current_group);
                    if (categories.get(position).equals("Video on demand")){
                        parent.vod_flag = true;
                        selected = -1;
                        categories = parent.getCategoryList();
                    }
                    else
                        selected = position;
                    parent.refreshChannels();
                    notifyDataSetChanged();
                }
            }
        });
    }
}
