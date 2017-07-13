package inc.kaushik.sumit.SalesApp.service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import inc.kaushik.sumit.SalesApp.SessionManagement;
import inc.kaushik.sumit.SalesApp.VolleyCustomRequest;
import inc.kaushik.sumit.SalesApp.app.Config;


/**
 * Created by sumitkaushik on 7/7/17.
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG=MyFirebaseInstanceIDService.class.getSimpleName();
    private static String updateToken="http://192.168.43.89:3000/api/FcmTokens/updateToken";
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        // Saving reg id to shared preferences
        storeRegIdInPref(refreshedToken);

        // sending reg id to your server
        SessionManagement sessionManagement=new SessionManagement(getApplicationContext());
        Map<String,String> map=sessionManagement.getUserDetails();
        if(map.get("userId")!=null)
        sendRegistrationToServer(refreshedToken);

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(Config.REGISTRATION_COMPLETE);
        registrationComplete.putExtra("token", refreshedToken);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
        Log.e(TAG, "sendRegistrationToServer1: " + refreshedToken);


    }

    public void sendRegistrationToServer(String refreshedToken) {
        Log.e(TAG, "sendRegistrationToServer: " + refreshedToken);
        Map<String, String> map1 = new HashMap<String, String>();
        SessionManagement sessionManagement=new SessionManagement(getApplicationContext());
        Map<String,String> map=sessionManagement.getUserDetails();
        map1.put("userId", map.get("userId"));
        map1.put("token", refreshedToken);
        VolleyCustomRequest request = new VolleyCustomRequest(Request.Method.PUT, updateToken, map1, new Response.Listener<JSONObject>() {
            @Override
                    public void onResponse(JSONObject response) {
                Toast.makeText(getApplicationContext(),"Token sent",Toast.LENGTH_LONG).show();
                    }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            Log.e(TAG,"error"+error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(request);

    }

    private void storeRegIdInPref(String refreshedToken) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("regId", refreshedToken);
        editor.commit();
    }
}
