package pms.co.pmsapp.model;

import com.google.gson.annotations.SerializedName;

public class Case {

    @SerializedName( "file_id" )
    private String fileId;
    @SerializedName( "subject" )
    private String subject;
    @SerializedName( "client_id" )
    private String clientId;
    @SerializedName("customer_name")
    private String customer_Name;
    @SerializedName( "created_at" )
    private String date;
    @SerializedName( "verification_point" )
    private String verification;
    @SerializedName( "path" )
    private String path;
    @SerializedName( "form_path" )
    private String formpath;
    @SerializedName( "document_id" )
    private String docId;
    @SerializedName( "document_name" )
    private String docName;
    @SerializedName( "client_name" )
    private String clientName;

    public String getCustomerName() {
        return customer_Name;
    }

    public String getClientName() {
        return clientName;
    }

    public String getDocId() {
        return docId;
    }

    public String getFileId() {
        return fileId;
    }

    public String getSubject() {
        return subject;
    }

    public String getClientId() {
        return clientId;
    }

    public String getDate() {
        return date;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFormpath() {
        return formpath;
    }

    public String getVerification() {
        return verification;
    }

    public String getDocName() {
        return docName;
    }

}
