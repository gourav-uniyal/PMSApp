package pms.co.pmsapp.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.watermark.androidwm_light.WatermarkBuilder;
import com.watermark.androidwm_light.bean.WatermarkText;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import id.zelory.compressor.Compressor;
import pms.co.pmsapp.R;
import pms.co.pmsapp.adapter.PhotoAdapter;
import pms.co.pmsapp.dao.RoomImagesDao;
import pms.co.pmsapp.database.AppDatabase;
import pms.co.pmsapp.interfaces.ApiInterface;
import pms.co.pmsapp.libs.ApiClient;
import pms.co.pmsapp.model.ResponseTotalImages;
import pms.co.pmsapp.model.ResponseVerification;
import pms.co.pmsapp.model.RoomImages;
import pms.co.pmsapp.utils.RecyclerViewItemDecorator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoActivity extends AppCompatActivity {

    //region Variable Declaration
    private static String folderpath;
    private String TAG = PhotoActivity.class.getSimpleName( );
    private int count = 1;
    private static final int Request_Camera_Code = 1;
    private PhotoAdapter photoAdapter;
    private RecyclerView rvPhotos;
    private String docId;
    private String path;
    private String formpath;
    private String verifier;
    private File actualFile;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private ProgressBar progressBar;
    private RoomImagesDao roomImagesDao;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_photo );

        //region StrictMode
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder( );
        StrictMode.setVmPolicy( builder.build( ) );
        //endregion

        verifier = getIntent( ).getStringExtra( "verifier" );

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient( getApplicationContext( ) );

        roomImagesDao = AppDatabase.getInstance( getApplicationContext( ) ).roomImagesDao( );

        //region TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar_photo );
        setSupportActionBar( toolbar );
        toolbar.setNavigationIcon( R.drawable.ic_arrow_back_black_24dp );
        toolbar.setNavigationOnClickListener( view -> finish( ) );
        //endregion

        createfolder( );

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
        rvPhotos = findViewById( R.id.rvPhotos );
        //endregion

        //region Intent GetData
        docId = getIntent( ).getStringExtra( "document_id" );
        formpath = getIntent( ).getStringExtra( "form_path" );
        String fileId = getIntent( ).getStringExtra( "fileId" );
        path = getIntent( ).getStringExtra( "path" );
        String status = getIntent( ).getStringExtra( "status" );
        //endregion

        if (status.equals( "complete" ))
            btnVerifComplete.setVisibility( View.GONE );

        path = "https://pmsapp.co.in/" + path;
        formpath = "https://pmsapp.co.in/" + formpath;

        lblfileId.setText( fileId );

        sendTotalEntryRepo( );

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
        lblAddPhotos.setOnClickListener( view -> {
            Intent camera = new Intent( );
            camera.setAction( MediaStore.ACTION_IMAGE_CAPTURE );
            if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                Intent cameraIntent = new Intent( android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
                String newPicFile = docId + "_" + System.currentTimeMillis( ) + ".jpg";
                actualFile = new File( folderpath + "/" + newPicFile );
                cameraIntent.putExtra( MediaStore.EXTRA_OUTPUT, Uri.fromFile( actualFile ) );
                startActivityForResult( cameraIntent, Request_Camera_Code );
            }
            else {
                if (camera.resolveActivity( getPackageManager( ) ) != null) {
                    String newPicFile = docId + "_" + System.currentTimeMillis( ) + ".jpg";
                    actualFile = new File( folderpath + "/" + newPicFile );
                    camera.putExtra( MediaStore.EXTRA_OUTPUT, Uri.fromFile( actualFile ) );
                    startActivityForResult( camera, Request_Camera_Code );
                }
            }
        } );
        //endregion

        //region Add Remark ClickListener
        lblAddRemarks.setOnClickListener( view -> {
            Intent intent = new Intent( getApplicationContext( ), AddRemarkActivity.class );
            intent.putExtra( "document_id", docId );
            intent.putExtra( "count", count );
            startActivity( intent );
        } );
        //endregion

        //region Verification Completed Button
        btnVerifComplete.setOnClickListener( v -> {
            progressBar.setVisibility( View.VISIBLE );
            if (roomImagesDao.getTotalImagesPath( docId ) == null) {
                progressBar.setVisibility( View.GONE );
                Toast.makeText( getApplicationContext( ), "No Data Found", Toast.LENGTH_SHORT ).show( );
            } else {
                int flag = roomImagesDao.getUploadedPath( docId, "false" ).size( );
                if (flag > 0) {
                    Toast.makeText( getApplicationContext( ), "Upload All Task First", Toast.LENGTH_SHORT ).show( );
                    progressBar.setVisibility( View.GONE );
                } else if (flag == 0) {
                    HashMap<String, String> params = new HashMap<>( );
                    params.put( "verifier", verifier );
                    params.put( "document_id", docId );
                    ApiInterface apiInterface = ApiClient.getRetrofitInstance( ).create( ApiInterface.class );
                    Call<ResponseVerification> call = apiInterface.verification( params );
                    call.enqueue( new Callback<ResponseVerification>( ) {
                        @Override
                        public void onResponse(Call<ResponseVerification> call, Response<ResponseVerification> response) {
                            ResponseVerification responseVerification = response.body( );
                            if (responseVerification.getStatus( ).equals( "success" )) {
                                progressBar.setVisibility( View.GONE );
                                Toast.makeText( getApplicationContext( ), "Verification Completed", Toast.LENGTH_SHORT ).show( );
                                Intent intent = new Intent( getApplicationContext( ), HomeActivity.class );
                                intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
                                intent.putExtra( "verifier", verifier );
                                startActivity( intent );
                                finish( );
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseVerification> call, Throwable t) {
                        }
                    } );
                }
            }
        } );
        //endregion

    }

    public void sendTotalEntryRepo() {
        int totalItem = roomImagesDao.getTotalImagesPath( docId ).size( );
        int uploadedItem = roomImagesDao.getUploadedPath( docId, "true" ).size( );

        HashMap<String, String> params = new HashMap<>( );
        params.put( "total_images", String.valueOf( totalItem ) );
        params.put( "uploaded_images", String.valueOf( uploadedItem ) );
        params.put( "document_id", docId );
        ApiInterface apiInterface = ApiClient.getRetrofitInstance( ).create( ApiInterface.class );
        Call<ResponseTotalImages> call = apiInterface.totalImages( params );
        call.enqueue( new Callback<ResponseTotalImages>( ) {
            @Override
            public void onResponse(Call<ResponseTotalImages> call, retrofit2.Response<ResponseTotalImages> response) {
                ResponseTotalImages responseTotalImages = response.body( );
                if (responseTotalImages.getStatus( ).equals( "success" )) {
                    Log.d( TAG, "Total Images: success" );
                }
            }

            @Override
            public void onFailure(Call<ResponseTotalImages> call, Throwable t) {
                Log.d( TAG, "Total Images: error" );
            }
        } );
    }

    void fetchData() {
        roomImagesDao.getAllImagesPath( docId ).observe( this, (List<String> list) -> {
            photoAdapter = new PhotoAdapter( this, list, docId );
            rvPhotos.setAdapter( photoAdapter );
        } );
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
                latLong = "lat: " + latitude + ", long: " + longitude;
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

                    //endregion

                    BitmapFactory.Options bmOptions = new BitmapFactory.Options( );
                    Bitmap bitmap = BitmapFactory.decodeFile( compressedImage.getAbsolutePath( ), bmOptions );

                    SimpleDateFormat sdf = new SimpleDateFormat( "dd.MM.yyyy '('HH:mm:ss')' " );
                    String currentDateandTime = sdf.format( new Date( ) );

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
                            .setPositionY( ((watermarkText.getTextSize( )) * 0.01) / 3 )
                            .setTextAlpha( 255 )
                            .setTextColor( Color.YELLOW )
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

                    RoomImages roomImages = new RoomImages( );
                    roomImages.setDocId( docId );
                    roomImages.setIsUploaded( "false" );
                    roomImages.setPath( watermarkImgFile.getAbsolutePath( ) );
                    roomImages.setLat_long( storeCurrentLatLong );

                    roomImagesDao.insert( roomImages );

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

    @Override
    public void onBackPressed() {
        finish( );
    }
}