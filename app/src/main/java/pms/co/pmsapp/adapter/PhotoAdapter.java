package pms.co.pmsapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pms.co.pmsapp.activity.FullScreenActivity;
import pms.co.pmsapp.R;
import pms.co.pmsapp.dao.RoomImagesDao;
import pms.co.pmsapp.database.AppDatabase;
import pms.co.pmsapp.interfaces.ApiInterface;
import pms.co.pmsapp.libs.ApiClient;
import pms.co.pmsapp.model.ResponseImageUpload;
import pms.co.pmsapp.model.ResponseTotalImages;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    //region Variable Declaration
    private String TAG = PhotoAdapter.class.getSimpleName( );
    private List<String> arrayList;
    private String docId;
    private Context context;
    private boolean multiSelect = false;
    private ArrayList<String> selectedItemArray = new ArrayList<>( );
    private RoomImagesDao roomImagesDao;
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
                roomImagesDao.deleteImages( docId, selectedItem );
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

    public PhotoAdapter(Context context, List<String> arrayList, String docId) {
        this.arrayList = arrayList;
        this.context = context;
        this.docId = docId;
        roomImagesDao = AppDatabase.getInstance( context ).roomImagesDao();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from( viewGroup.getContext( ) ).inflate( R.layout.row_photo, viewGroup, false );
        ViewHolder viewHolder = new ViewHolder( v );
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final ViewHolder viewHolder1 = viewHolder;
        final String image = arrayList.get( i );
        viewHolder1.update( image );
        String isUploaded = roomImagesDao.getIsUploaded( docId, image );
        viewHolder1.button.setOnClickListener( view -> {
            Log.v( TAG, "position: " + viewHolder1.getAdapterPosition( ) + "   images: " + image );
            roomImagesDao.updateIsUpload( docId, image, "true" );
            viewHolder1.button.setVisibility( View.GONE );
            sendTotalEntryRepo( );
            uploadOnServer( image );
        });
        if (isUploaded.equals( "true" ))
            viewHolder1.button.setVisibility( View.GONE );
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
                relativeLayout.setBackgroundColor( Color.parseColor( "#eaeaea" ) );
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

    public void uploadOnServer(String imagePath) {
        Log.v( TAG, "upload this" + imagePath );

        String latLong = roomImagesDao.getLatLong( docId, imagePath );
        File file = new File(imagePath);

        Log.v(TAG, roomImagesDao.getLatLong( docId, imagePath ));

        RequestBody mFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("image", file.getName(), mFile);
        RequestBody requestdocId = RequestBody.create( MediaType.parse("text/plain"), docId);
        RequestBody requestLatLong = RequestBody.create( MediaType.parse( "text/plain" ), latLong );

        ApiInterface apiInterface = ApiClient.getRetrofitInstance().create( ApiInterface.class );
        Call<ResponseImageUpload> call = apiInterface.uploadImage( requestdocId, requestLatLong, fileToUpload );
        call.enqueue( new Callback<ResponseImageUpload>( ) {
            @Override
            public void onResponse(Call<ResponseImageUpload> call, Response<ResponseImageUpload> response) {
                ResponseImageUpload responseImageUpload = response.body();
                if(responseImageUpload.getStatus().equals("success"))
                    Log.v(TAG, "Image Uploaded Successfully");
            }
            @Override
            public void onFailure(Call<ResponseImageUpload> call, Throwable t) {
                Log.v(TAG, "Image Upload Failed");
            }
        } );
    }

    public void sendTotalEntryRepo(){
        int totalItem = roomImagesDao.getTotalImagesPath( docId ).size();
        int uploadedItem = roomImagesDao.getUploadedPath( docId, "true" ).size();

        HashMap<String, String> params = new HashMap<>( );
        params.put("total_images", String.valueOf(totalItem));
        params.put("uploaded_images", String.valueOf( uploadedItem ));
        params.put( "document_id", docId );
        ApiInterface apiInterface = ApiClient.getRetrofitInstance().create( ApiInterface.class );
        Call<ResponseTotalImages> call = apiInterface.totalImages(params);
        call.enqueue( new Callback<ResponseTotalImages>( ) {
            @Override
            public void onResponse(Call<ResponseTotalImages> call, retrofit2.Response<ResponseTotalImages> response) {
                ResponseTotalImages responseTotalImages = response.body();
                if(responseTotalImages.getStatus().equals( "success" )){
                    Log.d( TAG, "Total Images: success" );
                }
            }
            @Override
            public void onFailure(Call<ResponseTotalImages> call, Throwable t) {
                Log.d( TAG, "Total Images: error" );
            }
        } );
    }

}