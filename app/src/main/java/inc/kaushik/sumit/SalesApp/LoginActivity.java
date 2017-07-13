package inc.kaushik.sumit.SalesApp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sumitkaushik on 19/6/17.
 */
public class LoginActivity extends AppCompatActivity {
    EditText txtEmail, txtPassword;
    Button btnLogin;
    SessionManagement session;
    Snackbar snackbar;
    LinearLayout linearLayout;
    private static final String Login_url = "http://192.168.43.89:3000/api/users/loginUser";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        session = new SessionManagement(getApplicationContext());
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        linearLayout = (LinearLayout) findViewById(R.id.loginlayout);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean cancel1 = true;
                boolean cancel2=true;
                final String email = txtEmail.getText().toString().trim();
                final String password = txtPassword.getText().toString().trim();
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    cancel1 = false;
                }
                if(!validateEmail(email)){
                    cancel2=false;
                    Toast.makeText(LoginActivity.this,"Invalid EmailId.",Toast.LENGTH_LONG).show();
                }
                if (cancel1 && cancel2) {
                    if(isConnectedToInternet()) {
                        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
                        progressDialog.setMessage("Logging In...");
                        progressDialog.show();

                        Map<String, String> map = new HashMap<>();
                        map.put("email", email);
                        map.put("password", password);
                        VolleyCustomRequest request = new VolleyCustomRequest(Request.Method.POST, Login_url, map, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONObject jsonObject=response.getJSONObject("email");
                                    String u = jsonObject.getString("userId");
                                    if (!u.isEmpty()) {
                                        Toast.makeText(LoginActivity.this, "Login successful.", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        session.createLoginSession(email, password, u);
                                        startActivity(intent);
                                    } else {
                                        JSONObject json = response.getJSONObject("error");
                                        Toast.makeText(LoginActivity.this, json.getString("message"), Toast.LENGTH_LONG).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                progressDialog.dismiss();
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                NetworkResponse response = error.networkResponse;
                            if(response!=null) {
                                switch (response.statusCode) {
                                    case 401:
                                        Toast.makeText(LoginActivity.this, "Password and mailId doesn't match.", Toast.LENGTH_LONG).show();
                                        break;
                                    case 422:
                                        Toast.makeText(LoginActivity.this,"User blocked by admin",Toast.LENGTH_LONG).show();
                                        break;
                                }
                            }else
                            Toast.makeText(LoginActivity.this,"Server not responding.Please try again...",Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                HashMap<String, String> headers = new HashMap<String, String>();
                                headers.put("Content-Type", "application/x-www-form-urlencoded");
                                return headers;
                            }
                        };
                        RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
                        requestQueue.add(request);
                    }
                    else {
                        snackbar = Snackbar.make(linearLayout, "Internet not Connected", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                } else if(!cancel1) {
                    snackbar = Snackbar.make(linearLayout, "Please fill in the details.", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }

            }
        });
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean validateEmail(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

}
