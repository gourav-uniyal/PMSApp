package pms.co.pmsapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

import pms.co.pmsapp.R;
import pms.co.pmsapp.adapter.MainAdapter;
import pms.co.pmsapp.adapter.ViewPagerAdapter;
import pms.co.pmsapp.fragment.CompletedTasksFragment;
import pms.co.pmsapp.fragment.IncompleteTasksFragment;
import pms.co.pmsapp.model.Case;
import pms.co.pmsapp.service.CheckLocationService;
import pms.co.pmsapp.utils.AppPreferences;

public class HomeActivity extends AppCompatActivity {

    //region Variable Declaration
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private String verifier;
    private ViewPagerAdapter viewPagerAdapter;
    private boolean doubleBackToExitPressedOnce = false;
    private static long back_pressed;
    private AppPreferences appPreferences;
    private static final String TAG = "HomeActivity";
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        verifier = getIntent().getStringExtra("verifier");

        Log.v(TAG, verifier);

        viewPager = findViewById(R.id.pager);
        tabLayout = findViewById(R.id.tabLayout);

        appPreferences = new AppPreferences( this );

        Bundle bundle = new Bundle();
        bundle.putString("verifier", verifier);

        //region Fragment Setup
        CompletedTasksFragment completedTasksFragment = new CompletedTasksFragment();
        completedTasksFragment.setArguments(bundle);
        IncompleteTasksFragment incompleteTasksFragment = new IncompleteTasksFragment();
        incompleteTasksFragment.setArguments(bundle);
        //endregion

        //region ViewPager Setup
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(completedTasksFragment, "Completed Tasks");
        viewPagerAdapter.addFragment(incompleteTasksFragment, "Incomplete Tasks");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        //endregion

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(HomeActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();

                startService(new Intent(this, CheckLocationService.class));
            } else {

                Toast.makeText(HomeActivity.this, "Permission denied to read your GPS", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_log_out) {
            appPreferences.clear();
            startActivity( new Intent( this, LogInActivity.class ) );
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        if (back_pressed + 1500 > System.currentTimeMillis()) super.onBackPressed();
        else Toast.makeText(getBaseContext(), "Press once again to exit!", Toast.LENGTH_SHORT).show();
        back_pressed = System.currentTimeMillis();
    }
}
