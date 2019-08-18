package pms.co.pmsapp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import pms.co.pmsapp.R;
import pms.co.pmsapp.utils.AppController;
import pms.co.pmsapp.utils.AppPreferences;
import pms.co.pmsapp.utils.EndPoints;

public class LogInActivity extends AppCompatActivity {

    //region Variable Declaration
    private Button btnLogin;
    private String TAG = LogInActivity.class.getSimpleName();
    private EditText txt_email;
    private EditText txt_password;
    private String status;
    private String user_id;
    private ProgressDialog progressDialog;
    //endregiong

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_log_in );

        final AppPreferences appPreferences = new AppPreferences( this );
        if (appPreferences.getEmail() != null) {
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            intent.putExtra( "verifier", appPreferences.getName() );
            startActivity( intent );
            finish();
        }

        btnLogin = findViewById( R.id.btn_login );
        txt_email = findViewById( R.id.txt_email );
        txt_password = findViewById( R.id.txt_password );

        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Logging In");
        progressDialog.setMessage("Please Wait...");

        btnLogin.setOnClickListener( new View.OnClickListener( ) {
            @Override
            public void onClick(View view) {

                progressDialog.show();

                boolean isEmptyField = false;

                final String email  = txt_email.getText().toString().trim();
                final String password  = txt_password.getText().toString().trim();

                if (TextUtils.isEmpty(email)){
                    isEmptyField = true;
                    txt_email.setError("required");
                }
                if (TextUtils.isEmpty(password)){
                    isEmptyField = true;
                    txt_password.setError("required");
                }
                if (!email.contains("@")) {
                    txt_email.setError("this doesn't look like an email id");
                    isEmptyField = true;
                }
                if (!isEmptyField) {
                    Map<String , String> params = new HashMap<>(  );
                    params.put("email", email);
                    params.put("password", password);
                    JSONObject jsonObject = new JSONObject(params);
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest( Request.Method.POST, EndPoints.LOGIN_API, jsonObject, new Response.Listener<JSONObject>( ) {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.v(TAG, "Response:" + response.toString());
                                status = response.getString("status");
                                Log.v(TAG, "VolleySuccess" + status);
                                if (status.equals("success")) {
                                    progressDialog.dismiss();
                                    JSONObject jsonObject1 = new JSONObject( response.getString( "data" ) );
                                    user_id = jsonObject1.getString( "id" );
                                    appPreferences.setEmail( jsonObject1.getString( "email" ) );
                                    appPreferences.setName(user_id );
                                    Toast.makeText(getApplicationContext(), "Login user successful", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                    intent.putExtra( "verifier" ,user_id);
                                    startActivity(intent);
                                    finish();
                                }
                                else {
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Email or Password is Incorrect", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                progressDialog.dismiss();
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.v(TAG, "VolleyError: " + error.getLocalizedMessage());
                            Toast.makeText(getApplicationContext(), "Email or Password is Incorrect", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
                    AppController.getInstance().addToRequestQueue(jsonObjectRequest);
                }
            }
        });
    }
}
