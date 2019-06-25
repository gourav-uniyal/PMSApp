package pms.co.pmsapp.activity;

import android.Manifest;
import android.annotation.TargetApi;
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
import android.nfc.Tag;
import android.os.Build;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.zelory.compressor.Compressor;
import pms.co.pmsapp.R;
import pms.co.pmsapp.adapter.PhotoAdapter;
import pms.co.pmsapp.database.DatabaseHelper;
import pms.co.pmsapp.utils.RecyclerViewItemDecorator;

public class PhotoActivity extends AppCompatActivity {

    //region Variable Declaration
    private static String folderpath;
    private String TAG = PhotoActivity.class.getSimpleName();
    private DatabaseHelper db;
    private int count = 1 ;
    private TextView lblfileId;
    private static final int Request_Camera_Code = 1;
    private ArrayList<String> arrayList;
    private PhotoAdapter photoAdapter;
    private RecyclerView rvPhotos;
    private String docId, path, formpath, fileId;
    private JSONArray jsonArray;
    private File compressedImage, actualFile;
    private TextView lblAddPhotos, lblAddRemarks;
    private ImageView imgPath, imgFormPath;
    private Bitmap bitmap;
    //watermark
    private WatermarkText watermarkText;
    private File watermarkImgFile;
    private int width, height;
    //lat long
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private double latitude;
    private double longitude;
    private String latLong;
    private String storeCurrentLatLong;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_photo );

        //region StrictMode
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder( );
        StrictMode.setVmPolicy( builder.build( ) );
        //endregion

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        //region TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar_photo );
        setSupportActionBar( toolbar );
        toolbar.setNavigationIcon( R.drawable.ic_arrow_back_black_24dp );
        toolbar.setNavigationOnClickListener( new View.OnClickListener( ) {
            @Override
            public void onClick(View view) {
                finish( );
            }
        } );
        //endregion



        createfolder( );

        db = new DatabaseHelper( this );

        jsonArray = new JSONArray(  );

        //region TextView ID's
        lblAddPhotos = findViewById( R.id.lbl_add_photos );
        lblAddPhotos.setPaintFlags( lblAddPhotos.getPaintFlags( ) | Paint.UNDERLINE_TEXT_FLAG );
        lblAddRemarks = findViewById( R.id.lbl_add_remarks );
        lblAddRemarks.setPaintFlags( lblAddRemarks.getPaintFlags( ) | Paint.UNDERLINE_TEXT_FLAG );
        lblfileId = findViewById( R.id.lbl_photo_file_id );
        //endregion

        imgPath = findViewById( R.id.img_path );
        imgFormPath = findViewById( R.id.img_form_path );

        //region Intent GetData
        rvPhotos = findViewById( R.id.rvPhotos );
        docId = getIntent( ).getStringExtra( "document_id" );
        formpath = getIntent( ).getStringExtra( "form_path" );
        fileId = getIntent( ).getStringExtra( "fileId" );
        path  = getIntent().getStringExtra( "path" );
        //endregion

        path = "https://pmsapp.co.in/" + path;
        formpath= "https://pmsapp.co.in/" + formpath;

        lblfileId.setText( fileId );

        arrayList = new ArrayList<>( );

        fetchData( );

        //region RecyclerView Grid Layout Manager
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager( getApplicationContext( ), 3 );
        int spaceInPixels = 13;
        rvPhotos.addItemDecoration( new RecyclerViewItemDecorator(spaceInPixels) );
        rvPhotos.setItemAnimator( new DefaultItemAnimator() );
        rvPhotos.setLayoutManager( layoutManager );
        //endregion

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

        lblAddPhotos.setOnClickListener( new View.OnClickListener( ) {
            @Override
            public void onClick(View view) {
                Intent camera = new Intent( );
                camera.setAction( MediaStore.ACTION_IMAGE_CAPTURE);
                if (camera.resolveActivity( getPackageManager( ) ) != null) {

                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        fusedLocationProviderClient.requestLocationUpdates( locationRequest, mLocationCallback, Looper.myLooper( ) );
                    }

                    String newPicFile = docId+"_" + System.currentTimeMillis( ) + ".jpg";
                    actualFile = new File( folderpath + "/" + newPicFile );
                    if (actualFile != null) {
                        camera.putExtra( MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile( actualFile ) );
                        startActivityForResult( camera, Request_Camera_Code );
                    }

                }
            }
        } );

        lblAddRemarks.setOnClickListener( new View.OnClickListener( ) {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( getApplicationContext( ), AddRemarkActivity.class );
                intent.putExtra( "document_id", docId );
                intent.putExtra( "count", count );
                startActivity( intent );
            }


        } );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Log.v(TAG, "reached to location callback");
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                Location location = locationList.get(locationList.size() - 1);
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                Log.v(TAG , "data "+ latitude  +" longitude:" + longitude);
                latLong = latitude + ", " + longitude;
                Log.v(TAG, " inserted "+latLong);
                storeCurrentLatLong = latLong;
                Log.v( TAG, storeCurrentLatLong );

            } else Log.v(TAG, "not is getting location");
        }
    };


    @Override
    protected void onStart() {
        super.onStart( );
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10*000); // twenty second interval
        locationRequest.setFastestInterval(120000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @Override
    protected void onStop() {
        super.onStop( );
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );

        if (requestCode == Request_Camera_Code && resultCode == Activity.RESULT_OK) {

            compressedImage = new Compressor.Builder( this )
                    .setMaxWidth( 1080 )
                    .setMaxHeight( 1080 )
                    .setQuality( 100 )
                    .setCompressFormat( Bitmap.CompressFormat.JPEG )
                    .setDestinationDirectoryPath( folderpath )
                    .build()
                    .compressToFile(actualFile);


            bitmap = BitmapFactory.decodeFile(compressedImage.getAbsolutePath());
            Log.v(TAG, "height:"+ bitmap.getHeight() + "widht:" +bitmap.getWidth());
            bitmap.getWidth();
            bitmap.getHeight();

            Log.v(TAG, "height:"+ height + "width:" +width);


            double position_x =  0;
            double position_y =0;

            WatermarkPosition watermarkPosition = new WatermarkPosition(position_x, position_y);

            watermarkText = new WatermarkText(storeCurrentLatLong)
                    .setPosition(watermarkPosition)
                    .setTextAlpha( 255 )
                    .setTextColor( Color.WHITE)
                    .setBackgroundColor( Color.BLACK )
                    .setRotation(0)
                    .setTextSize(10);


            bitmap = WatermarkBuilder
                    .create(getApplicationContext(), bitmap)
                    .loadWatermarkText(watermarkText)
                    .getWatermark()
                    .getOutputImage();

            watermarkImgFile = new File( actualFile.getAbsolutePath() );

            OutputStream os;
            try {
                os = new FileOutputStream(watermarkImgFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                os.flush();
                os.close();
            } catch (Exception e) {
                Log.v(TAG, "error on creating file");
            }

            compressedImage.delete();

            arrayList.add( watermarkImgFile.getAbsolutePath() );

            Cursor cursor = db.fetchdatabase( docId );
            String images = null;
            if (cursor.moveToFirst( ))
                images = cursor.getString( cursor.getColumnIndex( "images" ) );
            if(images !=null)
            try {
                JSONArray jsonArray1 = new JSONArray( images );
                for(int i =0;i<jsonArray1.length();i++){
                    JSONObject jsonObject = jsonArray1.getJSONObject( i );
                    jsonArray.put(jsonObject);
                }
                Log.v(TAG , "Json Array PRevious:" +jsonArray);
            } catch (JSONException e) {
                e.printStackTrace( );
            }

            Map<String ,String> map = new HashMap<>();
            map.put("name", watermarkImgFile.getAbsolutePath());
            map.put("isUploaded", "false");
            map.put("lat_long", storeCurrentLatLong);

            JSONObject jsonObject = new JSONObject( map );

            jsonArray.put( jsonObject );

            if (count == 0) {
                db.updateImages( docId, jsonArray.toString( ));
            }

            else {
                db.insertImages( docId, jsonArray.toString( ));
                count = 0;
            }
            Log.v(TAG, "JsonArray New Stored"+jsonArray.toString());


            photoAdapter.notifyDataSetChanged( );
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
                    arrayList.add(imgFile.toString());
                }
        }
        photoAdapter = new PhotoAdapter( this, arrayList, docId);
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

    protected void exportDb() {

        File dataDirectory = Environment.getDataDirectory();

        FileChannel source = null;
        FileChannel destination = null;

        String currentDBPath = "/data/" + getApplicationContext().getApplicationInfo().packageName + "/databases/pms_db";
        String backupDBPath = "PMS.sqlite";

        File currentDB = new File(dataDirectory, currentDBPath);
        File backupDB = new File(folderpath, backupDBPath);

        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());

            Toast.makeText(this, "DB Exported!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (source != null) source.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (destination != null) destination.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File convertBitmap(Bitmap bitmap, String filename) {

        File imageFile = new File(filename);


        return  imageFile;
    }



}
