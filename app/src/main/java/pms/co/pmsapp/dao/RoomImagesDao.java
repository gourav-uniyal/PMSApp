package pms.co.pmsapp.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

import pms.co.pmsapp.model.RoomImages;

@Dao
public interface RoomImagesDao {

    @Insert
    void insert(RoomImages roomImages);

    @Query( "UPDATE images SET isUploaded= :isUploaded WHERE path= :path and document_id= :docId" )
    void updateIsUpload(String docId, String path, String isUploaded);

    @Query( "SELECT path FROM images WHERE document_id = :docId" )
    LiveData<List<String>> getAllImagesPath(String docId);

    @Query( "DELETE FROM images WHERE path= :path AND document_id =  :docId" )
    void deleteImages(String docId, String path);

    @Query( "SELECT isUploaded FROM images WHERE path= :path AND document_id = :docId " )
    String getIsUploaded(String docId, String path);

    @Query( "SELECT lat_long FROM images WHERE path= :path AND document_id = :docId " )
    String getLatLong(String docId, String path);

    @Query( "SELECT path FROM images WHERE document_id = :docId AND isUploaded = :isUploaded" )
    List<String> getUploadedPath(String docId, String isUploaded);

    @Query( "SELECT path FROM images WHERE document_id = :docId" )
    List<String> getTotalImagesPath(String docId);

}
