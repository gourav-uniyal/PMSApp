package pms.co.pmsapp.activity;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.ferfalk.simplesearchview.SimpleSearchView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.jaredrummler.materialspinner.MaterialSpinner;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import pms.co.pmsapp.R;
import pms.co.pmsapp.adapter.ViewPagerAdapter;
import pms.co.pmsapp.fragment.CompletedTasksFragment;
import pms.co.pmsapp.fragment.IncompleteTasksFragment;
import pms.co.pmsapp.service.CheckLocationService;
import pms.co.pmsapp.utils.AppPreferences;

public class HomeActivity extends AppCompatActivity {

    //region Variable Declaration
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private SearchView searchView;
    private MaterialSpinner spinner;
    private CompletedTasksFragment completedTasksFragment;
    private IncompleteTasksFragment incompleteTasksFragment;

    private String spinnerSelectedItem="all";
    private String verifier;
    private static long back_pressed;
    private AppPreferences appPreferences;
    private ViewPagerAdapter viewPagerAdapter;

    private static final String TAG = "HomeActivity";
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_home );

        initToolbar();

        initSpinner();

        initTabs();

        ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1 );

        appPreferences = new AppPreferences( this );
    }

    void initToolbar(){
        String name = getIntent( ).getStringExtra( "name" );
        verifier = getIntent( ).getStringExtra( "verifier" );

        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );
        TextView lblToolBarTitle = findViewById( R.id.lbl_toolbar_title );
        lblToolBarTitle.setText( "(" + name + ")" );
    }

    void initSpinner(){
        spinner = findViewById( R.id.spinner_main );

        spinner.setItems( "all", "File Id", "Verification Point", "Document Name", "Subject", "Client Name" );
        spinner.setOnItemSelectedListener( (MaterialSpinner.OnItemSelectedListener<String>) (view, position, id, item) -> {
            spinnerSelectedItem = item;
            Snackbar.make( view, "Clicked " + item, Snackbar.LENGTH_LONG ).show( );
        } );
    }

    void initTabs(){

        viewPager = findViewById( R.id.pager );
        tabLayout = findViewById( R.id.tabLayout );

        Bundle bundle = new Bundle( );
        bundle.putString( "verifier", verifier );

        //region Fragment Setup
        completedTasksFragment = new CompletedTasksFragment( );
        completedTasksFragment.setArguments( bundle );
        incompleteTasksFragment = new IncompleteTasksFragment( );
        incompleteTasksFragment.setArguments( bundle );
        //endregion

        //region ViewPager Setup
        viewPagerAdapter = new ViewPagerAdapter( getSupportFragmentManager( ) );
        viewPagerAdapter.addFragment( completedTasksFragment, "Completed Tasks" );
        viewPagerAdapter.addFragment( incompleteTasksFragment, "Incomplete Tasks" );
        viewPager.setAdapter( viewPagerAdapter );
        tabLayout.setupWithViewPager( viewPager );
        //endregion
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText( HomeActivity.this, "Permission Granted", Toast.LENGTH_SHORT ).show( );

                startService( new Intent( this, CheckLocationService.class ) );
            } else {

                Toast.makeText( HomeActivity.this, "Permission denied to read your GPS", Toast.LENGTH_SHORT ).show( );
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater( ).inflate( R.menu.home_menu, menu );

        //Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService( Context.SEARCH_SERVICE );
        searchView = (SearchView) menu.findItem( R.id.action_search ).getActionView( );
        searchView.setSearchableInfo( searchManager.getSearchableInfo( getComponentName( ) ) );
        searchView.setMaxWidth( Integer.MAX_VALUE );
        searchView.setOnSearchClickListener( v -> {
            spinner.setVisibility( View.VISIBLE );
        } );
        searchView.setOnCloseListener( () -> {
            spinner.setVisibility( View.GONE );
            return false;
        } );

        // listening to search query text change
        searchView.setOnQueryTextListener( new SearchView.OnQueryTextListener( ) {
            @Override
            public boolean onQueryTextSubmit(String query) {

                Fragment fragment = viewPagerAdapter.getItem( viewPager.getCurrentItem( ) );

                if (fragment instanceof CompletedTasksFragment) {
                    completedTasksFragment = (CompletedTasksFragment) fragment;
                    if (completedTasksFragment != null) {
                        Log.d( TAG, "onQueryTextSubmit: Completed Task Fragment " );
                        completedTasksFragment.beginSearch( query, spinnerSelectedItem );
                    }
                } else if (fragment instanceof IncompleteTasksFragment) {
                    incompleteTasksFragment = (IncompleteTasksFragment) fragment;
                    if (incompleteTasksFragment != null) {
                        Log.d( TAG, "onQueryTextSubmit: Incompleted Task Fragment " );
                        incompleteTasksFragment.beginSearch(query, spinnerSelectedItem);
                    }
                }
                spinner.setVisibility( View.GONE );
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                spinner.setVisibility( View.VISIBLE );
                return false;
            }
        } );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId( );
        if (id == R.id.action_log_out) {
            appPreferences.clear( );
            startActivity( new Intent( this, LogInActivity.class ) );
            finish( );
            return true;
        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified( )) {
            searchView.setIconified( true );
            spinner.setVisibility( View.GONE );
            return;
        }
        if (back_pressed + 1500 > System.currentTimeMillis( )) super.onBackPressed( );
        else
            Toast.makeText( getBaseContext( ), "Press once again to exit!", Toast.LENGTH_SHORT ).show( );
        back_pressed = System.currentTimeMillis( );
    }
}