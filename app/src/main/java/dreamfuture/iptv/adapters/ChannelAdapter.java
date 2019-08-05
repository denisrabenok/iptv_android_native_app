package dreamfuture.iptv.adapters;

import android.graphics.Point;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dreamfuture.iptv.Epg_Channel;
import dreamfuture.iptv.MainActivity;
import dreamfuture.iptv.R;

import static dreamfuture.iptv.MainActivity.freeMemory;

public class ChannelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static MainActivity parent;
    public int selected = -1;
    public List<Epg_Channel> timeList;

    public ChannelAdapter(List<Epg_Channel> timeList, MainActivity activity) {
        parent = (MainActivity)activity;
        this.timeList = new ArrayList<>();
        if (timeList == null)
            this.timeList = null;
        else
            this.timeList.addAll(timeList);
    }

    @Override
    public ChannelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_channel,parent,false);
        return new ChannelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        ChannelViewHolder vh = (ChannelViewHolder) holder;
        vh.txtChannel.setTextSize(parent.height*19/731/parent.screen_density);
        vh.txtChannel.setText(timeList.get(position).display_name);

        RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)vh.containerLayout.getLayoutParams();
        param.height = (int)((float)parent.height*10.0f/12.0f*255.0f/353.0f/7.0f);

        vh.containerLayout.setLayoutParams(param);
//


        if(selected == position) {
//            holder.ivBackground.setImageResource(R.drawable.grid_item_border);
            vh.containerLayout.setBackgroundColor(parent.getResources().getColor(android.R.color.holo_blue_dark));
        } else {
//            holder.ivBackground.setImageBitmap(null);
            vh.containerLayout.setBackgroundColor(parent.getResources().getColor(android.R.color.transparent));
        }
//        RelativeLayout rlContainer= (RelativeLayout)parent.findViewById(R.id.rlContainer);;
        vh.txtChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (parent.isCatChanged){
                    parent.isCatChanged = false;
                    parent.releasePlayer();
                    ///////////////set title&description//////////////////
                    parent.contentCategory = parent.current_group;
                    parent.contentCatScrollY = parent.categoryRecyclerView.getScrollY();
                    parent.contentPageIndex = parent.current_page_index;
                    parent.contentPageSelected = selected;
                    parent.txtTitle.setText("");
                    parent.txtDescription.setText("");
                    parent.txtDuration.setText("");
                    parent.txtStartTime.setText("");
                    freeMemory();
                    Epg_Channel c = timeList.get(position);
                    if (parent.epg_programs != null) {
                        for (int i = 0; i < parent.epg_programs.size(); i++) {
                            if (c.channel_id.equalsIgnoreCase(parent.epg_programs.get(i).channel)) {
                                long a = Long.valueOf(parent.current_time.split(" ")[0]);
                                long start = Long.valueOf(parent.convertTimeZone(parent.epg_programs.get(i).start.split(" ")[0]));
                                long end = Long.valueOf(parent.convertTimeZone(parent.epg_programs.get(i).stop.split(" ")[0]));
                                if (a >= start && a < end) {
//                                try {
//                                    Picasso.with(parent).load(c.channel_logo_url).into(parent.img_ico);
//                                }catch (Exception e){}
                                    parent.txtTitle.setText(parent.epg_programs.get(i).title);
                                    parent.txtDescription.setText(parent.epg_programs.get(i).description);
                                    float d = parent.getdiff(parent.epg_programs.get(i).start.split(" ")[0], parent.epg_programs.get(i).stop.split(" ")[0]);
                                    String strDuration = "";
                                    if (d >= 1.0f) {
                                        if (d - (float) ((int) d) == 0.0f) {
                                            strDuration = (int) d + "h";
                                        } else {
                                            strDuration = (int) d + "h " + (int) ((d - (float) ((int) d)) * 60) + "mins";
                                        }
                                    } else {
                                        strDuration = (int) ((d - (float) ((int) d)) * 60) + "mins";
                                    }
                                    parent.txtDuration.setText(strDuration);
                                    String startAtTime = parent.epg_programs.get(i).start.split(" ")[0];
                                    startAtTime = parent.convertTimeZone(startAtTime);
                                    parent.txtStartTime.setText("Started at " + startAtTime.substring(8, 10) + ":" + startAtTime.substring(10, 12));
                                    break;
                                }
                            }
                        }
                    }
                    //////////////////////////////////////////////////////
                    selected = position;
                    String path = timeList.get(position).channel_url;
                    parent.contentUri = Uri.parse(path);
//                    parent.releasePlayer();
                    parent.initializePlayer();
                    notifyDataSetChanged();
                } else {
                    if(selected == position && parent.isPlaying) {
                        parent.go_to_fullscreen();
                        parent.isFullscreen = true;
                    } else {
                        parent.releasePlayer();
                        ///////////////set title&description//////////////////
                        parent.contentCategory = parent.current_group;
                        parent.contentCatScrollY = parent.categoryRecyclerView.getScrollY();
                        parent.contentPageIndex = parent.current_page_index;
                        parent.contentPageSelected = selected;
                        parent.txtTitle.setText("");
                        parent.txtDescription.setText("");
                        parent.txtDuration.setText("");
                        parent.txtStartTime.setText("");
                        freeMemory();
                        Epg_Channel c = timeList.get(position);
                        if (parent.epg_programs != null)
                            for (int i = 0;i<parent.epg_programs.size();i++){
                                if(c.channel_id.equalsIgnoreCase(parent.epg_programs.get(i).channel)){
                                    long a = Long.valueOf(parent.current_time.split(" ")[0]);
                                    long start= Long.valueOf(parent.convertTimeZone(parent.epg_programs.get(i).start.split(" ")[0]));
                                    long end= Long.valueOf(parent.convertTimeZone(parent.epg_programs.get(i).stop.split(" ")[0]));
                                    if(a>=start&& a< end){
    //                                try {
    //                                    Picasso.with(parent).load(c.channel_logo_url).into(parent.img_ico);
    //                                }catch (Exception e){}
                                        parent.txtTitle.setText(parent.epg_programs.get(i).title);
                                        parent.txtDescription.setText(parent.epg_programs.get(i).description);
                                        float d = parent.getdiff(parent.epg_programs.get(i).start.split(" ")[0], parent.epg_programs.get(i).stop.split(" ")[0]);
                                        String strDuration="";
                                        if (d>=1.0f){
                                            if (d-(float)((int)d)==0.0f){
                                                strDuration = (int) d+"h";
                                            }
                                            else {
                                                strDuration = (int) d+"h "+ (int)((d-(float)((int)d))*60)+"mins";
                                            }
                                        }
                                        else{
                                            strDuration = (int)((d-(float)((int)d))*60)+"mins";
                                        }
                                        parent.txtDuration.setText(strDuration);
                                        String startAtTime = parent.epg_programs.get(i).start.split(" ")[0];
                                        startAtTime = parent.convertTimeZone(startAtTime);
                                        parent.txtStartTime.setText("Started at "+startAtTime.substring(8,10)+":"+startAtTime.substring(10,12));
                                        break;
                                    }
                                }
                            }
                        //////////////////////////////////////////////////////
                        selected = position;
                        String path = timeList.get(position).channel_url;
                        parent.contentUri = Uri.parse(path);
//                    parent.releasePlayer();
                        parent.initializePlayer();
                        notifyDataSetChanged();
                    }
                }

//                parent.img_ico.setImageBitmap(null);


            }
        });
    }

    @Override
    public int getItemCount() {
        if (timeList == null)
            return 0;
        else
            return timeList.size();
    }

    public static class ChannelViewHolder extends RecyclerView.ViewHolder{

        private RelativeLayout containerLayout;
        private TextView txtChannel;

        public ChannelViewHolder(View itemView) {
            super(itemView);
            containerLayout = (RelativeLayout) itemView.findViewById(R.id.rlContainer);
            txtChannel = (TextView) itemView.findViewById(R.id.txtChannel);
            Display display = parent.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            txtChannel.setTextSize(width/6/30);
        }
    }

}
