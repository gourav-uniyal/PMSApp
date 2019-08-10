package pms.co.pmsapp.interfaces;

import org.json.JSONObject;

import java.util.HashMap;

import pms.co.pmsapp.model.ResponseTask;
import pms.co.pmsapp.model.Verifier;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {

    @POST("tasks?status=complete")
    Call<ResponseTask> completedTask(@Body Verifier verifier, @Query( "page" ) int page);

    @POST("tasks?status=incomplete")
    Call<ResponseTask> incompletedTask(@Body Verifier verifier, @Query( "page" ) int page);
}
