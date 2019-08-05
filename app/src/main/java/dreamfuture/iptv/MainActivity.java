package dreamfuture.iptv;

import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import dreamfuture.iptv.adapters.CategoryAdapter;
import dreamfuture.iptv.adapters.ChannelAdapter;
import dreamfuture.iptv.adapters.SelfRemovingOnScrollListener;
import dreamfuture.iptv.adapters.TimeAdapter;
import dreamfuture.iptv.adapters.VerticalRecyclerViewAdapter;


public class MainActivity extends AppCompatActivity implements Player.EventListener {

    public boolean isCatChanged;
    public int width;
    public int height;
    private Handler mainHandler;

    private int counter;
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private DefaultTrackSelector trackSelector;
    private DataSource.Factory mediaDataSourceFactory;
    public Uri contentUri;
    public int contentPageIndex;
    public int contentPageSelected;
    public String contentCategory;
    public int contentCatScrollY;

    public boolean isFullscreen;
    public RecyclerView categoryRecyclerView;
    CategoryAdapter categoryAdapter;
    private RecyclerView.Adapter timeAdapter;
    private ChannelAdapter channelAdapter;
    private VerticalRecyclerViewAdapter scheduleAdapter;
    RelativeLayout categoryPan;
    LinearLayout mainPan;
    LinearLayout descriptionPan;
    LinearLayout channelPan;
    HorizontalScrollView schedulePan;

    public boolean isPlaying;
    public TextView txtAllChannels;
    public TextView txtTitle;
    public TextView txtDescription;
    public TextView txtDuration;
    public TextView txtStartTime;
    public ImageView img_ico;

    TextView txtDate_Time;
    RecyclerView channelRecyclerView;
    RecyclerView scheduleRecyclerView;
    RecyclerView timeRecyclerView;

    public SimpleExoPlayerView vvPreview;
    public SimpleExoPlayer player;

    public static String current_time;
    public static String current_timezone;
    public static String server_timezone;

    public static float screen_density;
    public String current_group;
    boolean flag_channelScroll, flag_programScroll;

    public boolean vod_flag;
    public int current_page_index;
    public int current_page_count;//9, 12
    private List<Epg_Channel> current_channels;
    private long mLastKeyDownTime = 0;
    private long mPressedDelta = 1000;
    private final RecyclerView.OnScrollListener channelScrollListener = new SelfRemovingOnScrollListener() {
        @Override
        public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, final int dy) {
            super.onScrolled(recyclerView, dx, dy);

//                Log.d("onScrolled Channels -", " Y = "+a);
            scheduleRecyclerView.scrollBy(0, dy);
//                scheduleRecyclerView.scrollTo(0, a);
//                scheduleRecyclerView.scrollToPosition(channelRecyclerView.getVerticalScrollbarPosition());

        }
    };

    private final RecyclerView.OnScrollListener programScrollListener = new SelfRemovingOnScrollListener() {

        @Override
        public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, final int dy) {
            super.onScrolled(recyclerView, dx, dy);

//                Log.d("onScrolled Programs", "- Y = "+dy);
//            channelRecyclerView.scrollBy(0, dy);
//                channelRecyclerView.scrollToPosition(scheduleRecyclerView.getVerticalScrollbarPosition());
//                channelRecyclerView.scrollTo(0,a);

        }
    };


    public static List<Epg_Channel>  epg_channels;
    public static List<Epg_Channel> vod_channels;
    public static List<Epg_Program> epg_programs;
    public String minT;
    public String maxT;
    private Date date;

    private TextView txtTvguide;
    private ImageView timeline;
    private RelativeLayout.LayoutParams timeline_params;

    private boolean isActivated_ChannelRecyclerView;

    private int current_category_selected;
    private int current_channel_selected;
    private Uri currentUri;

    private Handler timerHandler;
    private Runnable timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFullscreen = false;
        isPlaying = false;
        isCatChanged = false;
        counter = 0;
        View v = getLayoutInflater().inflate(R.layout.activity_main, null);
        v.setKeepScreenOn(true);
        setContentView(v);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mediaDataSourceFactory = buildDataSourceFactory(true);
        mainHandler = new Handler();
        current_group = "All Channels";
        contentCategory = "";
        contentCatScrollY = 0;
        current_page_count = 7;
        current_page_index = 0;
        current_category_selected = -1;
        current_channel_selected = -1;
        current_channels = new ArrayList<>();
//        current_channels = epg_channels;
        vod_flag = false;
        flag_channelScroll = flag_programScroll = false;
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
        screen_density = getResources().getDisplayMetrics().density;

        timeline = (ImageView) findViewById(R.id.timeline);
        timeline_params = (RelativeLayout.LayoutParams) timeline.getLayoutParams();
        txtDate_Time = (TextView)findViewById(R.id.txtDate_Time);
        img_ico = (ImageView) findViewById(R.id.img_ico);
        final Handler someHandler = new Handler(getMainLooper());
        someHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DateFormat d = DateFormat.getDateInstance();
                date =  new Date();
                SimpleDateFormat dates = new SimpleDateFormat("yyyyMMddHHmmss");

                //Setting dates
                try{
                    date = dates.parse(current_time.split(" ")[0]);
                    long tt = date.getTime();
                    tt += (long) 60000;
                    date.setTime(tt);
                    String strDate = date.toString();
                    txtDate_Time.setText(strDate.substring(0,16));
                    String y = strDate.split(" ")[5];
                    String mm = String.format("%02d", date.getMonth()+1);
                    String ddd = String.format("%02d", Integer.valueOf(strDate.split(" ")[2]));
                    String h = String.format("%02d", date.getHours());
                    String m = String.format("%02d", date.getMinutes());
                    String s = String.format("%02d", date.getSeconds());
                    current_time = y + mm + ddd + h + m + s +" "+ current_time.split(" ")[1];
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            float d = getdiff(current_time, convertTimeZone(minT));
                            int dis = (int)(d*(float) width/12.5f*2.0f);
                            timeline_params.leftMargin = dis;
                            timeline.setLayoutParams(timeline_params);
//                            schedulePan.scrollTo(dis, 0);
                            freeMemory();
                        }
                    });

                    if (channelAdapter.timeList!=null && channelAdapter.selected>-1) {
                        changeProgram();
                    }

                }catch (Exception e)
                {

                }
                someHandler.postDelayed(this, 60000);
            }
        }, 0);



        //////////////////////////////////////////////////////////////////////////
        vvPreview = (SimpleExoPlayerView) findViewById(R.id.video_view);
        vvPreview.setUseController(false);
        vvPreview.setPlayer(player);
        RelativeLayout rr = (RelativeLayout) findViewById(R.id.tapView);
        rr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (player!=null && channelAdapter.selected!=-1){

//                if (contentUri != null){
//                    releasePlayer();
//                    Intent toFullscreen = new Intent(MainActivity.this, VideoActivity.class);
//                    Bundle b = new Bundle();
//                    // Pass the url from the input to the player
//                    VideoActivity.vod_selected = vod_flag;
//                    b.putString("url", contentUri.toString());
//                    toFullscreen.putExtras(b); //Put your id to your next Intent
//                    startActivity(toFullscreen);
//                }
                if (isPlaying) {
                    if (isFullscreen)
                        go_to_previewscreen();
                    else
                        go_to_fullscreen();
                    isFullscreen = !isFullscreen;
                }
            }
        });
        //////////////////////////////////////////////////////////////////////////



        categoryRecyclerView = (RecyclerView)findViewById(R.id.categoryRecyclerView);
        categoryRecyclerView.setNestedScrollingEnabled(false);
        categoryAdapter = new CategoryAdapter(this, getCategoryList());
        categoryRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        channelRecyclerView = (RecyclerView)findViewById(R.id.rcv_channel_name);
        channelRecyclerView.setNestedScrollingEnabled(false);
        channelAdapter = new ChannelAdapter(null,this);
        final RecyclerView.LayoutManager channelLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,
                false);
        channelRecyclerView.setLayoutManager(channelLayoutManager);

        timeRecyclerView = (RecyclerView) findViewById(R.id.rcv_time);
        timeAdapter = new TimeAdapter(getTimeList(), MainActivity.this);
        RecyclerView.LayoutManager headerLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL,
                false);
        timeRecyclerView.setLayoutManager(headerLayoutManager);

        scheduleRecyclerView = (RecyclerView) findViewById(R.id.rcv_program);
        scheduleRecyclerView.setNestedScrollingEnabled(false);
//        scheduleAdapter = new VerticalRecyclerViewAdapter(getChannelList(), MainActivity.this);
        scheduleAdapter = new VerticalRecyclerViewAdapter(null, MainActivity.this);
        MyDisabledRecyclerView.LayoutManager programLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,
                false);
        scheduleRecyclerView.setLayoutManager(programLayoutManager);
        scheduleRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            //            @Override
//            public boolean onInterceptTouchEvent(MotionEvent e) {
//                return false;
//            }
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scheduleRecyclerView.requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });
        //Sync channel name RCV and Programs RCV scrolling

        channelRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {

            private int mLastY;

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

                final Boolean ret = rv.getScrollState() != RecyclerView.SCROLL_STATE_IDLE;
                if (!ret) {
                    onTouchEvent(rv, e);
                }
                return Boolean.FALSE;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
                final int action;
                if ((action = e.getAction()) == MotionEvent.ACTION_DOWN && scheduleRecyclerView
                        .getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                    mLastY = rv.getScrollY();
                    rv.addOnScrollListener(channelScrollListener);
                }
                else {
                    if (action == MotionEvent.ACTION_UP && rv.getScrollY() == mLastY) {
                        rv.removeOnScrollListener(channelScrollListener);
                    }
                }
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });

        scheduleRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {

            private int mLastY;

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                return true;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });
        ///////////////////////////////////////////

        LinearLayout.LayoutParams param = (LinearLayout.LayoutParams)scheduleRecyclerView.getLayoutParams();
        scheduleRecyclerView.setLayoutParams(param);

        current_channels = getChannelList();


        LinearLayout.LayoutParams param1 = (LinearLayout.LayoutParams)findViewById(R.id.channelPan).getLayoutParams();
        param1.width = (int)(width*110.0f/572.0f);
        findViewById(R.id.channelPan).setLayoutParams(param1);

        categoryPan=(RelativeLayout)findViewById(R.id.categoryPan);
        mainPan=(LinearLayout)findViewById(R.id.mainPan);
        descriptionPan=(LinearLayout)findViewById(R.id.descriptionPan);
        schedulePan = (HorizontalScrollView)findViewById(R.id.schedulePan);
        schedulePan.setFocusable(true);
        schedulePan.setFocusableInTouchMode(true);
        schedulePan.requestFocus();
        refreshAdapter();
        channelPan = (LinearLayout)findViewById(R.id.channelPan);
        txtAllChannels = (TextView)findViewById(R.id.txtAllChannels);

        txtAllChannels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allchannelsClick();
            }
        });

        txtTvguide = (TextView) findViewById(R.id.tv_tvguide);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtDescription = (TextView) findViewById(R.id.txtDescription);
        txtDuration = (TextView) findViewById(R.id.txtDuration);
        txtStartTime = (TextView) findViewById(R.id.txtStartTime);
        init();

        timeRecyclerView.setAdapter(timeAdapter);

        final Handler handlerMain = new Handler();
        handlerMain.postDelayed(new Runnable() {
            @Override
            public void run() {
                categoryRecyclerView.setAdapter(categoryAdapter);
                channelRecyclerView.setAdapter(channelAdapter);
                channelAdapter.selected = 0;
//                channelItemSelect(0);
                channelAdapter.notifyDataSetChanged();
//                channelItemClick(0);
                scheduleRecyclerView.setAdapter(scheduleAdapter);
                float d = getdiff(current_time, convertTimeZone(minT));
                int dis = (int)(d*(float) width/12.5f*2.0f);
                timeline_params.leftMargin = dis;
                timeline.setLayoutParams(timeline_params);
                dis = (int)((d-1.0f)*(float) width/12.5f*2.0f);
                schedulePan.scrollTo(dis, 0);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                float d = getdiff(current_time, convertTimeZone(minT));
                                int dis = (int)(d*(float) width/12.5f*2.0f);
                                timeline_params.leftMargin = dis;
                                timeline.setLayoutParams(timeline_params);
                                Log.d("Memory usage size", (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())+"");
                                freeMemory();
                            }
                        });
                    }
                }, 15000);
//                schedulePan.setFocusable(true);
//                schedulePan.setFocusableInTouchMode(true);
//                schedulePan.requestFocus();
            }
        }, 50);

        channelRecyclerView.setOnTouchListener(new OnSwipeTouchListener(){
            public boolean onSwipeTop() {
                if (current_channels.size()%current_page_count == 0){
                    if (current_page_index <current_channels.size()/current_page_count-1){
                        current_page_index++;
                        refreshAdapter();
                        isCatChanged = true;
                    }
                }
                else{
                    if (current_page_index <current_channels.size()/current_page_count){
                        current_page_index++;
                        refreshAdapter();
                        isCatChanged = true;
                    }
                }

                return true;
            }
            public boolean onSwipeRight() {
//                Toast.makeText(MainActivity.this, "right", Toast.LENGTH_SHORT).show();
                return true;
            }
            public boolean onSwipeLeft() {
//                Toast.makeText(MainActivity.this, "left", Toast.LENGTH_SHORT).show();
                return true;
            }
            public boolean onSwipeBottom() {
                if (current_page_index !=0){
                    current_page_index--;
                    refreshAdapter();
                    isCatChanged = true;
                }
                return true;
            }
        });
//        schedulePan.setFocusable(true);
//        schedulePan.setFocusableInTouchMode(true);
//        schedulePan.requestFocus();
    }
    public void changeProgram(){
        Epg_Channel c = channelAdapter.timeList.get(channelAdapter.selected);
        if (epg_programs != null) {
            for (int i = 0; i < epg_programs.size(); i++) {
                if (c.channel_id.equalsIgnoreCase(epg_programs.get(i).channel)) {
                    long a = Long.valueOf(current_time.split(" ")[0]);
                    long start = Long.valueOf(convertTimeZone(epg_programs.get(i).start.split(" ")[0]));
                    long end = Long.valueOf(convertTimeZone(epg_programs.get(i).stop.split(" ")[0]));
                    if (a >= start && a < end) {
//                                try {
//                                    Picasso.with(parent).load(c.channel_logo_url).into(parent.img_ico);
//                                }catch (Exception e){}
                        txtTitle.setText(epg_programs.get(i).title);
                        txtDescription.setText(epg_programs.get(i).description);
                        float dif = getdiff(epg_programs.get(i).start.split(" ")[0], epg_programs.get(i).stop.split(" ")[0]);
                        String strDuration = "";
                        if (dif >= 1.0f) {
                            if (dif - (float) ((int) dif) == 0.0f) {
                                strDuration = (int) dif + "h";
                            } else {
                                strDuration = (int) dif + "h " + (int) ((dif - (float) ((int) dif)) * 60) + "mins";
                            }
                        } else {
                            strDuration = (int) ((dif - (float) ((int) dif)) * 60) + "mins";
                        }
                        txtDuration.setText(strDuration);
                        String startAtTime = epg_programs.get(i).start.split(" ")[0];
                        startAtTime = convertTimeZone(startAtTime);
                        txtStartTime.setText("Started at " + startAtTime.substring(8, 10) + ":" + startAtTime.substring(10, 12));
                        break;
                    }
                }
            }
        }
    }
    public void init(){
        ////////////////////////////////////////////////////////////
        isActivated_ChannelRecyclerView = true;
        //////////////////////Font setting///////////////////////////
        txtTvguide.setTextSize(height*54/731/screen_density);
        txtAllChannels.setTextSize(height*30/731/screen_density);
        txtTitle.setTextSize(height*32/731/screen_density);
        txtDescription.setTextSize(height*20/731/screen_density);
        txtDuration.setTextSize(height*20/731/screen_density);
        txtStartTime.setTextSize(height*20/731/screen_density);
        txtDate_Time.setTextSize(height*30/731/screen_density);
        TextView tv_change_date = (TextView) findViewById(R.id.tv_change_date);
        tv_change_date.setTextSize(height*20/731/screen_density);
        /////////////////////////////////////////////////////////////
        LinearLayout.LayoutParams param_Categorypan = (LinearLayout.LayoutParams) categoryPan.getLayoutParams();
        param_Categorypan.width = 0;
        categoryPan.setLayoutParams(param_Categorypan);

        LinearLayout.LayoutParams param_Channelpan = (LinearLayout.LayoutParams) channelPan.getLayoutParams();
        param_Channelpan.width = width*420/1673;
        channelPan.setLayoutParams(param_Channelpan);

        LinearLayout.LayoutParams param_Descriptionpan = (LinearLayout.LayoutParams) descriptionPan.getLayoutParams();
        param_Descriptionpan.height = height*86/348;
        descriptionPan.setLayoutParams(param_Descriptionpan);

        LinearLayout.LayoutParams param_Imageview = (LinearLayout.LayoutParams) img_ico.getLayoutParams();
        param_Imageview.width = height/20;
        param_Imageview.height = height/20;
        img_ico.setLayoutParams(param_Imageview);
    }

    public void go_to_fullscreen(){
        RelativeLayout view_pan = (RelativeLayout) findViewById(R.id.view_pan);
        LinearLayout.LayoutParams params_view_pan = (LinearLayout.LayoutParams) view_pan.getLayoutParams();
        params_view_pan.weight = 572;
        view_pan.setLayoutParams(params_view_pan);

        RelativeLayout guide_view = (RelativeLayout) findViewById(R.id.guide_view);
        LinearLayout.LayoutParams params_guide_view = (LinearLayout.LayoutParams) guide_view.getLayoutParams();
        params_guide_view.weight = 0;
        guide_view.setLayoutParams(params_guide_view);

        RelativeLayout allchannel_view = (RelativeLayout) findViewById(R.id.allchannel_view);
        LinearLayout.LayoutParams params_allchannel_view = (LinearLayout.LayoutParams) allchannel_view.getLayoutParams();
        params_allchannel_view.weight = 0;
        allchannel_view.setLayoutParams(params_allchannel_view);

        RelativeLayout tap_view = (RelativeLayout) findViewById(R.id.tapView);
        LinearLayout.LayoutParams params_tap_view = (LinearLayout.LayoutParams) tap_view.getLayoutParams();
        params_tap_view.weight = 352;
        tap_view.setLayoutParams(params_tap_view);

        RelativeLayout empty_view = (RelativeLayout) findViewById(R.id.empty_view);
        LinearLayout.LayoutParams params_empty_view = (LinearLayout.LayoutParams) empty_view.getLayoutParams();
        params_empty_view.weight = 0;
        empty_view.setLayoutParams(params_empty_view);

        RelativeLayout date_time_view = (RelativeLayout) findViewById(R.id.date_time_view);
        LinearLayout.LayoutParams params_date_time_view = (LinearLayout.LayoutParams) date_time_view.getLayoutParams();
        params_date_time_view.weight = 0;
        empty_view.setLayoutParams(params_date_time_view);

        ImageView ivDivider = (ImageView) findViewById(R.id.ivDivider);
        RelativeLayout.LayoutParams params_ivDivider = (RelativeLayout.LayoutParams) ivDivider.getLayoutParams();
        params_ivDivider.width = 0;
        ivDivider.setLayoutParams(params_ivDivider);

/////////////////////////////////////////////////////////////////////////////////////////////
        LinearLayout main_pan = (LinearLayout) findViewById(R.id.main_pan);
        LinearLayout.LayoutParams params_main_pan = (LinearLayout.LayoutParams) main_pan.getLayoutParams();
        params_main_pan.weight = 0;
        main_pan.setLayoutParams(params_main_pan);

        ViewTreeObserver vto = findViewById(R.id.tapView).getViewTreeObserver();
        vto.addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int width  = findViewById(R.id.tapView).getMeasuredWidth();
                int height = findViewById(R.id.tapView).getMeasuredHeight();

                RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)vvPreview.getLayoutParams();
                param.width = width;
                param.height = height;
                vvPreview.setLayoutParams(param);

                findViewById(R.id.tapView).getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    public void go_to_previewscreen(){
        RelativeLayout view_pan = (RelativeLayout) findViewById(R.id.view_pan);
        LinearLayout.LayoutParams params_view_pan = (LinearLayout.LayoutParams) view_pan.getLayoutParams();
        params_view_pan.weight = 166;
        view_pan.setLayoutParams(params_view_pan);

        RelativeLayout guide_view = (RelativeLayout) findViewById(R.id.guide_view);
        LinearLayout.LayoutParams params_guide_view = (LinearLayout.LayoutParams) guide_view.getLayoutParams();
        params_guide_view.weight = 103;
        guide_view.setLayoutParams(params_guide_view);

        RelativeLayout allchannel_view = (RelativeLayout) findViewById(R.id.allchannel_view);
        LinearLayout.LayoutParams params_allchannel_view = (LinearLayout.LayoutParams) allchannel_view.getLayoutParams();
        params_allchannel_view.weight = 22;
        allchannel_view.setLayoutParams(params_allchannel_view);

        RelativeLayout tap_view = (RelativeLayout) findViewById(R.id.tapView);
        LinearLayout.LayoutParams params_tap_view = (LinearLayout.LayoutParams) tap_view.getLayoutParams();
        params_tap_view.weight = 90;
        tap_view.setLayoutParams(params_tap_view);

        RelativeLayout empty_view = (RelativeLayout) findViewById(R.id.empty_view);
        LinearLayout.LayoutParams params_empty_view = (LinearLayout.LayoutParams) empty_view.getLayoutParams();
        params_empty_view.weight = 31;
        empty_view.setLayoutParams(params_empty_view);

        RelativeLayout date_time_view = (RelativeLayout) findViewById(R.id.date_time_view);
        LinearLayout.LayoutParams params_date_time_view = (LinearLayout.LayoutParams) date_time_view.getLayoutParams();
        params_date_time_view.weight = 27;
        empty_view.setLayoutParams(params_date_time_view);

        ImageView ivDivider = (ImageView) findViewById(R.id.ivDivider);
        RelativeLayout.LayoutParams params_ivDivider = (RelativeLayout.LayoutParams) ivDivider.getLayoutParams();
        params_ivDivider.width = (int)(20.0*screen_density);
        ivDivider.setLayoutParams(params_ivDivider);


/////////////////////////////////////////////////////////////////////////////////////////////
        LinearLayout main_pan = (LinearLayout) findViewById(R.id.main_pan);
        LinearLayout.LayoutParams params_main_pan = (LinearLayout.LayoutParams) main_pan.getLayoutParams();
        params_main_pan.weight = 402;
        main_pan.setLayoutParams(params_main_pan);

        ViewTreeObserver vto = findViewById(R.id.tapView).getViewTreeObserver();
        vto.addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int width  = findViewById(R.id.tapView).getMeasuredWidth();
                int height = findViewById(R.id.tapView).getMeasuredHeight();

                RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)vvPreview.getLayoutParams();
                param.width = width;
                param.height = height;
                vvPreview.setLayoutParams(param);

                findViewById(R.id.tapView).getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void allchannelsClick(){
        vod_flag = false;
        isActivated_ChannelRecyclerView = false;
        categoryAdapter.categories = getCategoryList();
        categoryAdapter.notifyDataSetChanged();
        LinearLayout.LayoutParams param_Categorypan = (LinearLayout.LayoutParams) categoryPan.getLayoutParams();
        param_Categorypan.width = (int)(width*110.0f/572.0f);
        categoryPan.setLayoutParams(param_Categorypan);

        LinearLayout.LayoutParams param_Descriptionpan = (LinearLayout.LayoutParams) descriptionPan.getLayoutParams();
        param_Descriptionpan.height = 0;
        descriptionPan.setLayoutParams(param_Descriptionpan);
    }

    public float getdiff (String a, String b){
        float dis=0.0f;
        try {
            //Dates to compare
            Date date1;
            Date date2;

            SimpleDateFormat dates = new SimpleDateFormat("yyyyMMddHHmmss");

            //Setting dates
            date1 = dates.parse(a);
            date2 = dates.parse(b);

            //Comparing dates
            long difference = Math.abs(date1.getTime() - date2.getTime());
            dis =(float)difference / (60.0f * 60.0f * 1000.0f);

        } catch (Exception exception) {
            Log.e("DIDN'T WORK", "exception " + exception);
        }
        return dis;
    }
    public static String convertTimeZone(String str_time){
        SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyyMMddHHmmss");
//        String a = epg_programs.get(0).start.split(" ")[1];
        if (server_timezone == null)
            return str_time;
        String t = "GMT"+server_timezone.substring(0,3)+":"+server_timezone.substring(3);
        sourceFormat.setTimeZone(TimeZone.getTimeZone(t));
        try {
            Date parsed = sourceFormat.parse(str_time); // => Date is in UTC now

            TimeZone tz = TimeZone.getTimeZone(current_timezone);
            SimpleDateFormat destFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            destFormat.setTimeZone(tz);

            String result = destFormat.format(parsed);
            return result;
        }catch (Exception e){
            return "";
        }
    }
    private List<String> getTimeList(){
        /////////////////////detect min, max in program//////////////
        if (epg_programs == null )
            return null;
        if (epg_programs.size() == 0)
            return  null;
        minT=epg_programs.get(0).start.split(" ")[0];
        maxT=epg_programs.get(0).stop.split(" ")[0];
        for (int i = 1;  i< epg_programs.size(); i++){
            if(Double.valueOf(minT)>Double.valueOf(epg_programs.get(i).start.split(" ")[0]))
                minT=epg_programs.get(i).start.split(" ")[0];
            if(Double.valueOf(maxT)<Double.valueOf(epg_programs.get(i).stop.split(" ")[0]))
                maxT=epg_programs.get(i).stop.split(" ")[0];
        }
        List<String> timeList = new ArrayList<>();

        int h = Integer.valueOf(minT.substring(8,10));
        int m = Integer.valueOf(minT.substring(10,12));
        if(m<30)
            minT = minT.substring(0,10)+"0000";
        else
            minT=minT.substring(0,10)+"3000";

        h = Integer.valueOf(maxT.substring(8,10));
        m = Integer.valueOf(maxT.substring(10,12));
        if(m<30)
            maxT = maxT.substring(0,10)+"3000";
        else {
            if(h+1<10)
                maxT = maxT.substring(0, 8) + "0" + (h + 1) + "0000";
            else
                maxT = maxT.substring(0, 8) + (h + 1) + "0000";
        }
        String convertedminT = convertTimeZone(minT);
        String convertedmaxT = convertTimeZone(maxT);

        h = Integer.valueOf(convertedminT.substring(8,10));
        m = Integer.valueOf(convertedminT.substring(10,12));
        int deltas = (int)(getdiff( convertedminT, convertedmaxT)*2);
        int start = 0;
        if (m == 30){
            timeList.add((h)%24+":30");
            deltas--;
            start = 2;
        }
        for (int i=start; i<=deltas; i=i+2){
            timeList.add((h+i/2)%24+":00");
            timeList.add((h+i/2)%24+":30");
        }
        if(deltas%2==1){
            h = Integer.valueOf(convertedmaxT.substring(8,10));
            timeList.add((h)+":00");
        }
        return timeList;
    }
    private List<Epg_Channel> getChannelList(){
        List<Epg_Channel> channelList = new ArrayList<>();
        String filter;
        if (vod_flag){
            if(current_group.equals("Video on demand"))
                channelList.addAll(vod_channels);
            else {
                filter = current_group;
                for (int i = 0; i < vod_channels.size(); i++) {
                    if (vod_channels.get(i).group_title.equals(filter)) {
                        channelList.add(vod_channels.get(i));
                    }
                }
            }
        }
        else{
            if(current_group.equals("All Channels"))
                channelList = epg_channels;
            else {
                filter = current_group;
                for (int i = 0; i < epg_channels.size(); i++) {
                    if (epg_channels.get(i).group_title.equals(filter)) {
                        channelList.add(epg_channels.get(i));
                    }
                }
            }
        }

        return channelList;
    }
    public List<String> getCategoryList(){
        List<String> categoryList = new ArrayList<>();
        if (vod_flag){
            for (int i = 0; i < vod_channels.size(); i++){
                String gt = vod_channels.get(i).group_title;
                if (gt.equals(""))
                    continue;
                boolean flag = true;
                for (int k = 0; k < categoryList.size(); k++) {
                    if (categoryList.get(k).equals(gt)) {
                        flag = false;
                        break;
                    }
                }
                if (flag)
                    categoryList.add(gt);
            }
        }
        else {
            categoryList.add("All Channels");
            categoryList.add("Video on demand");
            for (int i = 0; i < epg_channels.size(); i++) {
                String gt = epg_channels.get(i).group_title;
                if (gt.equals(""))
                    continue;
                boolean flag = true;
                for (int k = 0; k < categoryList.size(); k++) {
                    if (categoryList.get(k).equals(gt)) {
                        flag = false;
                        break;
                    }
                }
                if (flag)
                    categoryList.add(gt);
            }
        }
        return categoryList;
    }

    public void refreshChannels(){
        current_page_index = 0;
        current_channels = getChannelList();
        refreshAdapter();
//        schedulePan.setFocusable(true);
//        schedulePan.setFocusableInTouchMode(true);
//        schedulePan.requestFocus();
    }

    public void refreshAdapter(){
        int y = current_channels.size();
        List<Epg_Channel> c = new ArrayList<>();
        if (y - current_page_count*current_page_index <= current_page_count){
            for (int i = current_page_count*current_page_index; i < y; i++){
                c.add(current_channels.get(i));
            }
        }
        else {
            for (int i = current_page_count*current_page_index; i < current_page_count*(current_page_index+1); i++){
                c.add(current_channels.get(i));
            }
        }
        channelAdapter.timeList = c;
        channelAdapter.selected = -1;
        scheduleAdapter.channelList = c;

        categoryAdapter.notifyDataSetChanged();
        channelAdapter.notifyDataSetChanged();
        channelRecyclerView.scrollTo(0,0);
        channelRecyclerView.scrollBy(0,0);
        channelRecyclerView.scrollToPosition(0);
        scheduleAdapter.notifyDataSetChanged();
        scheduleRecyclerView.scrollTo(0,0);
        scheduleRecyclerView.scrollBy(0,0);
        scheduleRecyclerView.scrollToPosition(0);
        freeMemory();
//        schedulePan.setFocusable(true);
//        schedulePan.setFocusableInTouchMode(true);
//        schedulePan.requestFocus();
    }


    public static void freeMemory(){
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event){
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
//            case KeyEvent.KEYCODE_VOLUME_UP:
                timerHandler.removeCallbacks(timerRunnable);

                if (isFullscreen){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            go_to_previewscreen();
                            isFullscreen = false;
                        }
                    });
                } else{
                    if (contentUri != null){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                returnEpg();
                                isCatChanged = false;
                                channelItemClick(channelAdapter.selected);
//                                go_to_fullscreen();
//                                isFullscreen = true;
                            }
                        });
                    }
                }
                break;
            case KeyEvent.KEYCODE_BUTTON_B:
                timerHandler.removeCallbacks(timerRunnable);

                if (isFullscreen){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            go_to_previewscreen();
                            isFullscreen = false;
                        }
                    });
                } else{
                    if (contentUri != null){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                returnEpg();
                                channelItemClick(channelAdapter.selected);
//                                go_to_fullscreen();
//                                isFullscreen = true;
                            }
                        });
                    }
                }
                break;
        }
        return true;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                if (!isFullscreen) {
                    if (isActivated_ChannelRecyclerView) {
//                        Toast.makeText(this, "Up button test in channel", Toast.LENGTH_LONG).show();
                        if (channelAdapter.selected == 0) {
                            if (current_page_index > 0) {
                                current_page_index--;
                                current_channel_selected = -1;
                                refreshAdapter();
                                channelAdapter.selected = current_page_count - 1;
                                channelAdapter.notifyDataSetChanged();
                                isCatChanged = true;
                            }
                        } else {
                            channelAdapter.selected--;
                            channelAdapter.notifyDataSetChanged();
                        }
                    } else {
//                        Toast.makeText(this, "Up button test in category", Toast.LENGTH_LONG).show();
                        if (categoryAdapter.selected > 0) {
                            categoryAdapter.selected--;
                            categoryAdapter.notifyDataSetChanged();

                            if (categoryAdapter.categories.size() - categoryAdapter.selected > current_page_count - 1)
                                categoryRecyclerView.scrollBy(0, -height / 10);
                        }
                    }
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (!isFullscreen) {
                    if (isActivated_ChannelRecyclerView) {
                        int y = current_channels.size();
                        if (y - current_page_index * current_page_count > current_page_count) {
                            if (channelAdapter.selected == current_page_count - 1) {
                                current_page_index++;
                                current_channel_selected = -1;
                                refreshAdapter();
                                channelAdapter.selected = 0;
                                channelAdapter.notifyDataSetChanged();
                                isCatChanged = true;
                            } else {
                                channelAdapter.selected++;
                                channelAdapter.notifyDataSetChanged();
                            }
                        } else {
                            if (channelAdapter.selected < y - current_page_index * current_page_count - 1) {
                                channelAdapter.selected++;
                                channelAdapter.notifyDataSetChanged();
                            }
                        }
//                    channelItemSelect(channelAdapter.selected);
                    } else {
                        if (categoryAdapter.selected < getCategoryList().size() - 1) {
                            categoryAdapter.selected++;
                            categoryAdapter.notifyDataSetChanged();
                            if (categoryAdapter.selected > 7) {
                                categoryRecyclerView.scrollBy(0, height / 10);
                            }
                        }
                    }
                    Log.d("pad", "down");
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                Log.d("pad", "right");
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                Log.d("pad", "left");
                break;
            case KeyEvent.KEYCODE_BACK:
                timerHandler = new Handler();
                timerRunnable = new Runnable() {
                    @Override
                    public void run() {
                        releasePlayer();
                        finish();
                        int pid = android.os.Process.myPid();
                        android.os.Process.killProcess(pid);
                    }
                };
                timerHandler.postDelayed(timerRunnable, 3000);
                break;
            case KeyEvent.KEYCODE_BUTTON_B:
                timerHandler = new Handler();
                timerRunnable = new Runnable() {
                    @Override
                    public void run() {
                        releasePlayer();
                        finish();
                        int pid = android.os.Process.myPid();
                        android.os.Process.killProcess(pid);
                    }
                };
                timerHandler.postDelayed(timerRunnable, 3000);
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (!isFullscreen) {
                    Log.d("onkey", "key center!");
                    if (isActivated_ChannelRecyclerView ) {
                        if (channelAdapter.selected >= 0) {
                            contentPageIndex = current_page_index;
                            contentPageSelected = channelAdapter.selected;
                            contentCategory = current_group;
                            contentCatScrollY = categoryRecyclerView.getScrollY();
                            channelItemClick(channelAdapter.selected);
                        }
                    } else
                        categoryItemClick(categoryAdapter.selected);
                }
                break;
            case KeyEvent.KEYCODE_MENU:
                if (!isFullscreen) {
                    if (!isActivated_ChannelRecyclerView) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                init();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                allchannelsClick();
                            }
                        });
                    }
                }
                break;
            case KeyEvent.KEYCODE_2:
                if (!isFullscreen) {
                    if (!isActivated_ChannelRecyclerView) {
                        init();
                    } else {
                        allchannelsClick();
                    }
                }
                break;
            case KeyEvent.KEYCODE_1:
                if (!isFullscreen) {
                    if (current_page_index > 0) {
                        current_page_index--;
                        refreshAdapter();
                    }
                }
                break;
//            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                if (!isFullscreen) {
                    if (current_page_index > 0) {
                        current_page_index--;
                        refreshAdapter();
                        isCatChanged = true;
                    }
                }
                break;
            case KeyEvent.KEYCODE_3:
                if (!isFullscreen) {
                    if (current_channels.size() % current_page_count == 0) {
                        if (current_page_index < current_channels.size() / current_page_count - 1) {
                            current_page_index++;
                            refreshAdapter();
                        }
                    } else {
                        if (current_page_index < current_channels.size() / current_page_count) {
                            current_page_index++;
                            refreshAdapter();
                        }
                    }
                }
                break;
//            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                if (!isFullscreen) {
                    if (current_channels.size() % current_page_count == 0) {
                        if (current_page_index < current_channels.size() / current_page_count - 1) {
                            current_page_index++;
                            refreshAdapter();
                            isCatChanged = true;
                        }
                    } else {
                        if (current_page_index < current_channels.size() / current_page_count) {
                            current_page_index++;
                            refreshAdapter();
                            isCatChanged = true;
                        }
                    }
                }
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (channelAdapter.selected < 0)
                    break;
                contentPageIndex = current_page_index;
                contentPageSelected = channelAdapter.selected;
                contentCategory = current_group;
                contentCatScrollY = categoryRecyclerView.getScrollY();
                if (isFullscreen){
                    if (vod_flag){
                        if (isPlaying)
                            Toast.makeText(this, "Pause", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(this, "Play", Toast.LENGTH_SHORT).show();
                        player.setPlayWhenReady(!isPlaying);
                        isPlaying = !isPlaying;
                    }
                }
                else {
                    channelItemClick(channelAdapter.selected);
                    go_to_fullscreen();
                    isFullscreen = true;
                }
                break;
        }
        return true;
//        return super.onKeyDown(keyCode, event);
    }
    private void categoryItemClick(int position){
        List<String> categories = new ArrayList<>();
        categories = getCategoryList();
        if(current_category_selected == position){
            init();
        }
        else {
            isCatChanged = true;
            current_group = categories.get(position);
            txtAllChannels.setText(current_group);
            if (categories.get(position).equals("Video on demand")){
                vod_flag = true;
                categoryAdapter.selected = -1;
                categories = getCategoryList();
                categoryAdapter.categories = categories;
            }
            else
                current_category_selected = position;
            refreshChannels();
            categoryAdapter.notifyDataSetChanged();
        }
    }
    private void channelItemClick(int position){
        if (position < 0 )
            return;
        if (position >= channelAdapter.timeList.size())
            return;
        if (isCatChanged){
            isCatChanged = false;
            List<Epg_Channel> timeList;
            timeList = channelAdapter.timeList;
            Epg_Channel c = timeList.get(position);
            if (epg_programs != null) {
                for (int i = 0; i < epg_programs.size(); i++) {
                    if (c.channel_id.equalsIgnoreCase(epg_programs.get(i).channel)) {
                        long a = Long.valueOf(current_time.split(" ")[0]);
                        long start = Long.valueOf(convertTimeZone(epg_programs.get(i).start.split(" ")[0]));
                        long end = Long.valueOf(convertTimeZone(epg_programs.get(i).stop.split(" ")[0]));
                        if (a >= start && a < end) {
//                        try {
////                            Picasso.with(this).load(c.channel_logo_url).into(img_ico);
//                        }catch (Exception e){
//                            Toast.makeText(MainActivity.this, c.channel_logo_url, Toast.LENGTH_LONG).show();
//                        }
                            txtTitle.setText(epg_programs.get(i).title);
                            txtDescription.setText(epg_programs.get(i).description);
                            float d = getdiff(epg_programs.get(i).start.split(" ")[0], epg_programs.get(i).stop.split(" ")[0]);
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
                            txtDuration.setText(strDuration);
                            String startAtTime = epg_programs.get(i).start.split(" ")[0];
                            startAtTime = convertTimeZone(startAtTime);
                            txtStartTime.setText("Started at " + startAtTime.substring(8, 10) + ":" + startAtTime.substring(10, 12));
                            break;
                        }
                    }
                }
            }
            //////////////////////////////////////////////////////
            current_channel_selected = position;
            String path = timeList.get(position).channel_url;
            contentUri = Uri.parse(path);
            releasePlayer();
            initializePlayer();
        }
        else {
            if (position == current_channel_selected && isPlaying) {
//            releasePlayer();
//            Intent toFullscreen = new Intent(MainActivity.this, VideoActivity.class);
//            Bundle b = new Bundle();
//            // Pass the url from the input to the player
//            VideoActivity.vod_selected = vod_flag;
//            b.putString("url", channelAdapter.timeList.get(position).channel_url);
//            toFullscreen.putExtras(b); //Put your id to your next Intent
//            startActivity(toFullscreen);
                go_to_fullscreen();
                isFullscreen = true;
            } else {
                List<Epg_Channel> timeList;
                timeList = channelAdapter.timeList;
                Epg_Channel c = timeList.get(position);
                if (epg_programs != null) {
                    for (int i = 0; i < epg_programs.size(); i++) {
                        if (c.channel_id.equalsIgnoreCase(epg_programs.get(i).channel)) {
                            long a = Long.valueOf(current_time.split(" ")[0]);
                            long start = Long.valueOf(convertTimeZone(epg_programs.get(i).start.split(" ")[0]));
                            long end = Long.valueOf(convertTimeZone(epg_programs.get(i).stop.split(" ")[0]));
                            if (a >= start && a < end) {
//                        try {
////                            Picasso.with(this).load(c.channel_logo_url).into(img_ico);
//                        }catch (Exception e){
//                            Toast.makeText(MainActivity.this, c.channel_logo_url, Toast.LENGTH_LONG).show();
//                        }
                                txtTitle.setText(epg_programs.get(i).title);
                                txtDescription.setText(epg_programs.get(i).description);
                                float d = getdiff(epg_programs.get(i).start.split(" ")[0], epg_programs.get(i).stop.split(" ")[0]);
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
                                txtDuration.setText(strDuration);
                                String startAtTime = epg_programs.get(i).start.split(" ")[0];
                                startAtTime = convertTimeZone(startAtTime);
                                txtStartTime.setText("Started at " + startAtTime.substring(8, 10) + ":" + startAtTime.substring(10, 12));
                                break;
                            }
                        }
                    }
                }
                //////////////////////////////////////////////////////
                current_channel_selected = position;
                String path = timeList.get(position).channel_url;
                contentUri = Uri.parse(path);
                releasePlayer();
                initializePlayer();
            }
        }
    }
    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
    public void initializePlayer() {
        Intent intent = getIntent();
        boolean needNewPlayer = player == null;
        if (needNewPlayer) {
            TrackSelection.Factory adaptiveTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
            trackSelector = new DefaultTrackSelector(adaptiveTrackSelectionFactory);
            boolean preferExtensionDecoders = intent.getBooleanExtra("prefer_extension_decoders", false);
            @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode =
                    ((DemoApplication) getApplication()).useExtensionRenderers()
                            ? (preferExtensionDecoders ? DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                            : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                            : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
            DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this,
                    null, extensionRendererMode);

            player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);
            vvPreview.setPlayer(player);
            player.setPlayWhenReady(true);
        }
        MediaSource mediaSource = new ExtractorMediaSource(contentUri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                null, null);
        player.prepare(mediaSource, true, false);
        isPlaying = true;
    }
    public void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
            trackSelector = null;
            isPlaying = false;
        }
    }
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return ((DemoApplication) getApplication())
                .buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    private void returnEpg() {
        if (!current_group.equals(contentCategory)){
            current_group = contentCategory;
            txtAllChannels.setText(contentCategory);
            refreshChannels();
            current_page_index = contentPageIndex;
            refreshAdapter();
        }
        else {
            if (current_page_index != contentPageIndex) {
                current_page_index = contentPageIndex;
                refreshAdapter();
            }
        }
        channelAdapter.selected = contentPageSelected;
        channelAdapter.notifyDataSetChanged();
        txtAllChannels.setText(contentCategory);
        int i = getCategoryList().indexOf(contentCategory);
        categoryAdapter.selected = i;
        categoryAdapter.notifyDataSetChanged();
        Log.e("test", ""+ contentCatScrollY);
//        categoryRecyclerView.scrollTo(0, contentCatScrollY);
        categoryRecyclerView.scrollToPosition(i);
    }
    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        Log.e("stateChange", ""+ playbackState);
        if (playWhenReady && playbackState == Player.STATE_BUFFERING){
            releasePlayer();
            initializePlayer();
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        if (isBehindLiveWindow(error)) {
            initializePlayer();
        } else {
        }
    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }
}
