package dreamfuture.iptv.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dreamfuture.iptv.MainActivity;
import dreamfuture.iptv.R;


/**
 * Created by Sathishkumar.P07 on 7/25/2016.
 */
public class TimeAdapter extends RecyclerView.Adapter<TimeAdapter.TimeViewHolder> {

    private List<String> timeList;
    private static MainActivity parent;

    public TimeAdapter(List<String> timeList, MainActivity activity) {
        this.timeList = new ArrayList<>();
        if (timeList == null)
            this.timeList = null;
        else
            this.timeList.addAll(timeList);
        parent = activity;
    }

    @Override
    public TimeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tile_time,parent,false);
        return new TimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TimeViewHolder holder, int position) {
        holder.tvTime.setText(timeList.get(position));
        ViewGroup.LayoutParams layoutParams = holder.containerLayout.getLayoutParams();
//        layoutParams.height = 50;
//        layoutParams.width = 300;
        holder.containerLayout.setLayoutParams(layoutParams);
//        holder.containerLayout.setBackgroundColor(getRandomColor());
    }

    @Override
    public int getItemCount() {
        if (timeList == null)
            return 0;
        else
            return timeList.size();
    }

    public static class TimeViewHolder extends RecyclerView.ViewHolder{

        private RelativeLayout containerLayout;
        private TextView tvTime;

        public TimeViewHolder(View itemView) {
            super(itemView);
            containerLayout = (RelativeLayout) itemView.findViewById(R.id.ll_time_container);
            tvTime = (TextView) itemView.findViewById(R.id.tv_tile_time);

            tvTime.setTextSize(parent.height*20/731/parent.screen_density);
            RecyclerView.LayoutParams param_tr = (RecyclerView.LayoutParams) containerLayout.getLayoutParams();
            param_tr.width = (int)(parent.width/12.5);
            containerLayout.setLayoutParams(param_tr);
        }
    }
}
