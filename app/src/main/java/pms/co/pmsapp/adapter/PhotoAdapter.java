package pms.co.pmsapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pms.co.pmsapp.activity.FullScreenActivity;
import pms.co.pmsapp.R;
import pms.co.pmsapp.database.DatabaseHelper;
import pms.co.pmsapp.service.VolleyMultipartRequest;
import pms.co.pmsapp.utils.AppController;
import pms.co.pmsapp.utils.EndPoints;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    //region Variable Declaration
    private String TAG = PhotoAdapter.class.getSimpleName( );
    private ArrayList<String> arrayList;
    private String upload = "false", remarks;
    private String docId;
    private Context context;
    private int index, totalItem, uploadedItem;
    private DatabaseHelper db;
    private String latLong;
    private boolean multiSelect = false;
    private ArrayList<String> selectedItemArray = new ArrayList<String>( );
    private Bitmap bitmap;
    //endregion

    private ActionMode.Callback actionModeCallbacks = new ActionMode.Callback( ) {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            multiSelect = true;
            menu.add( "Delete" );
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            for (String selectedItem : selectedItemArray) {
                deleteFileFromDB( selectedItem );
                File file = new File( selectedItem );
                file.delete( );
                arrayList.remove( selectedItem );
            }
            mode.finish( );
            notifyDataSetChanged( );
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            multiSelect = false;
            selectedItemArray.clear( );
            notifyDataSetChanged( );
        }
    };

    public PhotoAdapter(Context context, ArrayList<String> arrayList, String docId) {
        this.arrayList = arrayList;
        this.context = context;
        this.docId = docId;
        db = new DatabaseHelper( context );
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from( viewGroup.getContext( ) ).inflate( R.layout.row_photo, viewGroup, false );
        ViewHolder viewHolder = new ViewHolder( v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        viewHolder.update( arrayList.get( i ) );
        index = viewHolder.getAdapterPosition();
        upload = checkUpdate( docId, arrayList.get( index ) );
        if(upload.equals( "true" ))
            viewHolder.button.setVisibility( View.GONE );
        else
        viewHolder.button.setOnClickListener( new View.OnClickListener( ) {
            @Override
            public void onClick(View view) {
                viewHolder.button.setVisibility( View.GONE );
                index = viewHolder.getAdapterPosition();
                uploadOnServer( arrayList.get( index ) );
                String newdata = updateDBUploaded( docId, arrayList.get( index ), "true" );
                db.updateImages( docId, newdata);
                getTotalUploads();
                Log.v(TAG, "totalItem: " +totalItem+ "& uploaded:" + uploadedItem);
                sendTotalEntryRepo();
            }
        } );
    }

    @Override
    public int getItemCount() {
        return arrayList.size( );
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageview;
        RelativeLayout relativeLayout;
        Button button;

        public ViewHolder(@NonNull View itemView) {
            super( itemView );
            imageview = (ImageView) itemView.findViewById( R.id.imgPic );
            relativeLayout = (RelativeLayout) itemView.findViewById( R.id.relative_Layout_item );
            relativeLayout.setBackgroundColor( Color.parseColor( "#eaeaea" ) );
            button = (Button) itemView.findViewById( R.id.btn_upload_photo );

        }

        public void selectItem(String item) {
            if (multiSelect) {
                if (selectedItemArray.contains( item )) {
                    selectedItemArray.remove( item );
                    Log.v( TAG, "Selected removed" );
                    relativeLayout.setBackgroundColor( Color.parseColor( "#eaeaea" ) );
                } else {
                    selectedItemArray.add( item );
                    Log.v( TAG, "Selected added" );
                    relativeLayout.setBackgroundColor( Color.LTGRAY );
                }
            }
        }

        void update(final String imagePath) {

            Uri photoUri = Uri.fromFile( new File( imagePath ) );

            Glide.with( context ).load( photoUri ).into( imageview );

            if (selectedItemArray.contains( imagePath )) {
                relativeLayout.setBackgroundColor( Color.LTGRAY );
            } else {
                relativeLayout.setBackgroundColor( Color.parseColor( "#eaeaea" ));
            }
            itemView.setOnLongClickListener( new View.OnLongClickListener( ) {
                @Override
                public boolean onLongClick(View view) {

                    ((AppCompatActivity) view.getContext( )).startSupportActionMode( actionModeCallbacks );
                    selectItem( imagePath );
                    return true;
                }
            } );
            itemView.setOnClickListener( new View.OnClickListener( ) {
                @Override
                public void onClick(View view) {
                    if (multiSelect == false) {
                        Intent intent = new Intent( context, FullScreenActivity.class );
                        intent.putExtra( "photo", imagePath );
                        context.startActivity( intent );
                    } else
                        selectItem( imagePath );
                }
            } );
        }

    }

    public byte[] getFileDataFromDrawable(Bitmap bitmap) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream( );
        bitmap.compress( Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream );
        return byteArrayOutputStream.toByteArray( );
    }

    public String checkUpdate(String docId, String imageName) {
        String upload = "";
        String images = "";
        Cursor cursor = db.fetchdatabase( docId );
        if (cursor.moveToFirst( )) {
            images = cursor.getString( cursor.getColumnIndex( "images" ) );
        }
        try {
            JSONArray jsonArray = new JSONArray( images );
            for (int i = 0; i < jsonArray.length( ); i++) {
                if (jsonArray.getJSONObject( i ).getString( "name" ).equals( imageName ))
                    upload = jsonArray.getJSONObject( i ).getString( "isUploaded" );
            }
        } catch (JSONException e) {
            Log.v( TAG, e.getLocalizedMessage( ) );
        }
        return upload;
    }

    public void uploadOnServer(String imagePath) {

        Log.v(TAG , "upload this" + imagePath);
        remarks = getRemarks( docId );
        latLong = getLocation( docId , imagePath);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options( );
        bitmap = BitmapFactory.decodeFile( imagePath, bmOptions );

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest( Request.Method.POST, EndPoints.UPLOAD_DATA_API,
                new Response.Listener<NetworkResponse>( ) {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        Log.v( TAG, "REsponse:" + new String(response.data) );
                    }
                },
                new Response.ErrorListener( ) {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v( TAG, "VolleyError:" + error.getLocalizedMessage( ) );
                        Toast.makeText( context, error.getLocalizedMessage( ), Toast.LENGTH_LONG ).show( );
                    }
                } ) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>( );
                params.put( "document_id", docId );
                params.put("lat_long", latLong);
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>( );
                long imageName = System.currentTimeMillis( );
                params.put( "image", new DataPart( imageName + ".png", getFileDataFromDrawable( bitmap ) ) );
                return params;
            }
        };
        AppController.getInstance( ).addToRequestQueue( volleyMultipartRequest );

    }

    public String updateDBUploaded(String docId, String imageName, String upload) {
        String update = "";
        Cursor cursor = db.fetchdatabase( docId );
        String images = null;
        if (cursor.moveToFirst( ))
            images = cursor.getString( cursor.getColumnIndex( "images" ) );

        try {
            JSONArray jsonArray = new JSONArray( images );
            for (int i = 0; i < jsonArray.length( ); i++) {
                JSONObject jsonObject = (jsonArray.getJSONObject( i ));
                if (jsonObject.getString( "name" ).equals( imageName )&&jsonObject.getString( "isUploaded" ).equals("false")) {
                    jsonObject.remove("isUploaded");
                    jsonObject.put( "isUploaded", upload );
                }
            }
            update = jsonArray.toString();
        } catch (JSONException e) {
            e.printStackTrace( );
        }

        return update;
    }

    public void deleteFileFromDB(String imageName){
        Cursor cursor = db.fetchdatabase( docId );
        String images = null;
        if (cursor.moveToFirst( ))
            images = cursor.getString( cursor.getColumnIndex( "images" ) );
        try {
            JSONArray jsonArray = new JSONArray( images );
            Log.v(TAG, "json"+ jsonArray.toString());
            for (int i = 0; i < jsonArray.length( ); i++) {
                JSONObject jsonObject = (jsonArray.getJSONObject( i ));
                if (jsonObject.getString( "name" ).equals( imageName )) {
                    jsonArray.remove( i );
                }
            }
            Log.v( TAG, "json"+ jsonArray.toString() );
        } catch (JSONException e) {
            e.printStackTrace( );
        }
    }

    public String getRemarks(String docId){
        String remarks=null;
        Cursor cursor = db.fetchdatabase( docId );
        if (cursor.moveToFirst( )) {
            remarks= cursor.getString( cursor.getColumnIndex( "remarks" ) );
        }
        return remarks;
    }

    public String getLocation(String docId, String imageName){
        String lat_long=null;
        String images= null;
        Cursor cursor = db.fetchdatabase( docId );
        if (cursor.moveToFirst( )) {
            images= cursor.getString( cursor.getColumnIndex( "images" ) );
        }

        try {
            JSONArray jsonArray = new JSONArray( images );
            for (int i = 0; i < jsonArray.length( ); i++) {
                JSONObject jsonObject = (jsonArray.getJSONObject( i ));
                if (jsonObject.getString( "name" ).equals( imageName )) {
                    lat_long = jsonObject.getString( "lat_long");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace( );
        }

        return lat_long;
    }

    public void getTotalUploads(){
        Cursor cursor = db.fetchdatabase( docId );
        String images = null;
        if (cursor.moveToFirst( ))
            images = cursor.getString( cursor.getColumnIndex( "images" ) );
        if (images != null)
            try {
                JSONArray jsonArray1 = new JSONArray( images );
                for (int i = 0; i < jsonArray1.length( ); i++) {
                    totalItem = jsonArray1.length();
                    JSONObject jsonObject = (jsonArray1.getJSONObject( i ));
                    if (!jsonObject.getString( "isUploaded" ).equals("false"))
                        ++uploadedItem;
                }
            } catch (JSONException e) {
                e.printStackTrace( );
            }
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
                    Log.v( TAG, "Response:" + response.toString( ) );
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


}