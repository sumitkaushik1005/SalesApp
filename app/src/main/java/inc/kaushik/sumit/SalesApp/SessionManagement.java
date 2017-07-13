package inc.kaushik.sumit.SalesApp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.HashMap;

/**
 * Created by sumitkaushik on 19/6/17.
 */
public class SessionManagement {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    int PRIVATE_MODE=0;
    private static final String PREF_NAME="SalesPref";
    private static final String IS_LOGIN="IsLoggedIn";
    public static final String KEY_PASS="pass";
    public static final String KEY_EMAIL="email";
    public static  final String KEY_ID="userId";
    public SessionManagement(Context context){
        this._context=context;
        pref=_context.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
        editor =pref.edit();
    }
    public void createLoginSession(String email,String pass,String id){
        editor.putBoolean(IS_LOGIN,true);
        editor.putString(KEY_EMAIL,email);
        editor.putString(KEY_PASS,pass);
        editor.putString(KEY_ID,id);
        editor.commit();
    }
    public void checkLogin(){
        if (!this.isLoggedIn()) {
           Intent i=new Intent(_context,LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _context.startActivity(i);
        }
    }
        public HashMap<String,String> getUserDetails(){
            HashMap<String,String> user=new HashMap<>();
            user.put(KEY_EMAIL,pref.getString(KEY_EMAIL,null));
            user.put(KEY_PASS,pref.getString(KEY_PASS,null));
            user.put(KEY_ID,pref.getString(KEY_ID,null));
            return user;
        }
    public void logoutUser(){
        editor.clear();
        editor.commit();
       Intent i=new Intent(_context,LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(i);
    }
    public boolean isLoggedIn() {
        return  pref.getBoolean(IS_LOGIN,false);
    }
}
