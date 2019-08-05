package dreamfuture.iptv.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dreamfuture.iptv.Epg_Program;
import dreamfuture.iptv.MainActivity;
import dreamfuture.iptv.R;


/**
 * Created by Sathishkumar.P07 on 7/20/2016.
 */
public class ProgramsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public MainActivity parent;
    private final String LOG_TAG = "ProgramsAdapter";
    public List<Epg_Program> programList;
    private OnItemClickListener mItemClickListener;

    public ProgramsRecyclerViewAdapter(List<Epg_Program> programList, MainActivity activity) {
        this.programList = new ArrayList<>();
        if (programList == null)
            this.programList = null;
        else
            this.programList.addAll(programList);
        parent = activity;
    }

    public ProgramsRecyclerViewAdapter() {
        super();
    }

    @Override
    public ProgramViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.tile_program, parent, false);
        return new ProgramViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ProgramViewHolder vh = (ProgramViewHolder) holder;
        Epg_Program program = programList.get(position);

        String minT;
        String maxT;

        if (position==0){
            minT = parent.minT;
        }
        else{
            minT = programList.get(position).start.split(" ")[0];
        }
        if (position == programList.size()-1){
            maxT = programList.get(position).stop.split(" ")[0];
        }
        else {
            Epg_Program p = programList.get(position+1);
            maxT = p.start.split(" ")[0];
        }

        vh.tvProgramTitle.setText(program.title);

        ViewGroup.LayoutParams layoutParams = vh.rl_program_content_container.getLayoutParams();
        float d = parent.getdiff(minT, maxT);
        layoutParams.width = (int)(d*(float) parent.width/12.5f*2.0f);
        layoutParams.height = parent.height/15;
        vh.rl_program_content_container.setLayoutParams(layoutParams);
    }

    @Override
    public int getItemCount() {
        if (programList == null)
            return 0;
        else
            return programList.size();
    }

    private class ProgramViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView tvProgramTitle;
        private RelativeLayout rl_program_content_container;

        public ProgramViewHolder(View itemView) {
            super(itemView);
            tvProgramTitle = (TextView) itemView.findViewById(R.id.tv_program_title);
            tvProgramTitle.setTextSize(parent.height*20/731/parent.screen_density);
            rl_program_content_container = (RelativeLayout)itemView.findViewById(R.id.rl_program_content_container);
//            rl_program_content_container.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
//            if (mItemClickListener != null) {
//                mItemClickListener.onItemClick(view, getAdapterPosition(), programList.get(getAdapterPosition()));
//            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, Epg_Program program);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
}
