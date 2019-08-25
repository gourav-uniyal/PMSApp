package pms.co.pmsapp.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import pms.co.pmsapp.model.RoomColumn;

@Dao
public interface RoomColumnDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(RoomColumn roomColumn);

    @Query("Update documents Set remarks = :remarks Where document_id = :docId")
    void updateRemark(String docId, String remarks);

}
