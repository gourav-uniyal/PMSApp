package pms.co.pmsapp.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import pms.co.pmsapp.activity.DialogActivity;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class CheckLocationService extends Service {

    static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    static final String LOCATION_CHANGED = "android.location.PROVIDERS_CHANGED";
    BroadcastReceiver receiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        IntentFilter filter = new IntentFilter();
        filter.addAction(CONNECTIVITY_CHANGE_ACTION);
        filter.addAction(LOCATION_CHANGED);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                if (CONNECTIVITY_CHANGE_ACTION.equals(action) || LOCATION_CHANGED.equals(action)) {

                    if (!ConnectionHelper.isConnectedOrConnecting(context)) {

                        if(!ConnectionHelper.isConnected(context) && !ConnectionHelper.isGpsEnabled(context)){
                            Log.i("NETWORK123", "Connection lost");
                            Intent intent1 = new Intent(context, DialogActivity.class);
                            intent1.putExtra("key1", "Location and Internet Services are Disabled. Enable them to use this App.");
                            intent1.putExtra("key2", "0");
                            intent1.addFlags(FLAG_ACTIVITY_NEW_TASK );
                            startActivity(intent1);
                        }

                        else if (!ConnectionHelper.isGpsEnabled(context)) {
                            Log.i("NETWORK123", "GPS Connection lost");
                            Intent intent1 = new Intent(context, DialogActivity.class);
                            intent1.putExtra("key1", "Location Services are Disabled or GPS may be off. Put Location service to High Accuracy to use this App");
                            intent1.putExtra("key2", "1");
                            intent1.addFlags(FLAG_ACTIVITY_NEW_TASK );
                            startActivity(intent1);
                        }

                        else if (!ConnectionHelper.isConnected(context)) {
                            Log.i("NETWORK123", "Connection lost");
                            Intent intent1 = new Intent(context, DialogActivity.class);
                            intent1.putExtra("key1", "Internet Services are Disabled. Enable them to use this App");
                            intent1.putExtra("key2", "2");
                            intent1.addFlags(FLAG_ACTIVITY_NEW_TASK );
                            startActivity(intent1);
                        }

                    } else {
                        Intent intent2 = new Intent("finish_activity");
                        intent2.addFlags(FLAG_ACTIVITY_NEW_TASK );
                        sendBroadcast(intent2);
                        Log.i("NETWORK123", "Both Connected");
                    }
                }
            }
        };
        registerReceiver(receiver, filter);
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(receiver);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
        super.onDestroy();
    }
}