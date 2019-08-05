package dreamfuture.iptv;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static dreamfuture.iptv.MainActivity.freeMemory;

public class LoginActivity extends AppCompatActivity{

    private String uuid;
    private static final String TAG = "payment";
    private Button btn_pay;
    private Button btn_trial;
    private Button btn_exit;

    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_PRODUCTION;

    // note that these credentials will differ between live & sandbox environments.
    //////   sandbox_client_id = AcIYrbn9gdQ_qJLdow73kVfRfUYkhEc4rWWtzWF_wD13AB3JQ4hP6c31nD2UGsyQkmUtqiCzkjlyd0Wc
    //////    Live_client_id = Aa9PxuCgVYjeYzyZGSMurWDfVsHgKzYBZQ3zlb9lffVAUrmGT5kSPAreU55p7S8p-K8ErUd7feNlZJYG
    private static final String CONFIG_CLIENT_ID = "Aa9PxuCgVYjeYzyZGSMurWDfVsHgKzYBZQ3zlb9lffVAUrmGT5kSPAreU55p7S8p-K8ErUd7feNlZJYG";

    private static final int REQUEST_CODE_PAYMENT = 1;

    private static PayPalConfiguration config;
//    boolean f;
    boolean timeflag;
    String start;

    public static SharedPreferences sp;
    public static SharedPreferences.Editor editor;
    //////////////firebase//////////////
    private DatabaseReference mRootRef;
    private ValueEventListener mValueEventListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        uuid = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        Log.d("test",uuid);
        sp = getSharedPreferences("IPTV", MODE_PRIVATE);
        editor = sp.edit();

        ////////////////////////////////////////////////////////////

//        mRootRef.removeValue();
//        readDataFromFireBase();
        ////////////////////////////////////////////////////////////

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        mRootRef = firebaseDatabase.getReference();
        mRootRef.child(uuid).child(uuid).setValue("test");
        btn_pay = (Button) findViewById(R.id.pay_button);
        btn_trial = (Button) findViewById(R.id.trial_button);
        btn_exit = (Button) findViewById(R.id.exit_button);
        if (sp != null) {
            String iptv_state = sp.getString("iptv_state", "");
            if (iptv_state.equals("purchased")) {
                Intent i = new Intent(LoginActivity.this, SplashActivity.class);
                startActivity(i);
                finish();
                freeMemory();
                Log.d("Memory usage size", (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "");
                return;
            } else if (iptv_state.equals("trial")){
                sync(uuid);
            } else if (iptv_state.equals("expired")){
                btn_trial.setEnabled(false);

            }
            else {
                sync(uuid);
            }
        }
        else
            finish();
        final Intent intent = new Intent(this, PayPalService.class);
        btn_pay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                config = new PayPalConfiguration()
                        .environment(CONFIG_ENVIRONMENT)
                        .clientId(CONFIG_CLIENT_ID);

                intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                startService(intent);
                payviapaypal();
            }
        });

        btn_trial.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
//                editor.putString("iptv_start", current_time);


                editor.putString("iptv_state", "trial");
                editor.apply();
                editor.commit();
                updateDatabase(uuid, "trial");
                Intent i = new Intent(LoginActivity.this, SplashActivity.class);
                startActivity(i);
                finish();
                freeMemory();
            }
        });

        btn_exit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void payviapaypal() {
        PayPalPayment ppp = new PayPalPayment(new BigDecimal("5.0"), "GBP", "Purchase this app",
                PayPalPayment.PAYMENT_INTENT_SALE)
                .payeeEmail("hostingsolutions@hushmail.com");
        Intent intent = new Intent(LoginActivity.this, PaymentActivity.class);
        // send the same configuration for restart resiliency
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, ppp);

        startActivityForResult(intent, REQUEST_CODE_PAYMENT);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm =
                        data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        Log.i(TAG, confirm.toJSONObject().toString(4));
                        Log.i(TAG, confirm.getPayment().toJSONObject().toString(4));
                        /**
                         *  TODO: send 'confirm' (and possibly confirm.getPayment() to your server for verification
                         * or consent completion.
                         * See https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
                         * for more details.
                         *
                         * For sample mobile backend interactions, see
                         * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
                         */
                        displayResultText("PaymentConfirmation info received from PayPal");
                        editor.putString("iptv_state", "purchased");
                        editor.apply();
                        editor.commit();
                        updateDatabase(uuid, "purchased");

                        Intent i = new Intent(LoginActivity.this, SplashActivity.class);
                        startActivity(i);
                        finish();
                        freeMemory();

                    } catch (JSONException e) {
                        Log.e(TAG, "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                displayResultText("The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                displayResultText("An invalid Payment or PayPalConfiguration was submitted. Please try again");
            }
        }
    }
    protected void displayResultText(String result) {
//        ((TextView)findViewById(R.id.txtResult)).setText("Result : " + result);
        Toast.makeText(
                getApplicationContext(),
                result, Toast.LENGTH_LONG)
                .show();
    }


    @Override
    public void onDestroy() {
        // Stop service when done
        stopService(new Intent(this, PayPalService.class));
//        freeMemory();
        super.onDestroy();
    }

    public void updateDatabase(String uuid, String com){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setCancelable(false);
        pd.setMessage("sync");
        pd.show();
        String HttpUrl = "http://54.37.17.137/manage.php?id="+uuid+"&com="+com;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, HttpUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String   response) {
                        pd.dismiss();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this, "Network Error", Toast.LENGTH_LONG).show();
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
        RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
        requestQueue.add(stringRequest);
    }
    public void sync(final String uuid){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setCancelable(false);
        pd.setMessage("sync");
        pd.show();
        String HttpUrl = "http://54.37.17.137/manage.php?id="+uuid;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, HttpUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String   response) {
                        response = response.trim();
                        if (response.equals("purchased")){
                            displayResultText("purchased");
                            editor.putString("iptv_state", "purchased");
                            editor.apply();
                            editor.commit();

                            Intent i = new Intent(LoginActivity.this, SplashActivity.class);
                            startActivity(i);
                            finish();
                            freeMemory();
                        }
                        else if (response.equals("trial")){}
                        else if (response.equals("expired")){
                            displayResultText("expired");
                            Log.d("uuid", uuid);
                            btn_trial.setEnabled(false);
                        }
                        else {
                            displayResultText("Welcome to IPTV");
                        }
                        pd.dismiss();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this, "Network Error", Toast.LENGTH_LONG).show();
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
        RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
        requestQueue.add(stringRequest);
    }

}

