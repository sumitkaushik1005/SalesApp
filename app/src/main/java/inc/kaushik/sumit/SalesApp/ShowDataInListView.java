package inc.kaushik.sumit.SalesApp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by sumitkaushik on 6/7/17.
 */
public class ShowDataInListView extends Fragment implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener
{

    private static final int MY_SOCKET_TIMEOUT_MS = 10000;
    SessionManagement session;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates = false;
    private Location mLastLocation;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters
    private static final String TAG = MainActivity.class.getSimpleName();
    TextView lblEmail;
    public static String userId;
    private String getTask = "http://192.168.43.89:3000/api/tasks/getTasksForAdmin";
    private String updatetask = "http://192.168.43.89:3000/api/tasks/updateTask";
    RecyclerView rlist;
    int[] addedBy = new int[100];
    String[] taskId = new String[100];
    private String clientName, details, status;
    private String slocation = "";
    private List<Task> feedList = new ArrayList<>();
    private String destinations[] = new String[20];
    public static double latitude1, longitude1;
    private FloatingActionButton locationUpdate;
    private String changestatus = "http://192.168.43.89:3000/api/tasks/changeStatus";
    private LocationCallback mLocationCallback;
    private String updatedLocation;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view=inflater.inflate(R.layout.listviewlayout,container,false);
        lblEmail=(TextView)view.findViewById(R.id.lblEmail);
        rlist=(RecyclerView)view.findViewById(R.id.recycler_view);
        rlist.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayout=new LinearLayoutManager(getContext());
        rlist.setLayoutManager(mLayout);
        rlist.setItemAnimator(new DefaultItemAnimator());
        rlist.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        final TaskAdapter mAdapter=new TaskAdapter(feedList);
        rlist.setAdapter(mAdapter);
        final SwipeRefreshLayout swipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.swpRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GetContent getContent=new GetContent();
                if (isConnectedToInternet()) {
                    getContent.loadContent(mAdapter);
                    swipeRefreshLayout.setRefreshing(false);
                }else
                {
                    Toast.makeText(getContext(),"Internet not connected",Toast.LENGTH_LONG).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        session=new SessionManagement(getActivity().getApplicationContext());
        session.checkLogin();
        locationUpdate=(FloatingActionButton)view.findViewById(R.id.locationUpdate);
        locationUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePeriodicLocationUpdates();
            }
        });
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();

        }
        mLocationCallback=new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location1 : locationResult.getLocations()) {
                    Toast.makeText(getActivity().getApplicationContext(), location1.toString(), Toast.LENGTH_LONG).show();
                    lblEmail.setText(location1.toString());
                }
            }
        };
        final GetContent getContent1=new GetContent();
        if (isConnectedToInternet()) {
            getContent1.loadContent(mAdapter);
        }
        else
        Toast.makeText(getActivity().getApplicationContext(),"Internet Not connected..",Toast.LENGTH_LONG).show();
        rlist.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity().getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(final View view, final int position) {
                        final Task task = feedList.get(position);
                        //   Toast.makeText(MainActivity.this,task.getClientName()+"",Toast.LENGTH_LONG).show();
                        if (addedBy[position] == 0) {
                            //  Toast.makeText(MainActivity.this,"You can edit this",Toast.LENGTH_LONG).show();
                            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.popuptask);
                            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View layout = layoutInflater.inflate(R.layout.addtasklayout, linearLayout);
                            final PopupWindow popupWindow = new PopupWindow(layout, LinearLayout.LayoutParams.MATCH_PARENT, 800, true);
                            popupWindow.setContentView(layout);

                            popupWindow.setFocusable(true);
                            popupWindow.setAnimationStyle(android.R.style.Animation_Toast);
                            popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
                            Button dismiss = (Button) layout.findViewById(R.id.addTaskDismiss);
                            final EditText eCName = (EditText) layout.findViewById(R.id.etCleintname);
//                            final EditText eCLocation = (EditText) layout.findViewById(R.id.etTaskLocation);
                            final EditText eCDetail = (EditText) layout.findViewById(R.id.etTaskdetails);
                            final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                                  getActivity().getFragmentManager().findFragmentById(R.id.etTaskLocation);
                            autocompleteFragment.setText(task.getLocation());
                            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                                    .setCountry("IN")
                                    .build();
                            autocompleteFragment.setFilter(typeFilter);
                            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                                @Override
                                public void onPlaceSelected(Place place) {
                                    // TODO: Get info about the selected place.
                                    updatedLocation = (String) place.getAddress();
                                }

                                @Override
                                public void onError(Status status) {
                                    // TODO: Handle the error.
                                    Log.i("error", status.toString());
                                    Toast.makeText(getActivity(), status.toString(), Toast.LENGTH_LONG).show();
                                }

                            });

                            eCName.setText(task.getClientName());
                            eCDetail.setText(task.getDetails());
                            dismiss.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    popupWindow.dismiss();
                                    PlaceAutocompleteFragment f = (PlaceAutocompleteFragment)getActivity().getFragmentManager().findFragmentById(R.id.etTaskLocation);
                                   getActivity().getFragmentManager().beginTransaction().remove(f).commit();
                                }
                            });
                            final Button updateTask = (Button) layout.findViewById(R.id.addTaskButton);
                            updateTask.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                                    progressDialog.setMessage("Updating...");
                                    progressDialog.show();
                                    Map<String, String> map = new HashMap<>();
                                    map.put("client_name", eCName.getText().toString());
                                    if (updatedLocation != null) {
                                        map.put("location", updatedLocation);
                                    } else {
                                        updatedLocation = task.getLocation();
                                    }
                                    if (details != null) {
                                        map.put("details", eCDetail.getText().toString());
                                    }
                                    Map<String, String> map1 = session.getUserDetails();
                                    userId = map1.get("userId");
                                    map.put("userId", userId);
                                    map.put("ownerId", userId);
                                    map.put("id", taskId[position]);
                                    VolleyCustomRequest request = new VolleyCustomRequest(Request.Method.PUT, updatetask, map, new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                JSONObject jsonObject = response.getJSONObject("id");
                                                if (jsonObject.getString("nModified").equals("1")) {
                                                    Toast.makeText(getActivity(), "Task Updated...", Toast.LENGTH_LONG).show();
                                                    popupWindow.dismiss();
                                                }
                                               getActivity().getFragmentManager().beginTransaction().remove(autocompleteFragment).commit();

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            progressDialog.dismiss();
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            //Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_LONG).show();
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
                                    RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
                                    requestQueue.add(request);
                                }
                            });
                        } else {
                            Toast.makeText(getActivity(), "Task added by admin.You can't edit this", Toast.LENGTH_LONG).show();

                        }
                    }

                })

        );
        FloatingActionButton floatingActionButton=(FloatingActionButton)view.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent=new Intent(getContext(),NewTask.class);
                startActivity(intent);
            }
        });
        return  view;

    }

    private void togglePeriodicLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            // Changing the button text


            mRequestingLocationUpdates = true;

            // Starting the location updates
            startLocationUpdates();

            Log.d(TAG, "Periodic location updates started!");

        } else {
            // Changing the button text

            mRequestingLocationUpdates = false;

            // Stopping the location updates
            stopLocationUpdates();

            Log.d(TAG, "Periodic location updates stopped!");
        }
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity())
                        .setTitle("Location Permission required")
                        .setMessage("Location permission required")
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        99);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        99);
            }
        } else
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 99: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "sms & location services permission granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            new AlertDialog.Builder(getActivity().getApplicationContext())
                                    .setTitle("Location permission denied")
                                    .setMessage("Location permission is required in order to fetch device's geographic position and to be able to calculate your distance from the destination")
                                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            //Prompt the user once explanation has been shown
                                            ActivityCompat.requestPermissions(getActivity(),
                                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                                    99);
                                        }
                                    })
                                    .create()
                                    .show();
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(getActivity().getApplicationContext(), "Go to settings and enable Location permissions", Toast.LENGTH_LONG)
                                    .show();
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getActivity().getApplicationContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getActivity().getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();

            }
            return false;
        }
        return true;
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        //displayLocation();
        startLocationUpdates();


    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPlayServices();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            if (checkLocationPermission()) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                  //  startLocationUpdates();
                    displayLocation();
                }
            }

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                new AlertDialog.Builder(getActivity())
                        .setTitle("Location permission denied")
                        .setMessage("1.Location permission is required in order to fetch device's geographic position and to be able to calculate your distance from the destination")
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        99);
                            }
                        })
                        .create()
                        .show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        99);
            }
            return false;
        } else {
            return true;
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        Toast.makeText(getContext(), "Location changed!",
                Toast.LENGTH_SHORT).show();
        displayLocation();

    }

    private void displayLocation() {
        try {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                    new AlertDialog.Builder(getActivity().getApplicationContext())
                            .setTitle("Location permission denied")
                            .setMessage("Location permission is required in order to fetch device's geographic position and to be able to calculate your distance from the destination")
                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Prompt the user once explanation has been shown
                                    ActivityCompat.requestPermissions(getActivity(),
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            99);
                                }
                            })
                            .create()
                            .show();

                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            99);
                }
                return;
            }
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLastLocation != null) {
                latitude1 = mLastLocation.getLatitude();
                longitude1 = mLastLocation.getLongitude();

                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                List<Address> addresses;
                addresses = geocoder.getFromLocation(latitude1, longitude1, 1);
                String add = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                lblEmail.setTypeface(null, Typeface.BOLD);
                lblEmail.setText(add + city + state + country + postalCode);




            } else {
                lblEmail.setText("(Couldn't get the location. Make sure location is enabled on the device)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getContext(), "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode(),Toast.LENGTH_LONG).show();
    }



    public class GetContent {
        public void loadContent(final TaskAdapter mAdapter) {

            Map<String, String> map = session.getUserDetails();
            Map<String, String> param = new HashMap<>();
            param.put("userId", map.get("userId"));
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Loading contents..");
            progressDialog.show();
            VolleyCustomRequest request = new VolleyCustomRequest(Request.Method.POST,
                    getTask, param, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    try {
                        int j = 0;
                        JSONArray jsonArray = response.getJSONArray("id");
                        feedList.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            taskId[j] = object.getString("_id");
                            clientName = object.getString("client_name");
                            slocation = object.getString("location");
                            status = object.getString("status");
                            JSONArray array = object.getJSONArray("lat_lng");
                            String lat2 = array.getString(0);
                            String long2 = array.getString(1);
                            float[] results = new float[10];
                            if (mLastLocation != null) {
                                latitude1 = mLastLocation.getLatitude();
                                longitude1 = mLastLocation.getLongitude();
                                double d = calculateDistance(latitude1, longitude1, Double.parseDouble(lat2), Double.parseDouble(long2));
                                if (d < 3.0) {
                                    changeStatus(taskId[j]);
                                }
                            }

                            details = "";
                            addedBy[j] = object.getInt("added_by");
                            if (object.has("details")) {
                                details = object.getString("details");
                            }
                            destinations[j] = object.getString("location");
                            Task task = new Task(clientName, slocation, status, details);
                            feedList.add(task);
                            j++;
                        }
                        rlist.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.i("exception", e.toString());
                    }
                    progressDialog.dismiss();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i("errorHere", error.toString());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> header = new HashMap<>();
                    header.put("Content-type", "application/x-www-form-urlencoded");
                    return header;
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(
                    MY_SOCKET_TIMEOUT_MS,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
            requestQueue.add(request);
        }
    }
    private void changeStatus(String s) {

        Map<String, String> map = new HashMap<>();
        map.put("id", s);

        VolleyCustomRequest request = new VolleyCustomRequest(Request.Method.PUT,
                changestatus, map, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("response", response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("error in change staus", error.toString());
                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> header = new HashMap<>();
                header.put("Content-type", "application/x-www-form-urlencoded");
                return header;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        requestQueue.add(request);

    }
    private double calculateDistance(double initialLat, double initialLong, double finalLat, double finalLong) {
        int R = 6371; // km (Earth radius)
        double dLat = toRadians(finalLat - initialLat);
        double dLon = toRadians(finalLong - initialLong);
        initialLat = toRadians(initialLat);
        finalLat = toRadians(finalLat);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(initialLat) * Math.cos(finalLat);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    private double toRadians(double deg) {
        return deg * (Math.PI / 180);
    }
}
