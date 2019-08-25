package pms.co.pmsapp.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;
import pms.co.pmsapp.R;
import pms.co.pmsapp.dao.RoomColumnDao;
import pms.co.pmsapp.database.AppDatabase;
import pms.co.pmsapp.interfaces.ApiInterface;
import pms.co.pmsapp.libs.ApiClient;
import pms.co.pmsapp.model.ResponseRemarks;
import pms.co.pmsapp.model.RoomColumn;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddRemarkActivity extends Activity implements View.OnClickListener {

    //region Variable Declaration
    private EditText txtRemarks;
    private String TAG = AddRemarkActivity.class.getSimpleName( );
    private String docId;
    private RoomColumnDao roomColumnDao;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_add_remark );

        docId = getIntent( ).getStringExtra( "document_id" );
        roomColumnDao = AppDatabase.getInstance( getApplicationContext() ).roomColumnDao();

        TextView lblSaveRemark = findViewById( R.id.lbl_save_remark );
        txtRemarks = findViewById( R.id.txt_remark );

        lblSaveRemark.setOnClickListener( this );
    }

    @Override
    public void onClick(View view) {
        switch (view.getId( )) {
            case R.id.lbl_save_remark:
                String remarks = txtRemarks.getText( ).toString( );
                RoomColumn roomColumn = new RoomColumn();
                roomColumn.setDocId( docId );
                roomColumn.setRemarks( remarks );
                roomColumnDao.insert( roomColumn );
                uploadRemark( docId, remarks );
                Log.v( TAG, "Remarks added" );
                finish( );
        }
    }

    public void uploadRemark(String docId, final String remarks) {

        HashMap<String, String> params = new HashMap<>( );
        params.put( "document_id", docId );
        params.put( "remarks", remarks );

        ApiInterface apiInterface = ApiClient.getRetrofitInstance().create( ApiInterface.class );
        Call<ResponseRemarks> call = apiInterface.remarks(params);
        call.enqueue( new Callback<ResponseRemarks>( ) {
            @Override
            public void onResponse(Call<ResponseRemarks> call, Response<ResponseRemarks> response) {
                ResponseRemarks responseRemarks = response.body();
                if(responseRemarks.getStatus().equals( "success" )){
                    Toast.makeText( AddRemarkActivity.this, "Remark Added Successfully", Toast.LENGTH_SHORT ).show( );
                }
            }
            @Override
            public void onFailure(Call<ResponseRemarks> call, Throwable t) {
                Log.d( TAG, "onFailure: Remarks Uploading failed" );
            }
        } );
    }
}

