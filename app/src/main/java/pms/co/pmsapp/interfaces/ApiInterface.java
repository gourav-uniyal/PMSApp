package pms.co.pmsapp.interfaces;

import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pms.co.pmsapp.model.ResponseImageUpload;
import pms.co.pmsapp.model.ResponseLogin;
import pms.co.pmsapp.model.ResponseRemarks;
import pms.co.pmsapp.model.ResponseTask;
import pms.co.pmsapp.model.ResponseTotalImages;
import pms.co.pmsapp.model.ResponseVerification;
import pms.co.pmsapp.model.Verifier;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {

    @POST("tasks?status=complete")
    Call<ResponseTask> completedTask(@Body HashMap verifier, @Query( "page" ) int page);

    @POST("tasks?status=incomplete")
    Call<ResponseTask> incompletedTask(@Body HashMap verifier, @Query( "page" ) int page);

    @POST("login")
    Call<ResponseLogin> userLogin(@Body Verifier verifier);

    @POST("total-images")
    Call<ResponseTotalImages> totalImages(@Body HashMap map);

    @POST("remarks/create")
    Call<ResponseRemarks> remarks(@Body HashMap map );

    @POST("verification-completed")
    Call<ResponseVerification> verification(@Body HashMap map);

    @Multipart
    @POST("reports/create")
    Call<ResponseImageUpload> uploadImage(
            @Part("document_id") RequestBody docId, @Part("lat_long") RequestBody latLong,
            @Part MultipartBody.Part file);
}
