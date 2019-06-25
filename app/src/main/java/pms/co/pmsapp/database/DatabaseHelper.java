package pms.co.pmsapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pms.co.pmsapp.model.DBModel;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "pms_db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL( DBModel.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DBModel.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public long insertImages(String docId, String path) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBModel.COLUMN_DOC_ID , docId );
        values.put(DBModel.COLUMN_PATH , path );

        long id = db.insert(DBModel.TABLE_NAME, null, values);

        db.close();
        return id;
    }

    public long updateImages(String docId, String imageDetail) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBModel.COLUMN_PATH, imageDetail);

        String[] args = new String[]{docId};

        return db.update(DBModel.TABLE_NAME , values,    DBModel.COLUMN_DOC_ID + "=?", args);

    }

    public long updateRemark(String docId, String remark) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBModel.COLUMN_REMARK, remark);

        String[] args = new String[]{docId};

        return db.update(DBModel.TABLE_NAME, values, DBModel.COLUMN_DOC_ID +  "=?", args);
    }

    public Cursor fetchdatabase(String docId){

        String query = "select * from " +DBModel.TABLE_NAME+ " where "+ DBModel.COLUMN_DOC_ID + " = '" + docId + "'";
        SQLiteDatabase sql = this.getReadableDatabase();
        Cursor cur = sql.rawQuery(query, null);
        return cur;
    }

    public String displayDatabase(String docId){
        Cursor cursor = fetchdatabase( docId );
        String images = null;
        if (cursor.moveToFirst( ))
            images = cursor.getString( cursor.getColumnIndex( "images" ) );

        else
            images= null;

        return images;
    }


}