package pms.co.pmsapp.model;

import com.google.gson.annotations.SerializedName;

public class Verifier {

    @SerializedName( "verifier" )
    private String verifier;

    public void setVerifier(String verifier) {
        this.verifier = verifier;
    }
}
