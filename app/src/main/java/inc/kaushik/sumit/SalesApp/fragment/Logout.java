package inc.kaushik.sumit.SalesApp.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import inc.kaushik.sumit.SalesApp.R;
import inc.kaushik.sumit.SalesApp.SessionManagement;

public class Logout extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_logout, container, false);
        new AlertDialog.Builder(getContext())
                .setTitle("You want to exit")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SessionManagement ses=new SessionManagement(getContext());
                        ses.logoutUser();

                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            Fragment fragment=new Home();
                FragmentManager fragmentManager=getFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frame,fragment);

                fragmentTransaction.commit();
            }
        }).show();
        return  view;
    }

}
