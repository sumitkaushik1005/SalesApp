package inc.kaushik.sumit.SalesApp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sumitkaushik on 21/6/17.
 */
public class NewTask extends AppCompatActivity {
    String location, name, details;
    Button add, dismiss;
    EditText etName, etDetails;
    SessionManagement session;
    private String userId;
    public static String addtask = "http://192.168.43.89:3000/api/tasks/addTasks";
    private static final int MY_SOCKET_TIMEOUT_MS = 10000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newtask);
        etName = (EditText) findViewById(R.id.etName);
        etDetails = (EditText) findViewById(R.id.etDetails);
        add = (Button) findViewById(R.id.btAdd);
        dismiss = (Button) findViewById(R.id.btDismiss);
        session = new SessionManagement(getApplicationContext());
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragement);
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("IN")
                .build();
        autocompleteFragment.setFilter(typeFilter);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.

                location = (String) place.getAddress();
                Log.i("address", location);

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("error", status.toString());
                Toast.makeText(NewTask.this, status.toString(), Toast.LENGTH_LONG).show();
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean cancel = true;
                View focusView;
                Map<String, String> map = session.getUserDetails();
                userId = map.get("userId");
                String name = etName.getText().toString();

                if (TextUtils.isEmpty(name)) {
                    cancel = false;
                    focusView = etName;
                }
                if (TextUtils.isEmpty(location)) {
                    cancel = false;
                    Toast.makeText(NewTask.this, "Please enter location", Toast.LENGTH_LONG).show();
                }
                if (cancel) {
                    final ProgressDialog progressDialog = new ProgressDialog(NewTask.this);
                    progressDialog.setMessage("Adding.. ");
                    progressDialog.show();
                    Map<String, String> map1 = new HashMap<String, String>();
                    map1.put("client_name", etName.getText().toString());
                    map1.put("location", location);
                    if (etDetails.getText() != null) {
                        map1.put("details", etDetails.getText().toString());
                    }
                    map1.put("userId", userId);
                    map1.put("ownerId", userId);
                    VolleyCustomRequest request = new VolleyCustomRequest(Request.Method.POST, addtask, map1, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Toast.makeText(NewTask.this, "Task added..", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(NewTask.this, "Server not responding.Please try again..", Toast.LENGTH_LONG).show();
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
                            MY_SOCKET_TIMEOUT_MS,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    RequestQueue requestQueue = Volley.newRequestQueue(NewTask.this);
                    requestQueue.add(request);

                }
            }
        });
    }
}
