package pms.co.pmsapp.model;

import com.google.gson.annotations.SerializedName;

public class ResponseRemarks {

    @SerializedName( "status" )
    private String status;

    public String getStatus() {
        return status;
    }

}
