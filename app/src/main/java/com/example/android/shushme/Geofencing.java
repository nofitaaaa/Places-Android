package com.example.android.shushme;

/**
 * Created by nofit on 15/09/2017.
 */

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;
import java.util.List;

public class Geofencing implements ResultCallback
{
    public static final String TAG = Geofencing.class.getSimpleName();
    private static final float GEOFENCE_RADIUS = 50; // 50 meters
    private static final long GEOFENCE_TIMEOUT = 24 * 60 * 60 * 1000; // 24 hours

    private List<Geofence> mGeoFenceList;
    private PendingIntent mGeoFencePendingIntent;
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;

    public Geofencing(Context context, GoogleApiClient client)
    {
        mContext = context;
        mGoogleApiClient = client;
        mGeoFencePendingIntent = null;
        mGeoFenceList = new ArrayList<>();
    }

    public void registerAllGeofences()
    {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected() || mGeoFenceList == null || mGeoFenceList.size() == 0)
        {
            return;
        }

        try
        {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()).setResultCallback(this);
        }

        catch (SecurityException securityException)
        {
            Log.e(TAG, securityException.getMessage());
        }
    }

    public void unRegisterAllGeofences()
    {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected())
        {
            return;
        }

        try
        {
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,getGeofencePendingIntent()).setResultCallback(this);
        }

        catch (SecurityException securityException)
        {
            Log.e(TAG, securityException.getMessage());
        }
    }

    public void updateGeoFencesList(PlaceBuffer places)
    {
        mGeoFenceList = new ArrayList<>();

        if (places == null || places.getCount() == 0) return;

        for (Place place : places)
        {
            String placeUID = place.getId();
            double placeLat = place.getLatLng().latitude;
            double placeLng = place.getLatLng().longitude;

            Geofence geofence = new Geofence.Builder()
                    .setRequestId(placeUID)
                    .setExpirationDuration(GEOFENCE_TIMEOUT)
                    .setCircularRegion(placeLat, placeLng, GEOFENCE_RADIUS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();

            mGeoFenceList.add(geofence);
        }
    }

    private GeofencingRequest getGeofencingRequest()
    {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeoFenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent()
    {
        if (mGeoFencePendingIntent != null)
        {
            return mGeoFencePendingIntent;
        }

        Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
        mGeoFencePendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeoFencePendingIntent;
    }

    @Override
    public void onResult(@NonNull Result result)
    {
        Log.e(TAG, String.format("Error Adding / Removing Geofence : %s", result.getStatus().toString()));
    }
}
