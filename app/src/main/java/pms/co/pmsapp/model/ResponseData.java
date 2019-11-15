package pms.co.pmsapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ResponseData {

    @SerializedName( "last_page" )
    private String totalPage;
    @SerializedName("current_page")
    private String currentPage;
    @SerializedName( "data" )
    private ArrayList<Case> caseArrayList = new ArrayList<>();

    public String getCurrent_page() {
        return currentPage;
    }

    public ArrayList<Case> getCaseArrayList() {
        return caseArrayList;
    }

    public String getTotalPage() {
        return totalPage;
    }
}
