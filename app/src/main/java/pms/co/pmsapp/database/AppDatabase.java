package pms.co.pmsapp.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import pms.co.pmsapp.dao.RoomColumnDao;
import pms.co.pmsapp.dao.RoomImagesDao;
import pms.co.pmsapp.model.RoomColumn;
import pms.co.pmsapp.model.RoomImages;

@Database( entities = {RoomColumn.class, RoomImages.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase appDatabase;
    private static String DB_NAME ="PMSApp";

    public static AppDatabase getInstance(Context context){
        if(appDatabase == null){
            appDatabase = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DB_NAME)
                    .allowMainThreadQueries()
                    .build();
        }
        return appDatabase;
    }

    public abstract RoomColumnDao roomColumnDao();

    public abstract RoomImagesDao roomImagesDao();

    public static void destroyInstance() {
        appDatabase = null;
    }
}
