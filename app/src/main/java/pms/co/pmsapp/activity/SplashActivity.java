package pms.co.pmsapp.activity;

import android.content.Intent;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import pms.co.pmsapp.R;

public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT= 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_splash );

        new Handler( ).postDelayed( new Runnable( ) {
            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this, LogInActivity.class);
                startActivity(i);
                finish();
            }


        }, SPLASH_TIME_OUT);

    }
}
