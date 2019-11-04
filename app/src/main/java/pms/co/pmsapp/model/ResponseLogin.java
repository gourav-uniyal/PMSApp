package pms.co.pmsapp.model;

import com.google.gson.annotations.SerializedName;

public class ResponseLogin {

    @SerializedName( "status" )
    private String status;
    @SerializedName( "data" )
    private Verifier verifier;
    @SerializedName( "message" )
    private String message;

    public String getStatus() {
        return status;
    }

    public Verifier getVerifier() {
        return verifier;
    }

    public String getMessage() {
        return message;
    }
}
