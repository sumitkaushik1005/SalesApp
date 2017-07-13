package inc.kaushik.sumit.SalesApp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import inc.kaushik.sumit.SalesApp.fragment.AboutUs;
import inc.kaushik.sumit.SalesApp.fragment.ContactUs;
import inc.kaushik.sumit.SalesApp.fragment.Home;
import inc.kaushik.sumit.SalesApp.fragment.Notification;
import inc.kaushik.sumit.SalesApp.service.MyFirebaseInstanceIDService;

public class MainActivity extends AppCompatActivity implements
NavigationView.OnNavigationItemSelectedListener{
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private TextView txtName, txtemail,txtempId;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    // tags used to attach the fragments
    private static final String TAG_HOME = "home";
    private static final String TAG_CONTACT = "contact";
    private static final String TAG_ABOUT = "aboutus";
    private static final String TAG_NOTIFICATIONS = "notifications";
    public static String CURRENT_TAG = TAG_HOME;
    private static final String TAG=MyFirebaseInstanceIDService.class.getSimpleName();
    private static String updateToken="http://192.168.43.89:3000/api/FcmTokens/updateToken";
    private static String getInfo="http://192.168.43.89:3000/api/users/";
    private String[] activityTitles;
    public static int navItemIndex = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);
        setSupportActionBar(toolbar);
        drawer=(DrawerLayout)findViewById(R.id.drawer_layout);
        navigationView=(NavigationView)findViewById(R.id.nav_view);
        txtName=(TextView)findViewById(R.id.name);
        txtemail=(TextView)findViewById(R.id.email);
        txtempId=(TextView)findViewById(R.id.empId);
        LinearLayout layout=(LinearLayout)findViewById(R.id.layout1);
        //setUpNavigationView();
        /*if(savedInstanceState==null){
            navItemIndex=0;
            CURRENT_TAG=TAG_HOME;
            loadHomeFragment();
        }*/
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,drawer,toolbar,
                R.string.openDrawer,R.string.closeDrawer);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener( this);
        if(FirebaseInstanceId.getInstance().getToken()!=null){
            String token=FirebaseInstanceId.getInstance().getToken();
            Map<String, String> map1 = new HashMap<String, String>();
            SessionManagement sessionManagement=new SessionManagement(getApplicationContext());
            Map<String,String> map=sessionManagement.getUserDetails();
            map1.put("userId", map.get("userId"));
            map1.put("token", token);
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
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();
        Map<String, String> map1 = new HashMap<String, String>();
        map1.put("","");
        SessionManagement sessionManagement=new SessionManagement(MainActivity.this);
        Map<String,String> map=sessionManagement.getUserDetails();
        VolleyCustomRequest request = new VolleyCustomRequest(Request.Method.GET, getInfo+map.get("userId"), map1, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String name=(String)response.get("name");
                    txtName.setText(Html.fromHtml("<b>Hi..</b><br>")  +"    "+name);
                    String email=(String)response.get("email");
                    txtemail.setText(Html.fromHtml("Email Id: ")+email);
                    String empId=response.getInt( "employee_id")+"";
                    txtempId.setText(Html.fromHtml("Employee Id: ")+empId);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Server not responding.Please try again..", Toast.LENGTH_LONG).show();
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
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(request);
      Snackbar  snackbar = Snackbar.make(layout, "Use Navigation menu for more...", Snackbar.LENGTH_LONG);
        snackbar.show();

    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }

        else
        super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        switch (id) {
            case R.id.nav_home:
                navItemIndex=0;
                Fragment fragment=new Home();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                getSupportFragmentManager().executePendingTransactions();
                fragmentTransaction.commit();
                break;
            case R.id.nav_contact:
                navItemIndex=1;
                Fragment fragment1=new ContactUs();
                FragmentTransaction fragmentTransaction1 = getSupportFragmentManager().beginTransaction();
                fragmentTransaction1.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction1.replace(R.id.frame, fragment1, CURRENT_TAG);
                LinearLayout linearLayout=(LinearLayout)findViewById(R.id.layout1);
                linearLayout.setVisibility(LinearLayout.GONE);
                getSupportFragmentManager().executePendingTransactions();
                fragmentTransaction1.commitAllowingStateLoss();
                break;
            case R.id.nav_about:
                navItemIndex=2;
                Fragment fragment2=new AboutUs();
                FragmentTransaction fragmentTransaction2 = getSupportFragmentManager().beginTransaction();
                fragmentTransaction2.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction2.replace(R.id.frame, fragment2, CURRENT_TAG);
                LinearLayout linearLayout1=(LinearLayout)findViewById(R.id.layout1);
                linearLayout1.setVisibility(LinearLayout.GONE);
                getSupportFragmentManager().executePendingTransactions();
                fragmentTransaction2.commit();
                break;
            case R.id.nav_notifications:
                navItemIndex=3;
                Fragment fragment4=new Notification();
                FragmentTransaction fragmentTransaction4 = getSupportFragmentManager().beginTransaction();
                fragmentTransaction4.setCustomAnimations(android.R.anim.fade_in,
                       android.R.anim.fade_out);
                fragmentTransaction4.replace(R.id.frame, fragment4, CURRENT_TAG);
                getSupportFragmentManager().executePendingTransactions();
                fragmentTransaction4.commit();
                break;
            case R.id.nav_logout:
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("You want to exit")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SessionManagement ses=new SessionManagement(MainActivity.this);
                                ses.logoutUser();

                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
                break;

            default:
                Fragment fragment3=new Home();
                FragmentTransaction fragmentTransaction3 = getSupportFragmentManager().beginTransaction();
                fragmentTransaction3.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction3.replace(R.id.frame, fragment3, CURRENT_TAG);
                getSupportFragmentManager().executePendingTransactions();
                fragmentTransaction3.commit();

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        setToolbarTitle();
        return true;
    }
    private void setToolbarTitle() {
        getSupportActionBar().setTitle(activityTitles[navItemIndex]);
    }
}

