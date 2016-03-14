package org.esec.mcg.androidu2f;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.esec.mcg.androidu2f.client.curl.Curl;
import org.esec.mcg.androidu2f.curl.FidoWebService;
import org.esec.mcg.utils.HTTP;
import org.esec.mcg.utils.logger.LogUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements LoginFragment.OnFragmentInteractionListener, EnrollFragment.OnFragmentInteractionListener,
                    SignFragment.OnFragmentInteractionListener {

    private final Map<String,String> details = new LinkedHashMap<String, String>();
    private static final int REG_ACTIVITY_RES_1 = 1;
    private static final int SIGN_ACTIVITY_RES_2 = 2;
    public static String sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        LoginFragment fragment = new LoginFragment();
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
////        TextView tv = (TextView) findViewById(R.id.text_view);
////        SharedPreferences pf = PreferenceManager.getDefaultSharedPreferences(this);
////        tv.setText(pf.getString("server_endpoint", "yz"));
//
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent("org.esec.mcg.androidu2f.SettingsActivity"));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REG_ACTIVITY_RES_1) {
            if (resultCode == RESULT_CANCELED) {
                Fragment currentFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
                ((TextView)currentFragment.getView().findViewById(R.id.status_text)).setText(data.getExtras().getString("Response"));
                return;
            }
            LogUtils.d("resultCode = " + resultCode);
            String registerResponse = data.getStringExtra("Response");
            LogUtils.d(registerResponse);
            //TODO send register response to StrongAuth U2F Server
            try {
                final JSONObject response = new JSONObject(registerResponse).getJSONObject("responseData").put("sessionId", sessionId);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String webResponse = null;
                        try {
                            webResponse = FidoWebService.callFidoWebService(FidoWebService.SKFE_REGISTER_WEBSERVICE, getResources(), "yz", response);
                        } catch (U2FException e) {
                            e.printStackTrace();
                        }
                        LogUtils.d(webResponse);
                    }
                }).start();

            } catch (JSONException e) {
                e.printStackTrace();
            }

//            SharedPreferences pf = PreferenceManager.getDefaultSharedPreferences(this);
//            String endPoint = pf.getString("server_endpoint", null);
//            String bindPoint = pf.getString("bind", null);
//
//            String headerStr = "Content-Type:Application/json Accept:Application/json";
//            Curl.postInSeperateThread(endPoint + bindPoint, headerStr, registerResponse);


//            HTTP.post(new URL(endPoint + bindPoint), )
        } else if (SIGN_ACTIVITY_RES_2 == requestCode) {
            if (resultCode == RESULT_CANCELED) {
                LogUtils.d("sign failed!");
                return;
            } else if (resultCode == RESULT_OK) {
                final String signResponse = data.getStringExtra("Response");

                LogUtils.d(signResponse);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject response = new JSONObject(signResponse).getJSONObject("responseData");
                            String webResponse = null;
                            webResponse = FidoWebService.callFidoWebService(FidoWebService.SKFE_AUTHENTICATE_WEBSERVICE, getResources(), "ly", response);
                            LogUtils.d(webResponse);
                        } catch (JSONException | U2FException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }

    /**
     * Callback of the LoginFragment.
     * After user inputs username and password, do register or authenticate the android token
     * @param username
     * @param password
     * @param sign If false, then register; If true, then authenticate.
     */
    @Override
    public void onLoginEntered(String username, String password, boolean sign) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        details.clear();
//        Fragment fragment = sign ? new SignFragment(details, username, password) : new EnrollFragment(details, username, password);
        Fragment fragment = sign ? (Fragment)SignFragment.newInstance(username, password) : (Fragment)EnrollFragment.newInstance(username, password);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
