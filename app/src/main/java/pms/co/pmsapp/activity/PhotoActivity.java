package pms.co.pmsapp.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.watermark.androidwm_light.WatermarkBuilder;
import com.watermark.androidwm_light.bean.WatermarkPosition;
import com.watermark.androidwm_light.bean.WatermarkText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.zelory.compressor.Compressor;
import pms.co.pmsapp.R;
import pms.co.pmsapp.adapter.PhotoAdapter;
import pms.co.pmsapp.database.DatabaseHelper;
import pms.co.pmsapp.utils.AppController;
import pms.co.pmsapp.utils.EndPoints;
import pms.co.pmsapp.utils.RecyclerViewItemDecorator;

public class PhotoActivity extends AppCompatActivity {

    //region Variable Declaration
    private static String folderpath;
    private String TAG = PhotoActivity.class.getSimpleName( );
    private DatabaseHelper db;
    private int count = 1;
    private static final int Request_Camera_Code = 1;
    private ArrayList<String> arrayList;
    private PhotoAdapter photoAdapter;
    private RecyclerView rvPhotos;
    private String docId;
    private String path;
    private String formpath;
    private String verifier;
    private JSONArray jsonArray;
    private File  actualFile;
    private int totalItem=0, uploadedItem=0;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private ProgressBar progressBar;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_photo );

        //region StrictMode
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder( );
        StrictMode.setVmPolicy( builder.build( ) );
        //endregion

        verifier = getIntent().getStringExtra("verifier");

        Log.v(TAG, verifier);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient( getApplicationContext( ) );

        //region TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar_photo );
        setSupportActionBar( toolbar );
        toolbar.setNavigationIcon( R.drawable.ic_arrow_back_black_24dp );
        toolbar.setNavigationOnClickListener( new View.OnClickListener( ) {
            @Override
            public void onClick(View view) {
                finish();
            }
        } );
        //endregion

        createfolder( );

        db = new DatabaseHelper( this );

        jsonArray = new JSONArray( );

        //region View ID's
        TextView lblAddPhotos = findViewById( R.id.lbl_add_photos );
        lblAddPhotos.setPaintFlags( lblAddPhotos.getPaintFlags( ) | Paint.UNDERLINE_TEXT_FLAG );
        TextView lblAddRemarks = findViewById( R.id.lbl_add_remarks );
        lblAddRemarks.setPaintFlags( lblAddRemarks.getPaintFlags( ) | Paint.UNDERLINE_TEXT_FLAG );
        TextView lblfileId = findViewById( R.id.lbl_photo_file_id );
        progressBar = findViewById( R.id.progress_bar_photo );
        ImageView imgPath = findViewById( R.id.img_path );
        ImageView imgFormPath = findViewById( R.id.img_form_path );
        Button btnVerifComplete = findViewById( R.id.btn_photo_verification_completed );
        //endregion

        //region Intent GetData
        rvPhotos = findViewById( R.id.rvPhotos );
        docId = getIntent( ).getStringExtra( "document_id" );
        formpath = getIntent( ).getStringExtra( "form_path" );
        String fileId = getIntent( ).getStringExtra( "fileId" );
        path = getIntent( ).getStringExtra( "path" );
        String status = getIntent().getStringExtra( "status" );
        //endregion

        if(status.equals( "complete" ))
            btnVerifComplete.setVisibility( View.GONE );

        path = "https://pmsapp.co.in/" + path;
        formpath = "https://pmsapp.co.in/" + formpath;

        lblfileId.setText( fileId );

        arrayList = new ArrayList<>( );

        Cursor cursor = db.fetchdatabase( docId );
        String images = null;
        if (cursor.moveToFirst( ))
            images = cursor.getString( cursor.getColumnIndex( "images" ) );
        if (images != null)
            try {
                JSONArray jsonArray1 = new JSONArray( images );
                String s;
                totalItem = jsonArray1.length();
                for (int i = 0; i < jsonArray1.length( ); i++) {
                    JSONObject jsonObject = (jsonArray1.getJSONObject( i ));
                    s = jsonObject.getString( "isUploaded" );
                    if (s.equals("true"))
                        uploadedItem++;
                }
            } catch (JSONException e) {
                e.printStackTrace( );
            }

        Log.v(TAG, "totalItem: " +totalItem+ "& uploaded:" + uploadedItem);

        sendTotalEntryRepo();

        fetchData( );

        //region RecyclerView Grid Layout Manager
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager( getApplicationContext( ), 3 );
        int spaceInPixels = 13;
        rvPhotos.addItemDecoration( new RecyclerViewItemDecorator( spaceInPixels ) );
        rvPhotos.setItemAnimator( new DefaultItemAnimator( ) );
        rvPhotos.setLayoutManager( layoutManager );
        //endregion

        //region Path Setup
        if (path.contains( ".jpg" ) || path.contains( ".png" ) || path.contains( ".gif" ) || path.contains( ".jpeg" )) {
            Glide.with( getApplicationContext( ) ).load( path ).into( imgPath );
        }

        imgPath.setOnClickListener( new View.OnClickListener( ) {
            @Override
            public void onClick(View view) {

                if (path.contains( ".pdf" )) {
                    Intent intent = new Intent( getApplicationContext( ), PdfActivity.class );
                    intent.putExtra( "pdfurl", path );
                    startActivity( intent );
                } else if (path.contains( ".jpg" ) || path.contains( ".png" ) || path.contains( ".gif" ) || path.contains( ".jpeg" ) || path.contains( ".TIF" ) || path.contains( ".TIFF" )) {
                    Intent intent = new Intent( getApplicationContext( ), FullScreenActivity.class );
                    intent.putExtra( "photourl", path );
                    startActivity( intent );
                }
            }
        } );
        //endregion

        //region FormPath Setup
        if (formpath.contains( ".jpg" ) || formpath.contains( ".png" ) || formpath.contains( ".gif" ) || formpath.contains( ".jpeg" ) || formpath.contains( ".TIF" ) || formpath.contains( ".TIFF" )) {
            Glide.with( getApplicationContext( ) ).load( formpath ).into( imgFormPath );
        }

        imgFormPath.setOnClickListener( new View.OnClickListener( ) {
            @Override
            public void onClick(View view) {

                if (formpath.contains( ".pdf" )) {
                    Intent intent = new Intent( getApplicationContext( ), PdfActivity.class );
                    intent.putExtra( "pdfurl", formpath );
                    startActivity( intent );
                } else if (formpath.contains( ".jpg" ) || formpath.contains( ".png" ) || formpath.contains( ".gif" ) || formpath.contains( ".jpeg" ) || formpath.contains( ".TIF" ) || formpath.contains( ".TIFF" )) {
                    Intent intent = new Intent( getApplicationContext( ), FullScreenActivity.class );
                    intent.putExtra( "photourl", formpath );
                    startActivity( intent );
                }
            }
        } );
        //endregion

        //region Add Photo ClickListener
        lblAddPhotos.setOnClickListener( new View.OnClickListener( ) {
            @Override
            public void onClick(View view) {
                Intent camera = new Intent( );
                camera.setAction( MediaStore.ACTION_IMAGE_CAPTURE );
                if (camera.resolveActivity( getPackageManager( ) ) != null) {
                    String newPicFile = docId + "_" + System.currentTimeMillis( ) + ".jpg";
                    actualFile = new File( folderpath + "/" + newPicFile );
                    if (actualFile != null) {
                        camera.putExtra( MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile( actualFile ) );
                        startActivityForResult( camera, Request_Camera_Code );
                    }
                }
            }
        } );
        //endregion

        //region Add Remark ClickListener
        lblAddRemarks.setOnClickListener( new View.OnClickListener( ) {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( getApplicationContext( ), AddRemarkActivity.class );
                intent.putExtra( "document_id", docId );
                intent.putExtra( "count", count );
                startActivity( intent );
            }


        } );
        //endregion

        //region Verification Completed Button
        btnVerifComplete.setOnClickListener( new View.OnClickListener( ) {
            @Override
            public void onClick(View v) {
                Cursor cursor = db.fetchdatabase( docId );
                String images = null;
                int flag = 0;
                if (cursor.moveToFirst( ))
                    images = cursor.getString( cursor.getColumnIndex( "images" ) );
                if(images==null){
                    Toast.makeText( getApplicationContext(), "No Data Found", Toast.LENGTH_SHORT ).show();
                }
                else{
                try {
                    JSONArray jsonArray = new JSONArray( images );
                    for (int i = 0; i < jsonArray.length( ); i++) {
                        JSONObject jsonObject = (jsonArray.getJSONObject( i ));
                        if (jsonObject.getString( "isUploaded" ).equals("false")) {
                            flag = 1;
                            break;
                        }
                    }
                    if(flag == 1)
                        Toast.makeText( getApplicationContext( ), "Upload All Task First", Toast.LENGTH_SHORT ).show( );
                    else {
                        Map<String, String> params = new HashMap<>( );
                        params.put( "verifier", verifier );
                        params.put( "document_id", docId );
                        JSONObject jsonObject1 = new JSONObject( params );
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest( Request.Method.POST, EndPoints.VERIFICATION_COMPLETED, jsonObject1, new Response.Listener<JSONObject>( ) {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String status;
                                    Log.v( TAG, "Verifiation Response:" + response.toString( ) );
                                    status = response.getString( "status" );
                                    if (status.equals( "success" )) {
                                        Toast.makeText( getApplicationContext( ), "Verification Completed", Toast.LENGTH_SHORT ).show( );
                                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                        intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
                                        intent.putExtra( "verifier", verifier);
                                        startActivity(intent);
                                        finish();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace( );
                                }
                            }
                        }, new Response.ErrorListener( ) {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.v( TAG, "VolleyError: " + error.getLocalizedMessage( ) );
                            }
                        } );
                        AppController.getInstance( ).addToRequestQueue( jsonObjectRequest );
                    }
                } catch (JSONException e) {
                    e.printStackTrace( );
                }
            }}
        } );
        //endregion

    }

    public void sendTotalEntryRepo(){
        Map<String, String> params = new HashMap<>( );
        params.put("total_images", String.valueOf(totalItem));
        params.put("uploaded_images", String.valueOf( uploadedItem ));
        params.put( "document_id", docId );
        JSONObject jsonObject1 = new JSONObject( params );
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest( Request.Method.POST, EndPoints.TOTAL_IMAGES, jsonObject1, new Response.Listener<JSONObject>( ) {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String status;
                    Log.v( TAG, "Total Entry Response:" + response.toString( ) );
                    status = response.getString( "status" );
                    if (status.equals( "success" )) {
                        Log.v(TAG, "totalentry status: "+ status);
                    }
                } catch (JSONException e) {
                    e.printStackTrace( );
                }
            }
        }, new Response.ErrorListener( ) {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v( TAG, "VolleyError: " + error.getLocalizedMessage( ) );
            }
        } );
        AppController.getInstance( ).addToRequestQueue( jsonObjectRequest );
    }

    private LocationCallback mLocationCallback = new LocationCallback( ) {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            double latitude;
            double longitude;
            String latLong;
            List<Location> locationList = locationResult.getLocations( );
            if (locationList.size( ) > 0) {
                Location location = locationList.get( locationList.size( ) - 1 );
                longitude = location.getLongitude( );
                latitude = location.getLatitude( );
                latLong = "lat: "+latitude + ", long: " + longitude;
                String storeCurrentLatLong = latLong;

                if (storeCurrentLatLong != null) {

                    Log.v( TAG, storeCurrentLatLong );
                    File compressedImage;

                    //region CompressImage And Save in Storage
                    compressedImage = new Compressor.Builder( getApplicationContext( ) )
                            .setMaxWidth( 1080 )
                            .setMaxHeight( 1080 )
                            .setQuality( 100 )
                            .setCompressFormat( Bitmap.CompressFormat.JPEG )
                            .setDestinationDirectoryPath( folderpath )
                            .build( )
                            .compressToFile( actualFile );

                    Log.v( TAG, compressedImage.getAbsolutePath( ) );
                    //endregion

                    BitmapFactory.Options bmOptions = new BitmapFactory.Options( );
                    Bitmap bitmap = BitmapFactory.decodeFile( compressedImage.getAbsolutePath( ), bmOptions );

                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy '('HH:mm:ss')' ");
                    String currentDateandTime = sdf.format(new Date());

                    //region WaterMark Implementation
                    WatermarkText watermarkText, watermarkText1;
                    File watermarkImgFile;
                    watermarkText = new WatermarkText( storeCurrentLatLong )
                            .setPositionX( 0 )
                            .setPositionY( 0 )
                            .setTextAlpha( 255 )
                            .setTextColor( Color.YELLOW )
                            .setRotation( 0 )
                            .setTextSize( 10 );

                    bitmap = WatermarkBuilder
                            .create( getApplicationContext( ), bitmap )
                            .loadWatermarkText( watermarkText )
                            .getWatermark( )
                            .getOutputImage( );

                    watermarkText1 = new WatermarkText( currentDateandTime )
                            .setPositionX( 0 )
                            .setPositionY(((watermarkText.getTextSize())*0.01)/3)
                            .setTextAlpha( 255 )
                            .setTextColor( Color.YELLOW)
                            .setRotation( 0 )
                            .setTextSize( 10 );

                    bitmap = WatermarkBuilder
                            .create( getApplicationContext( ), bitmap )
                            .loadWatermarkText( watermarkText1 )
                            .getWatermark( )
                            .getOutputImage( );
                    //endregion

                    //region Save Watermark Image File
                    watermarkImgFile = new File( actualFile.getAbsolutePath( ) );
                    OutputStream os;
                    try {
                        os = new FileOutputStream( watermarkImgFile );
                        bitmap.compress( Bitmap.CompressFormat.JPEG, 100, os );
                        os.flush( );
                        os.close( );
                    } catch (Exception e) {
                        Log.v( TAG, "error on creating file" );
                    }
                    //endregion

                    compressedImage.delete( );

                    arrayList.add( watermarkImgFile.getAbsolutePath( ) );

                    //region Fetch Images From Sqlite DB
                    Cursor cursor = db.fetchdatabase( docId );
                    String images = null;
                    if (cursor.moveToFirst( ))
                        images = cursor.getString( cursor.getColumnIndex( "images" ) );
                    if (images != null)
                        try {
                            JSONArray jsonArray1 = new JSONArray( images );
                            for (int i = 0; i < jsonArray1.length( ); i++) {
                                JSONObject jsonObject = jsonArray1.getJSONObject( i );
                                jsonArray.put( jsonObject );
                            }
                        } catch (JSONException e) {
                            e.printStackTrace( );
                        }
                    //endregion

                    Map<String, String> map = new HashMap<>( );
                    map.put( "name", watermarkImgFile.getAbsolutePath( ) );
                    map.put( "isUploaded", "false" );
                    map.put( "lat_long", storeCurrentLatLong );

                    JSONObject jsonObject = new JSONObject( map );

                    jsonArray.put( jsonObject );

                    if (count == 0) {
                        db.updateImages( docId, jsonArray.toString( ) );
                    } else {
                        db.insertImages( docId, jsonArray.toString( ) );
                        count = 0;
                    }

                    photoAdapter.notifyDataSetChanged( );

                    progressBar.setVisibility( View.GONE );

                } else Log.v( TAG, "not is getting location" );
            }
        }
    };


    @Override
    protected void onStart() {
        super.onStart( );
        locationRequest = new LocationRequest( );
        locationRequest.setFastestInterval( 120000 );
        locationRequest.setPriority( LocationRequest.PRIORITY_HIGH_ACCURACY );
    }

    @Override
    protected void onResume() {
        super.onResume( );
        locationRequest = new LocationRequest( );
        locationRequest.setInterval( 0 ); // twenty second interval
        locationRequest.setFastestInterval( 120000 );
        locationRequest.setPriority( LocationRequest.PRIORITY_HIGH_ACCURACY );
    }

    @Override
    protected void onStop() {
        super.onStop( );
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates( mLocationCallback );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );

        if (requestCode == Request_Camera_Code && resultCode == Activity.RESULT_OK) {

            if (ActivityCompat.checkSelfPermission( getApplicationContext( ), Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( getApplicationContext( ), Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
                progressBar.setVisibility( View.VISIBLE );
                fusedLocationProviderClient.requestLocationUpdates( locationRequest, mLocationCallback, Looper.myLooper( ) );
            }
            }
        }

    void fetchData() {
        count = 1;
        File directory = new File( folderpath );
        File[] files = directory.listFiles( );
        for (File file : files) {
            File imgFile = new File( folderpath + "/" + file.getName( ) );
            if (imgFile.exists( ))
                if (imgFile.toString( ).contains( docId + "_" )) {
                    count = 0;
                    arrayList.add( imgFile.toString( ) );
                }
        }
        photoAdapter = new PhotoAdapter( this, arrayList, docId );
        rvPhotos.setAdapter( photoAdapter );
    }

    void createfolder() {
        boolean success = true;
        File folder = new File( Environment.getExternalStorageDirectory( ) + File.separator + "PMS App" );
        if (!folder.exists( )) {
            success = folder.mkdirs( );
        }

        if (success)
            folderpath = folder.getAbsolutePath( );
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
