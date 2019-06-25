package pms.co.pmsapp.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

import pms.co.pmsapp.R;

public class FullScreenActivity extends AppCompatActivity {

    ImageView imageView;
    String photo,photourl,docId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_full_screen );

        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar_fullscreen);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener( new View.OnClickListener( ) {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        photo = getIntent().getStringExtra( "photo" );
        photourl = getIntent().getStringExtra( "photourl" );
        imageView = findViewById( R.id.img_full_screen );

        if(photo!=null) {
            File file = new File( photo );
            imageView.setImageURI( Uri.fromFile( file ) );
        }
        else if(photourl!=null)
            Glide.with(getApplicationContext()).load(photourl).into(imageView);

    }
}
