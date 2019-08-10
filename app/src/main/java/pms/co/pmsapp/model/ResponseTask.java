package pms.co.pmsapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ResponseTask {

    @SerializedName( "status" )
    private String status;
    @SerializedName( "data" )
    private ResponseData responseData;

    public String getStatus() {
        return status;
    }

    public ResponseData getResponseData() {
        return responseData;
    }
}
