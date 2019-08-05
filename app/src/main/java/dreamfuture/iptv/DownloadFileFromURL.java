package dreamfuture.iptv;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.CharsetEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Handler;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import okio.Utf8;

import static com.google.android.exoplayer2.mediacodec.MediaCodecInfo.TAG;
import static dreamfuture.iptv.MainActivity.epg_programs;

/**
 * Created by VVV on 2/14/2018.
 */

public class DownloadFileFromURL extends AsyncTask<String, Void, String> {

    /**
     * Before starting background thread
     * Show Progress Bar Dialog
     * */
    File apkStorage = null;
    File outputFile = null;
    InputStream stream = null;
    FileOutputStream fos = null;
    Context parent;
    String downloadFileName;
    public DownloadFileFromURL(Context context){
        parent = context;
        if (SplashActivity.EpgURL.equals(""))
            downloadFileName = "epgFile";
        else
            downloadFileName = SplashActivity.EpgURL.substring(SplashActivity.EpgURL.lastIndexOf( '/' ),SplashActivity.EpgURL.length());
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(final String xml) {
        ((Activity)parent).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (xml.equals("unzip")) {

                    Intent i = new Intent(parent, MainActivity.class);
                    MainActivity.epg_programs = null;
                    parent.startActivity(i);
                    ((Activity)parent).finish();


//                    GZIPInputStream zin = null;
//                    try {
//                        FileInputStream fin = new FileInputStream(downloadFileName);
//                        zin = new GZIPInputStream(fin);
//                        //byte[] arr = IOUtils.toByteArray(zin);
//
//                        //zin.close();
//
//                        //String xmlStr = "";
//                        //xmlStr = IOUtils.toString(zin);
//                        outputFile = new File(downloadFileName);
//                        if(outputFile.exists())
//                            outputFile.delete();
//                        readEPGData(zin);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        Intent i = new Intent(parent, MainActivity.class);
//                        MainActivity.epg_programs = null;
//                        parent.startActivity(i);
//                        ((Activity)parent).finish();
//                    }
                } else if (xml.equals("go") || xml.equals("")){
                    Intent i = new Intent(parent, MainActivity.class);
                    MainActivity.epg_programs = null;
                    parent.startActivity(i);
                    ((Activity)parent).finish();
                }
                else
                    readEPGData(xml);
            }
        });
    }

    @Override
    protected String doInBackground(String... arg0) {
        StringBuffer output = new StringBuffer("");

        String xmlStr = "";

        try {
            URL url = new URL(SplashActivity.EpgURL);//Create Download URl
            int redirectedCount = 0;
            String contentType;
            while (redirectedCount <= 1) {
                URLConnection connection = url.openConnection();

                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();
                httpConnection.getRequestMethod();

                int i = httpConnection.getResponseCode();
                contentType = httpConnection.getContentType();
                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream();
                    if (contentType.equals("application/x-gzip") || contentType.equals("application/octet-stream")){
                        downloadFileName = Environment.getExternalStorageDirectory() + "/" + "guide";
                        InputStream input = new BufferedInputStream(url.openStream(), 8192);
                        OutputStream out = new FileOutputStream(downloadFileName);

                        byte data[] = new byte[1024];
                        long total = 0;
                        int count;
                        while ((count = input.read(data)) != -1) {
                            total += count;
                            // publishing the progress....
                            // After this onProgressUpdate will be called
                            // writing data to file
                            out.write(data, 0, count);
                        }
                        // flushing output
                        out.flush();
                        // closing streams
                        out.close();
                        input.close();
                        return "unzip";
                    } else { //if (contentType.equals("")){
                        BufferedReader buffer = new BufferedReader(
                                new InputStreamReader(stream));
                        String s = "";
                        while ((s = buffer.readLine()) != null)
                            output.append(s);

                        xmlStr = output.toString();
                        if (xmlStr.equals(""))
                            return "";
                    }
                    break;
                } else if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP ||
                        httpConnection.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM) {
                    String redirectedUrl = httpConnection.getHeaderField("Location");
                    url = new URL(redirectedUrl);
                    redirectedCount++;
                }
            }
            stream.close();
        } catch (Exception e) {

            //Read exception if something went wrong
            e.printStackTrace();
            outputFile = new File(downloadFileName);
            outputFile.delete();
            outputFile = null;
            Log.e(TAG, "Download Error Exception " + e.getMessage());
            return "go";
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
            Log.d("OutofMemory", error.getMessage());
            return "go";
        }

        return xmlStr;
    }

    private void readEPGData(String xml) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            epg_programs = new ArrayList<Epg_Program>();

            DefaultHandler handler = new DefaultHandler() {
                Epg_Program epg = null;
                int style = -1;
                public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
                    if(qName.equals("programme")) {
                        epg = new Epg_Program();
                        epg.start = attributes.getValue("start");
                        epg.stop = attributes.getValue("stop");
                        epg.channel = attributes.getValue("channel");
                        MainActivity.server_timezone = epg.start.split(" ")[1];
                    } else {
                        if(epg != null) {
                            if(qName.equals("title")) {
                                style = 0;
                            } else if(qName.equals("desc")) {
                                style = 1;
                            }
                        }
                    }
                }//end of startElement method

                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if(qName.equals("programme")) {
                        if (epg != null) {
                            float sd = getdiff(MainActivity.current_time, convertTimeZone(epg.start));
                            float ed = getdiff(MainActivity.current_time, convertTimeZone(epg.stop));
//                            if (sd<1.0f ||ed<1.0f)
                                epg_programs.add(epg);
                        }
                    } else if(qName.equals("tv")) {
                        ((Activity)parent).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent i = new Intent(parent, MainActivity.class);
                                parent.startActivity(i);
                                ((Activity)parent).finish();
                            }
                        });
                    }
                }

                public void characters(char ch[], int start, int length) throws SAXException {
                    switch (style) {
                        case 0:
                            epg.title = new String(ch, start, length);
                            break;
                        case 1:
                            epg.description = new String(ch, start, length);
                            break;
                    }
                    style = -1;
                }//end of characters
            };

            InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            saxParser.parse(stream, handler);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void readEPGData(GZIPInputStream xml) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            epg_programs = new ArrayList<Epg_Program>();

            DefaultHandler handler = new DefaultHandler() {
                Epg_Program epg = null;
                int style = -1;
                public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
                    if(qName.equals("programme")) {
                        epg = new Epg_Program();
                        epg.start = attributes.getValue("start");
                        epg.stop = attributes.getValue("stop");
                        epg.channel = attributes.getValue("channel");
                        MainActivity.server_timezone = epg.start.split(" ")[1];
                    } else {
                        if(epg != null) {
                            if(qName.equals("title")) {
                                style = 0;
                            } else if(qName.equals("desc")) {
                                style = 1;
                            }
                        }
                    }
                }//end of startElement method

                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if(qName.equals("programme")) {
                        if (epg != null) {
                            float sd = getdiff(MainActivity.current_time, convertTimeZone(epg.start));
                            float ed = getdiff(MainActivity.current_time, convertTimeZone(epg.stop));
//                            if (sd<1.0f ||ed<1.0f  )
                                epg_programs.add(epg);
                        }
                    } else if(qName.equals("tv")) {
                        ((Activity)parent).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent i = new Intent(parent, MainActivity.class);
                                parent.startActivity(i);
                                ((Activity)parent).finish();
                            }
                        });
                    }
                }

                public void characters(char ch[], int start, int length) throws SAXException {
                    switch (style) {
                        case 0:
                            epg.title = new String(ch, start, length);
                            break;
                        case 1:
                            epg.description = new String(ch, start, length);
                            break;
                    }
                    style = -1;
                }//end of characters
            };

            //InputStream stream = new ByteArrayInputStream(xml);
            saxParser.parse(xml, handler);
        }catch (Exception e){
            e.printStackTrace();
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
            dis =Math.abs((float)difference / (60.0f * 60.0f * 1000.0f));

        } catch (Exception exception) {
            Log.e("DIDN'T WORK", "exception " + exception);
        }
        return dis;
    }
    public static String convertTimeZone(String str_time){
        SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyyMMddHHmmss");
//        String a = epg_programs.get(0).start.split(" ")[1];
        if (MainActivity.server_timezone == null)
            return str_time;
        String t = "GMT"+MainActivity.server_timezone.substring(0,3)+":"+MainActivity.server_timezone.substring(3);
        sourceFormat.setTimeZone(TimeZone.getTimeZone(t));
        try {
            Date parsed = sourceFormat.parse(str_time); // => Date is in UTC now

            TimeZone tz = TimeZone.getTimeZone(MainActivity.current_timezone);
            SimpleDateFormat destFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            destFormat.setTimeZone(tz);

            String result = destFormat.format(parsed);
            return result;
        }catch (Exception e){
            return "";
        }
    }
}
