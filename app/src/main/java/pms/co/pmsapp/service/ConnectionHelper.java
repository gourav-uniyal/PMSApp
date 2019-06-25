package pms.co.pmsapp.service;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.android.gms.location.LocationResult;

public class ConnectionHelper {

    public static long lastNoConnectionTs = -1;
    public static boolean isOnline = false;
    public static boolean isGpsEnable = false;

    public static boolean isConnected(Context context) {

        ConnectivityManager cm =(ConnectivityManager)  context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnected();
    }

    public static boolean isConnectedOrConnecting(Context context) {

        ConnectivityManager cm =(ConnectivityManager)         context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting() && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static boolean isGpsEnabled(Context context) {

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}
