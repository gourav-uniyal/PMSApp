package pms.co.pmsapp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.StrictMode;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import pms.co.pmsapp.R;
import pms.co.pmsapp.interfaces.ApiInterface;
import pms.co.pmsapp.libs.ApiClient;
import pms.co.pmsapp.model.ResponseLogin;
import pms.co.pmsapp.model.Verifier;
import pms.co.pmsapp.utils.AppPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
            intent.putExtra( "verifier", appPreferences.getVerifier() );
            intent.putExtra( "name", appPreferences.getEmail() );
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

        btnLogin.setOnClickListener( view -> {

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
                txt_email.setError("enter valid email address");
                isEmptyField = true;
            }
            if (!isEmptyField) {

                progressDialog.show();

                Verifier verifier = new Verifier(email, password);

                ApiInterface apiInterface = ApiClient.getRetrofitInstance().create( ApiInterface.class );
                Call<ResponseLogin> call = apiInterface.userLogin( verifier);
                call.enqueue( new Callback<ResponseLogin>( ) {
                    @Override
                    public void onResponse(Call<ResponseLogin> call, Response<ResponseLogin> response) {
                        ResponseLogin responseLogin = response.body();
                        progressDialog.dismiss();
                        if(responseLogin.getStatus().equals( "success" )){
                            appPreferences.setVerifier( responseLogin.getVerifier().getVerifier() );
                            appPreferences.setEmail( responseLogin.getVerifier().getName() );
                            Log.v( TAG, responseLogin.getVerifier().getName() );
                            String verifier = responseLogin.getVerifier().getVerifier();
                            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                            intent.putExtra( "verifier" ,verifier);
                            intent.putExtra( "name", responseLogin.getVerifier().getName() );
                            startActivity(intent);
                            finish();
                        }
                        if(responseLogin.getStatus().equals( "error" ))
                            Toast.makeText( LogInActivity.this, "Email or Password is incorrect", Toast.LENGTH_SHORT ).show( );

                    }
                    @Override
                    public void onFailure(Call<ResponseLogin> call, Throwable t) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Email or Password is Incorrect", Toast.LENGTH_SHORT).show();
                    }
                } );

            }
        } );
    }
}
