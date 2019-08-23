package pms.co.pmsapp.model;

import com.google.gson.annotations.SerializedName;

public class Verifier {

    @SerializedName( "id" )
    private String verifier;
    @SerializedName("email")
    private String email;
    @SerializedName( "password" )
    private String password;

    public Verifier(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setVerifier(String verifier) {
        this.verifier = verifier;
    }

    public String getVerifier() {
        return verifier;
    }
}
