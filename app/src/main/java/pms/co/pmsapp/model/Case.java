package pms.co.pmsapp.model;

public class Case {

    private String fileId;
    private String subject;
    private String clientId;
    private String customer_Name;
    private String date;
    private String verification;
    private String path;
    private String formpath;
    private String docId;
    private String docName;
    private String clientName;

    public String getCustomerName() {
        return customer_Name;
    }

    public void setCustomerName(String customer_id) {
        this.customer_Name = customer_id;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public void setFormpath(String formpath) {
        this.formpath = formpath;
    }

    public String getVerification() {
        return verification;
    }

    public void setVerification(String verification) {
        this.verification = verification;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }
}
