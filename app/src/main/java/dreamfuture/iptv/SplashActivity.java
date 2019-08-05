package dreamfuture.iptv;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import java_cup.Main;

import static dreamfuture.iptv.MainActivity.epg_programs;
import static dreamfuture.iptv.MainActivity.freeMemory;


public class SplashActivity extends AppCompatActivity {
    ImageView ivCenter;
    ImageView ivParticle;
    TextView ivText;

    Timer timer = new Timer();

    int count = 0;
    int cc=0;
    boolean flowDirection=true;

    float angle = 0;


    public static String httpURL;
    public static String EpgURL;

    RequestQueue requestQueue;

    private Channel_Cell[] channels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        ivCenter = (ImageView)findViewById(R.id.ivCenter);
        ivParticle = (ImageView)findViewById(R.id.ivParticle);
        ivText = (TextView)findViewById(R.id.ivText);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        float density = getResources().getDisplayMetrics().density;

        RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)ivCenter.getLayoutParams();
        param.width = (int)(width*0.25);
        param.height = (int)(param.width*1275.0f/1326.0f);
        ivCenter.setLayoutParams(param);

        RelativeLayout.LayoutParams textparam = (RelativeLayout.LayoutParams)ivText.getLayoutParams();
        textparam.width = (int)(width*0.3);
        textparam.topMargin=(int)(textparam.width*0.05);
        ivText.setTextSize(textparam.width/6/density);
        ivText.setLayoutParams(textparam);

        RelativeLayout.LayoutParams particleparam = (RelativeLayout.LayoutParams)ivParticle.getLayoutParams();
        particleparam.width = (int)(param.width * 0.6f);
        particleparam.height = particleparam.width*434/487;
        particleparam.rightMargin=(int)(param.width*(1-Math.sqrt(2))/4)+particleparam.width/7;
        particleparam.bottomMargin=particleparam.rightMargin;
        ivParticle.setLayoutParams(particleparam);


        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                rotateImage(angle);
                angle = (float) (angle+0.6)%360;
                if(count==0)
                    flowDirection=true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ivParticle.setImageResource(R.drawable.splash_anim_00001+count);
                    }
                });
                if(count==16)
                    flowDirection=false;
                if(flowDirection)
                    count++;
                else
                    count--;
                cc++;


            }

        }, 0, 20);
        LayoutInflater factory = LayoutInflater.from(this);

//text_entry is an Layout XML file containing two text field to display in alert dialog
        final View textEntryView = factory.inflate(R.layout.url_select, null);

        final EditText input1 = (EditText) textEntryView.findViewById(R.id.et_playlist);
        final EditText input2 = (EditText) textEntryView.findViewById(R.id.et_epg);

        SharedPreferences sp = getSharedPreferences("iptv_urls", MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();
        httpURL = sp.getString("playlist_url", "");//http://tinyurl.com/y9fvs8um
        EpgURL = sp.getString("epg_url", "");//http://tinyurl.com/yadrehbj

//        httpURL = "https://url.subportal.io/azsq3";
//        EpgURL = "https://url.subportal.io/0he7t";


        input1.setText(httpURL, TextView.BufferType.EDITABLE);
        input2.setText(EpgURL, TextView.BufferType.EDITABLE);

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setCancelable(false);
        alert.setTitle("Fill Playlist Url and Epg Url").setView(textEntryView).setPositiveButton("GO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {

                        httpURL = input1.getText().toString();
                        EpgURL = input2.getText().toString();

//                        httpURL = "http://mq.eez363.com:9732/playlist?type=simple-m3u&deviceUser=Markhtv123&devicePass=98765";
//                        httpURL = "http://mq.eez363.com:9732/fullplaylist.m3u?type=m3u&deviceUser=Markhtv123&devicePass=98765";
//                        httpURL = "http://mq.eez363.com:9732/playlist?type=simple-m3u&deviceUser=Markhtv123&devicePass=98765";
//                        httpURL = "http://mq.eez363.com:9732/vodlist.m3u?type=m3u&deviceUser=Markhtv123&devicePass=98765";
//                        httpURL = "http://mq.eez363.com:9732/webtvlist.txt?type=webtv&ts=1&deviceUser=Markhtv123&devicePass=98765";
//                        httpURL = "http://mq.eez363.com:9732/playlist.xml?type=xml&deviceUser=Markhtv123&devicePass=98765";
//                        httpURL = "http://mq.eez363.com:9732/playlist.txt?type=text&deviceUser=Markhtv123&devicePass=98765";

//
//                        httpURL = "https://url.subportal.io/azsq3";
//                        EpgURL = "https://url.subportal.io/0he7t";

//                        httpURL = "http://tinyurl.com/ycmwp3n2";
//                        EpgURL = "https://xl2s.io/r1ved";//http://tinyurl.com/ycwnqh9w";
//                        httpURL = "http://wickediptv.tk:25461/get.php?username=aoORC6Xz3R&password=ZdtPtzPCmi&type=m3u_plus&output=ts";
//                        EpgURL = "http://wickediptv.tk:25461/xmltv.php?username=aoORC6Xz3R&password=ZdtPtzPCmi";

                        httpURL = "https://xl2s.io/cd8yl";
                        EpgURL = "https://xl2s.io/oc9cp";
                        editor.putString("playlist_url", httpURL);
                        editor.putString("epg_url", EpgURL);
                        editor.commit();
                        Toast.makeText(SplashActivity.this,"Start reading Channel data",Toast.LENGTH_LONG).show();
                        ReadAllChannelDataFromUrl();
                    }
                });
        alert.show();

        int myVersion = Build.VERSION.SDK_INT;
        if (myVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            ActivityCompat.requestPermissions(SplashActivity.this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }
    }

    private void rotateImage(final float angle) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ivCenter.setRotation(angle);
            }
        });
    }

    public void fixingChannel(){
//        Toast.makeText(SplashActivity.this, "start fix channel", Toast.LENGTH_LONG).show();
        List<String> delList = new ArrayList<>();
        boolean f = true;

        MainActivity.epg_channels = new ArrayList<>();
        MainActivity.vod_channels = new ArrayList<>();
        for (int m = 0; m < channels.length; m++){
            Epg_Channel e = new Epg_Channel();
            e.channel_id = channels[m].tvg_ID;
            e.display_name = channels[m].tvg_name;
            e.channel_url = channels[m].channel_url;
            e.channel_logo_url = channels[m].tvg_logo_url;
            e.group_title = channels[m].group_title;
            if (channels[m].tvg_ID.contains("")) {
                MainActivity.vod_channels.add(e);
            }
            else {
                MainActivity.epg_channels.add(e);
            }
        }
//        temp = null;
//        Log.d("Memory usage size", (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())+"");
        freeMemory();
        channels = null;
//        Toast.makeText(SplashActivity.this, "all prepared!", Toast.LENGTH_LONG).show();
    }

    private void ReadAllChannelDataFromUrl() {
        final Response.Listener<String> successListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                all_channel_parse(response);

                Toast.makeText(SplashActivity.this,"Start reading time data",Toast.LENGTH_LONG).show();
                setTimefromUrl();
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                final int status = error.networkResponse.statusCode;
                // Handle 30x
                if(HttpURLConnection.HTTP_MOVED_PERM == status || status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_SEE_OTHER) {
                    final String location = error.networkResponse.headers.get("Location");
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, location, successListener, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            finish();
                        }
                    });
                    stringRequest.setRetryPolicy(new RetryPolicy() {
                        @Override
                        public int getCurrentTimeout() {
                            return 70000;
                        }

                        @Override
                        public int getCurrentRetryCount() {
                            return 70000;
                        }

                        @Override
                        public void retry(VolleyError error) throws VolleyError {

                        }
                    });
                    requestQueue= Volley.newRequestQueue(SplashActivity.this);
                    requestQueue.add(stringRequest);
                } else {
                    Toast.makeText(SplashActivity.this,"Network Error",Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        };

        StringRequest stringRequest = new StringRequest(Request.Method.POST, httpURL, successListener, errorListener);
        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 70000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 70000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        requestQueue= Volley.newRequestQueue(SplashActivity.this);
        requestQueue.add(stringRequest);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(SplashActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
//                    finish();
                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
                return;
            }
            default:
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(i);
                finish();


        }
    }
    private void all_channel_parse(String response){
        MainActivity.epg_channels = new ArrayList<>();
        MainActivity.vod_channels = new ArrayList<>();
        if (response.contains("#EXTM3U")) {
            if (response.contains("#EXTINF:0,NOT FOUND")) {
                MainActivity.epg_channels = null;
                MainActivity.vod_channels = null;
                Toast.makeText(SplashActivity.this, "No Channel", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            String[] all = response.trim().split("#EXTINF:-1");
            if (all[1].contains("tvg-id") && all[1].contains("tvg-link")) {
                for (int i = 1; i < all.length; i++) {
                    Epg_Channel cell = new Epg_Channel();
//            channels[i-1] = new Channel_Cell();
                    String[] item = all[i].trim().split("\n");
                    String a = item[0];
//                    Log.d("test: ", "" + item.length);
//                    Log.d("test: ", "" + a);
                    if (a.contains("tvg-name")) {
                        cell.display_name = a.split("tvg-name=\"")[1].split("\"")[0].trim();
//                String[] b = a.trim().split("\"");
                        cell.channel_id = a.split("tvg-id=\"")[1].split("\"")[0].trim();
                        cell.channel_logo_url = "";
                        cell.group_title = a.split("group-title=\"")[1].split("\"")[0].trim();
                    } else {
                        cell.display_name = a.split(",")[1].trim();
                        cell.group_title = a.split("group-title=\"")[1].split("\"")[0].trim();
                        cell.channel_logo_url = "";
                        cell.channel_id = "";
                    }
                    cell.channel_url = item[1];
                    if (!cell.channel_id.equals("")) {
                        MainActivity.epg_channels.add(cell);
                    } else {
                        MainActivity.vod_channels.add(cell);
                    }
                }
            }
            else if (all[1].contains("group-title") &&  all[1].contains("tvg-ID") && all[1].contains("tvg-logo")) {
                for (int i = 1; i < all.length; i++) {
                    Epg_Channel cell = new Epg_Channel();
//            channels[i-1] = new Channel_Cell();
                    String[] item = all[i].trim().split("\n");
                    String a = item[0];
//                    Log.d("test: ", "" + item.length);
//                    Log.d("test: ", "" + a);
                    cell.display_name = a.split("tvg-name=\"")[1].split("\"")[0].trim();
                    cell.group_title = a.split("group-title=\"")[1].split("\"")[0].trim();
                    cell.channel_logo_url = "";
                    cell.channel_id = a.split("tvg-ID=\"")[1].split("\"")[0].trim();;
                    cell.channel_url = item[1];
                    MainActivity.epg_channels.add(cell);
                }
            }else if (all[1].contains("group-title") && !all[1].contains("tvg-id") && !all[1].contains("tvg-link")) {
                for (int i = 1; i < all.length; i++) {
                    Epg_Channel cell = new Epg_Channel();
//            channels[i-1] = new Channel_Cell();
                    String[] item = all[i].trim().split("\n");
                    String a = item[0];
//                    Log.d("test: ", "" + item.length);
//                    Log.d("test: ", "" + a);
                    cell.display_name = a.split(",")[1].trim();
                    cell.group_title = a.split("group-title=\"")[1].split("\"")[0].trim();
                    cell.channel_logo_url = "";
                    cell.channel_id = "";
                    cell.channel_url = item[1];
                    MainActivity.vod_channels.add(cell);
                }
            }
            else if (all[1].split("\n")[0].contains("=")) {
                for (int i = 1; i < all.length; i++) {
                    Epg_Channel cell = new Epg_Channel();
//            channels[i-1] = new Channel_Cell();
                    String[] item = all[i].trim().split("\n");
                    String a = item[0].trim();
                    String[] b = a.trim().split("\"");
                    cell.channel_id = b[1];
                    cell.display_name = b[3];
                    cell.channel_logo_url = b[5];
                    cell.group_title = b[7];
                    cell.channel_url = item[1];
                    if (cell.channel_url.contains(".ts") || cell.channel_url.contains(".m3u8")) {
                        MainActivity.epg_channels.add(cell);
                    } else {
                        MainActivity.vod_channels.add(cell);
                    }
                }
            } else if (all[1].split(",").length == 2) {
                for (int i = 1; i < all.length; i++) {
                    Epg_Channel cell = new Epg_Channel();
//            channels[i-1] = new Channel_Cell();
                    String[] item = all[i].trim().split("\n");
                    String a = item[0];
                    cell.display_name = a.split(",")[1].trim();
//                String[] b = a.trim().split("\"");
                    cell.channel_id = "";
                    cell.channel_logo_url = "";
                    cell.group_title = "";
                    cell.channel_url = item[1];
                    if (cell.channel_url.contains(".ts")) {
                        MainActivity.epg_channels.add(cell);
                    } else {
                        MainActivity.vod_channels.add(cell);
                    }
                }
            } else {
                for (int i = 1; i < all.length; i++) {
                    Epg_Channel cell = new Epg_Channel();
//            channels[i-1] = new Channel_Cell();
                    String[] item = all[i].trim().split("\n");
                    String a = item[0];
                    cell.display_name = a.split(",")[1].trim();
//                String[] b = a.trim().split("\"");
                    cell.channel_id = "";
                    cell.channel_logo_url = "";
                    cell.group_title = "";
                    cell.channel_url = item[1];
                    if (cell.channel_url.contains(".ts")) {
                        MainActivity.epg_channels.add(cell);
                    } else {
                        MainActivity.vod_channels.add(cell);
                    }
                }
            }
        }
        else if (response.contains("<Categories>")) {
            List<String> cats = new ArrayList<>();
            String str = response.split("<Categories>")[1].split("</Categories>")[0];
            int i;
            String[] temp_str = str.split("<Id>");
            for (i = 1; i < temp_str.length; i++){
                String cat_id = temp_str[i].split("</Id>")[0].trim();
                String cat_name = temp_str[i].split("<Name>")[1].split("</Name>")[0].trim();
                String cell = cat_id + "-----" + cat_name;
                cats.add(cell);
            }

            str = response.split("<Channels>")[1].split("</Channels>")[0];
            temp_str = str.split("<Channel>");
            for (i = 1; i < temp_str.length; i++) {
                Epg_Channel cell = new Epg_Channel();
                cell.channel_id = temp_str[i].split("<Id>")[1].split("</Id>")[0];
                cell.display_name = temp_str[i].split("<Name>")[1].split("</Name>")[0];
                cell.channel_url = temp_str[i].split("<PlayUrl>")[1].split("</PlayUrl>")[0].trim();
                cell.group_title = "";
                String cat_id = temp_str[i].split("<Value>")[1].split("</Value>")[0].trim();

                for (int j = 0; j < cats.size(); j++){
                    if (cats.get(j).contains(cat_id + "-----")) {
                        cell.group_title = cats.get(j).split("-----")[1].trim();
                        break;
                    }
                }
                if (cell.group_title.equals(""))
                    MainActivity.vod_channels.add(cell);
                else
                    MainActivity.epg_channels.add(cell);
            }

        }else if (response.contains("# ")) {
            String[] tmp = response.split("# ");
            for (int i = 1; i < tmp.length; i++) {
                Epg_Channel cell = new Epg_Channel();
//            channels[i-1] = new Channel_Cell();
                String[] item = tmp[i].trim().split("\n");
                cell.display_name = item[0].trim();
                cell.group_title = "";
                cell.channel_logo_url = "";
                cell.channel_id = "";
                cell.channel_url = item[1].trim();
                MainActivity.vod_channels.add(cell);
            }
        }
        else {
            String[] items = response.split("Channel name:");
            for (int i = 1; i < items.length; i++){
                Epg_Channel cell = new Epg_Channel();
                cell.display_name = items[i].split("\n")[0];
                cell.channel_url = items[i].split("URL:")[1].trim();
                cell.channel_id = "";
                cell.channel_logo_url = "";
                cell.group_title = "";
                MainActivity.vod_channels.add(cell);
            }
        }

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
            long difference = date1.getTime() - date2.getTime();
            dis =(float)difference / (60.0f * 60.0f * 1000.0f);

        } catch (Exception exception) {
            Log.e("DIDN'T WORK", "exception " + exception);
        }
        return dis;
    }
    public void setTimefromUrl(){

        String HttpUrl = "https://timezoneapi.io/api/ip";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, HttpUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String   response) {
                        //Toast.makeText(LoginActivity.this,response, Toast.LENGTH_LONG).show();

                        JSONObject jsonObj= null;
                        try {
                            jsonObj = new JSONObject(response);
                            MainActivity.current_time = jsonObj.getJSONObject("data").getJSONObject("datetime").getString("year").trim()
                                    +jsonObj.getJSONObject("data").getJSONObject("datetime").getString("month_wilz").trim()
                                    +jsonObj.getJSONObject("data").getJSONObject("datetime").getString("day_wilz").trim()
                                    +jsonObj.getJSONObject("data").getJSONObject("datetime").getString("hour_24_wilz").trim()
                                    +jsonObj.getJSONObject("data").getJSONObject("datetime").getString("minutes").trim()
                                    +jsonObj.getJSONObject("data").getJSONObject("datetime").getString("seconds").trim()
                                    +" "
                                    +jsonObj.getJSONObject("data").getJSONObject("datetime").getString("date_time_wti").split(" ")[5].trim();
                            MainActivity.current_timezone = jsonObj.getJSONObject("data").getJSONObject("timezone").getString("id").trim();

                            Toast.makeText(SplashActivity.this,"Start reading EPG data",Toast.LENGTH_LONG).show();

                            SplashActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    DownloadFileFromURL myUrl = new DownloadFileFromURL(SplashActivity.this);
                                    myUrl.execute();

                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(SplashActivity.this, "Network Error!", Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
        {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(SplashActivity.this);
        requestQueue.add(stringRequest);
    }
}
