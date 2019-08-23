package pms.co.pmsapp.model;

import com.google.gson.annotations.SerializedName;

public class ResponseLogin {

    @SerializedName( "status" )
    private String status;
    @SerializedName( "data" )
    private Verifier verifier;

    public String getStatus() {
        return status;
    }

    public Verifier getVerifier() {
        return verifier;
    }
}
