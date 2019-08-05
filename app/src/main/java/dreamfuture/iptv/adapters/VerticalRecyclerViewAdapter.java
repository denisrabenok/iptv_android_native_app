package dreamfuture.iptv.adapters;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import dreamfuture.iptv.Epg_Channel;
import dreamfuture.iptv.Epg_Program;
import dreamfuture.iptv.MainActivity;
import dreamfuture.iptv.R;


/**
 * Created by Sathishkumar.P07 on 7/21/2016.
 */
public class VerticalRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String LOG_TAG = "VerticalRecyViewAdapter";
    //    private static final int PROGRAM_COUNT = 20;
    public List<Epg_Channel> channelList;
    private MainActivity parent;
    private ProgramsRecyclerViewAdapter.OnItemClickListener mItemClickListener;

    private int tempHeight = 0;
    public VerticalRecyclerViewAdapter(List<Epg_Channel> channelList, MainActivity activity) {
        this.channelList = new ArrayList<Epg_Channel>();
//        this.channelList.addAll(channelList);
        this.channelList = channelList;
        parent = activity;
        tempHeight = (int)((float)parent.height*10.0f/12.0f*255.0f/353.0f/7.0f);
    }

    @Override
    public VerticalRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.verticle_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder vh = (ViewHolder) holder;

        RelativeLayout.LayoutParams param_rcvContainer = (RelativeLayout.LayoutParams)vh.rlContainer.getLayoutParams();
        param_rcvContainer.height = tempHeight;
        vh.rlContainer.setLayoutParams(param_rcvContainer);

        List<Epg_Program> programList = new ArrayList<>();
        if (MainActivity.epg_programs == null)
            programList = null;
        else
            programList.addAll(getTestData(channelList.get(position).channel_id));
        ProgramsRecyclerViewAdapter adapter = (ProgramsRecyclerViewAdapter)vh.rcvHorizontal.getAdapter();
        if(adapter == null) {
            ProgramsRecyclerViewAdapter mAdapter = new ProgramsRecyclerViewAdapter(programList, parent);
            vh.rcvHorizontal.setAdapter(mAdapter);
            mAdapter.setOnItemClickListener(mItemClickListener);
        } else {
            adapter.programList = programList;
            adapter.parent = parent;
            adapter.notifyDataSetChanged();
            adapter.setOnItemClickListener(mItemClickListener);
        }
    }

    @Override
    public int getItemCount() {
        if (channelList == null)
            return 0;
        else
            return channelList.size();
    }

    private List<Epg_Program> getTestData(String channel){

        List<Epg_Program> testDataList = new ArrayList<>();
        boolean f = false;
        if (MainActivity.epg_programs == null)
            return null;
        for (int i = 0; i < MainActivity.epg_programs.size(); i++){
            Epg_Program program;
            if(channel.equalsIgnoreCase(MainActivity.epg_programs.get(i).channel)){
                program = MainActivity.epg_programs.get(i);
                testDataList.add(program);
                f = true;
            }
        }
        int n = testDataList.size();
        Epg_Program temp ;
        for(int i=0; i < n-1; i++){
            for(int j=i+1 ; j < n ; j++){
                if( testDataList.get(i).start.compareToIgnoreCase(testDataList.get(j).start) > 0) {
                    //swap elements
                    temp = testDataList.get(i);
                    testDataList.set(i, testDataList.get(j));
                    testDataList.set(j, temp);
                }
            }
        }
        return testDataList;
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        private RecyclerView rcvHorizontal;
        private RelativeLayout rlContainer;

        public ViewHolder(View itemView) {
            super(itemView);
            rlContainer = (RelativeLayout) itemView.findViewById(R.id.rlContainer);
            rcvHorizontal = (RecyclerView) itemView.findViewById(R.id.rcv_horizontal);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(
                    itemView.getContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false);
            rcvHorizontal.setLayoutManager(layoutManager);
        }
    }

    public void setOnItemClickListener(ProgramsRecyclerViewAdapter.OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
}
