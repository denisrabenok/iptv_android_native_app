package dreamfuture.iptv.adapters;

import android.app.Activity;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import dreamfuture.iptv.MainActivity;
import dreamfuture.iptv.R;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.MyHolder> {
    private MainActivity parent;
    private int selected = -1;

    public class MyHolder extends RecyclerView.ViewHolder {
        public LinearLayout llContainer;

        public MyHolder(View v) {
            super(v);

            llContainer = (LinearLayout)v.findViewById(R.id.llContainer);
        }
    }

    public ScheduleAdapter(Activity activity) {
        parent = (MainActivity)activity;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule_row, parent, false);
        return new MyHolder(inflatedView);
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    @Override
    public void onBindViewHolder(final MyHolder holder, final int position) {
        Display display = parent.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)holder.llContainer.getLayoutParams();
        param.height = (int)(width*0.05);
        holder.llContainer.setLayoutParams(param);

        for(int i = 0 ; i < 10 ; i++) {
            RelativeLayout layout = (RelativeLayout) LayoutInflater.from(parent).inflate(R.layout.item_schedule, null, false);
            TextView txt = (TextView) layout.findViewById(R.id.txtEpg);
            txt.setText("Epg"+i);

            LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            param1.width = (i+1)*50;
            param1.height = param.height;
            layout.setLayoutParams(param1);

            holder.llContainer.addView(layout);
        }
    }
}
