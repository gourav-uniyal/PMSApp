package pms.co.pmsapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import pms.co.pmsapp.R;
import pms.co.pmsapp.database.DatabaseHelper;
import pms.co.pmsapp.utils.AppController;
import pms.co.pmsapp.utils.EndPoints;

public class AddRemarkActivity extends Activity implements View.OnClickListener {

    //region Variable Declaration
    private EditText txtRemarks;
    private String status;
    private String TAG = AddRemarkActivity.class.getSimpleName( );
    private String docId;
    private DatabaseHelper dbHelper;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_add_remark );

        docId = getIntent( ).getStringExtra( "document_id" );
        dbHelper = new DatabaseHelper( this );

        TextView lblSaveRemark = findViewById( R.id.lbl_save_remark );
        txtRemarks = findViewById( R.id.txt_remark );

        lblSaveRemark.setOnClickListener( this );
    }

    @Override
    public void onClick(View view) {
        switch (view.getId( )) {
            case R.id.lbl_save_remark:
                String remarks = txtRemarks.getText( ).toString( );
                dbHelper.updateRemark( docId, remarks );
                uploadRemark( docId, remarks );
                Log.v( TAG, "Remarks added" );
                finish( );
        }
    }

    public void uploadRemark(String docId, String remarks) {

        Map<String, String> params = new HashMap<>( );
        params.put( "document_id", docId );
        params.put( "remarks", remarks );

        JSONObject jsonObject = new JSONObject( params );

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest( Request.Method.POST, EndPoints.REMARK_DATA_API, jsonObject, new Response.Listener<JSONObject>( ) {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    Log.v( TAG, "Response:" + response.toString( ) );
                    status = response.getString( "status" );
                    if (status.equals( "success" )) {
                        Toast.makeText( getApplicationContext( ), "Remark added successfully", Toast.LENGTH_SHORT ).show( );
                    } else {
                        Toast.makeText( getApplicationContext( ), "Error on adding Remark", Toast.LENGTH_SHORT ).show( );
                    }

                } catch (JSONException e) {
                    e.printStackTrace( );
                }
            }
        }, new Response.ErrorListener( ) {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v( TAG, "VolleyError: " + error.getLocalizedMessage( ) );
                Toast.makeText( getApplicationContext( ), "Email or Password is Incorrect", Toast.LENGTH_SHORT ).show( );
            }
        } );
        AppController.getInstance( ).addToRequestQueue( jsonObjectRequest );
    }
}

