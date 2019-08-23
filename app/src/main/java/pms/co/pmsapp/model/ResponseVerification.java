package pms.co.pmsapp.model;

import com.google.gson.annotations.SerializedName;

public class ResponseVerification {

    @SerializedName( "status" )
    private String status;

    public String getStatus() {
        return status;
    }
}


