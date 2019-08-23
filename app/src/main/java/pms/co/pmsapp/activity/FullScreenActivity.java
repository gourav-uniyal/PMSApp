package pms.co.pmsapp.activity;

import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;

import pms.co.pmsapp.R;

public class FullScreenActivity extends AppCompatActivity {

    PhotoView photoView;
    String photo,photourl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_full_screen );

        //region Toolbar Setup
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar_fullscreen);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener( new View.OnClickListener( ) {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        //endregion

        photoView = findViewById( R.id.photo_view_full_screen );

        photo = getIntent().getStringExtra( "photo" );
        photourl = getIntent().getStringExtra( "photourl" );

        if(photo!=null) {
            File file = new File( photo );
            photoView.setImageURI( Uri.fromFile( file ) );
        }
        else if(photourl!=null)
            Glide.with(getApplicationContext()).load(photourl).into(photoView);

    }
}
