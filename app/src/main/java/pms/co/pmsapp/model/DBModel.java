package pms.co.pmsapp.model;

public class DBModel {

    public static final String TABLE_NAME = "documents";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DOC_ID = "document_id";
    public static final String COLUMN_PATH = "images";
    public static final String COLUMN_REMARK ="remarks";



    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_DOC_ID + " TEXT,"
                    + COLUMN_PATH + " TEXT," +
                    COLUMN_REMARK +" TEXT"  + ")";


}
