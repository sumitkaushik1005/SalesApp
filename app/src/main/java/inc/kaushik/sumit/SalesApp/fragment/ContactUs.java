package inc.kaushik.sumit.SalesApp.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import inc.kaushik.sumit.SalesApp.MainActivity;
import inc.kaushik.sumit.SalesApp.R;
import inc.kaushik.sumit.SalesApp.SessionManagement;
import inc.kaushik.sumit.SalesApp.VolleyCustomRequest;


public class ContactUs extends Fragment {
    private  String feedBack="http://192.168.43.89:3000/api/feedbacks";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_contact_us, container, false);
        final EditText editText=(EditText)view.findViewById(R.id.feedback);
        Button send=(Button)view.findViewById(R.id.send);
        SessionManagement sessionManagement=new SessionManagement(getContext());
        Map<String,String> map=sessionManagement.getUserDetails();
        final String email=map.get("email");
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean cancel=true;
                final ProgressDialog progressDialog=new ProgressDialog(getContext());
                progressDialog.setMessage("Adding.. ");
                progressDialog.show();
                Map<String,String> map1=new HashMap<String, String>();
                map1.put("email",email);
                if(TextUtils.isEmpty(editText.getText().toString())){
                    cancel=false;
                }
                map1.put("feedback",editText.getText().toString());
                VolleyCustomRequest request=new VolleyCustomRequest(Request.Method.POST, feedBack, map1, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getContext(),"Feed back sent",Toast.LENGTH_LONG).show();
                        editText.setText(null);
                        progressDialog.dismiss();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(),"error",Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String,String> headers=new HashMap<String, String>();
                        headers.put("Content-Type","application/x-www-form-urlencoded");
                        return headers;
                    }
                };
                request.setRetryPolicy(new DefaultRetryPolicy(
                        10000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                RequestQueue requestQueue= Volley.newRequestQueue(getContext());
                if(cancel)
                    requestQueue.add(request);
                else
                    Toast.makeText(getContext(),"Please say something..",Toast.LENGTH_LONG).show();

            }
        });
        return view;
    }


}
