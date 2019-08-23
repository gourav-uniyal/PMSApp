package pms.co.pmsapp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.StrictMode;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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
                    Verifier verifier = new Verifier(email, password);

                    ApiInterface apiInterface = ApiClient.getRetrofitInstance().create( ApiInterface.class );
                    Call<ResponseLogin> call = apiInterface.userLogin( verifier);
                    call.enqueue( new Callback<ResponseLogin>( ) {
                        @Override
                        public void onResponse(Call<ResponseLogin> call, Response<ResponseLogin> response) {
                            ResponseLogin responseLogin = response.body();
                            if(responseLogin.getStatus().equals( "success" )){
                                progressDialog.dismiss();
                                appPreferences.setName( responseLogin.getVerifier().getVerifier() );
                                appPreferences.setEmail( responseLogin.getVerifier().getEmail() );
                                String verifier = responseLogin.getVerifier().getVerifier();
                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                intent.putExtra( "verifier" ,verifier);
                                startActivity(intent);
                                finish();
                            }
                        }
                        @Override
                        public void onFailure(Call<ResponseLogin> call, Throwable t) {
                            Toast.makeText(getApplicationContext(), "Email or Password is Incorrect", Toast.LENGTH_SHORT).show();
                        }
                    } );

                }
            }
        });
    }
}
