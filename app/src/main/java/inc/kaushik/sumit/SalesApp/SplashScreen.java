package inc.kaushik.sumit.SalesApp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        final ImageView icon=(ImageView)findViewById(R.id.splash);
        Animation anim= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade);
        icon.setAnimation(anim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SessionManagement sessionManagement=new SessionManagement(getApplicationContext());
                if (sessionManagement.isLoggedIn()) {
                    Intent intent=new Intent(SplashScreen.this,MainActivity.class);
                    startActivity(intent);
                    icon.setAnimation(null);
                    finish();
                }
                else{
                    Intent intent=new Intent(SplashScreen.this, LoginActivity.class);
                    startActivity(intent);
                    icon.setAnimation(null);
                    finish();
                }
            }
        },2000);
    }
}
