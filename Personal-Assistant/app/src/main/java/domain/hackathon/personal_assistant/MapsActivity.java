package domain.hackathon.personal_assistant;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.model.DirectionsResult;
import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.android.rides.RideRequestButton;
import com.uber.sdk.android.rides.RideRequestButtonCallback;
import com.uber.sdk.android.rides.RideRequestDeeplink;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.core.client.ServerTokenSession;
import com.uber.sdk.core.client.SessionConfiguration;
import com.uber.sdk.rides.client.error.ApiError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMapLongClickListener {

    public GoogleMap mMap;
    private final double fullSaillat = 28.595504;
    private final double fullSaillong = -81.304142;
    EditText manualinput;
    Button requestbtn;
    String startlocation;
    String endlocation;
    private RideRequestButton rideRequestButton;
    SessionConfiguration config;
    private Marker markertodelete = null;
    private LocationManager mLocationManager;
    Geocoder gc;

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                mLocationManager.removeUpdates(mLocationListener);
                addMapMarker("Current Location", "You are here", location);
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        rideRequestButton = (RideRequestButton) findViewById(R.id.riderequestbtn);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        manualinput = (EditText) findViewById(R.id.manualreq);
        requestbtn = (Button) findViewById(R.id.btnreq);


        config = new SessionConfiguration.Builder()
                .setClientId("22LI3sqhTJFtDvecVdiA-IQXNknAkp44")
                .setServerToken("CyEx0JbSCoAoi2kvFSiS5jWugrkHIiuO3vp7jThx")
                .setEnvironment(SessionConfiguration.Environment.PRODUCTION)
                .setScopes(Arrays.asList(Scope.PROFILE, Scope.RIDE_WIDGETS))
                .build();

        UberSdk.initialize(config);

//        RideRequestDeeplink deeplink = new RideRequestDeeplink.Builder(getApplicationContext())
//                .setSessionConfiguration(config)
//                .setRideParameters(rideParams)
//                .build();
//
//        deeplink.execute();

        gc = new Geocoder(getApplicationContext());

        requestbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (gc.isPresent()) {
                    try {
                        mMap.clear();
                        List<Address> list = gc.getFromLocationName(manualinput.getText().toString(), 1);
                        Address address = list.get(0);
                        double lat = address.getLatitude();
                        double lng = address.getLongitude();
                        Location curlocation = getCurrentLocation();
                        LatLng clatlong = new LatLng(curlocation.getLatitude(), curlocation.getLongitude());
                        LatLng dlatlong = new LatLng(lat, lng);
                        Location dlocation = new Location("");
                        dlocation.setLatitude(lat);
                        dlocation.setLongitude(lng);
                        markertodelete = addMapMarker("Drop off", "End of the ride", dlocation);
                        GoogleDirection.withServerKey("AIzaSyDXPStB0SkGvzwAsef7OkfjyWMjoMAyIx0")
                                .from(clatlong)
                                .to(dlatlong)
                                .execute(new DirectionCallback() {
                                    @Override
                                    public void onDirectionSuccess(Direction direction, String rawBody) {
                                        if (direction.getRouteList().size() > 0)
                                        {
                                            Route route = direction.getRouteList().get(0);
                                            Leg leg = route.getLegList().get(0);
                                            ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                                            PolylineOptions polylineOptions = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 5, Color.RED);
                                            mMap.addPolyline(polylineOptions);
                                        }
                                        else {
                                            Toast.makeText(getApplicationContext(), "Could not find a route. Please try again", Toast.LENGTH_LONG).show();
                                        }

                                    }

                                    @Override
                                    public void onDirectionFailure(Throwable t) {
                                        // Do something here
                                    }
                                });

                        RideParameters rideParams = new RideParameters.Builder()
                                .setProductId("a1111c8c-c720-46c3-8534-2fcdd730040d")
                                .setPickupLocation(curlocation.getLatitude(), curlocation.getLongitude(), "Current location", "1455 Market Street, San Francisco, California")
                                .setDropoffLocation(lat, lng, "Drop off point", manualinput.getText().toString())
                                .build();
                        ServerTokenSession session = new ServerTokenSession(config);

                        RideRequestButtonCallback callback = new RideRequestButtonCallback() {

                            @Override
                            public void onRideInformationLoaded() {
                                // react to the displayed estimates
                                Log.d("MapsActivity", "infoloaded");

                            }

                            @Override
                            public void onError(ApiError apiError) {
                                // API error details: /docs/riders/references/api#section-errors
                                Log.d("MapsActivity", "apierror" + apiError);

                            }

                            @Override
                            public void onError(Throwable throwable) {
                                // Unexpected error, very likely an IOException
                                Log.d("MapsActivity", "error" + throwable);


                            }
                        };
                        // set parameters for the RideRequestButton instance
                        rideRequestButton.setCallback(callback);
                        rideRequestButton.setSession(session);

                        rideRequestButton.setRideParameters(rideParams);
                        rideRequestButton.loadRideInformation();

                    } catch (Exception e) {
                        Log.d(MapsActivity.class.getSimpleName(), "Exception " + e);

                    }
                }
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCurrentLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(mLocationListener);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapLongClick(LatLng point) {
        if (markertodelete != null)
            markertodelete.remove();
        mMap.clear();


        //Toast.makeText(this, "Long click:" + point, Toast.LENGTH_SHORT).show();

        Location curlocation = getCurrentLocation();
        Location dlocation = new Location("");
        dlocation.setLatitude(point.latitude);
        dlocation.setLongitude(point.longitude);
        markertodelete = addMapMarker("Drop off", "End of the ride", dlocation);

        startlocation = curlocation.getLatitude() + "," + curlocation.getLatitude();

        endlocation = dlocation.getLatitude() + "," + dlocation.getLatitude();

        RideParameters rideParams = new RideParameters.Builder()
                .setProductId("a1111c8c-c720-46c3-8534-2fcdd730040d")
                .setPickupLocation(curlocation.getLatitude(), curlocation.getLongitude(), "Current location", "1455 Market Street, San Francisco, California")
                .setDropoffLocation(point.latitude, point.longitude, "Drop off point", "One Embarcadero Center, San Francisco")
                .build();
        ServerTokenSession session = new ServerTokenSession(config);

        RideRequestButtonCallback callback = new RideRequestButtonCallback() {

            @Override
            public void onRideInformationLoaded() {
                // react to the displayed estimates
                Log.d("MapsActivity", "infoloaded");

            }

            @Override
            public void onError(ApiError apiError) {
                // API error details: /docs/riders/references/api#section-errors
                Log.d("MapsActivity", "apierror" + apiError);

            }

            @Override
            public void onError(Throwable throwable) {
                // Unexpected error, very likely an IOException
                Log.d("MapsActivity", "error" + throwable);


            }
        };
        // set parameters for the RideRequestButton instance
        rideRequestButton.setCallback(callback);
        rideRequestButton.setSession(session);

        rideRequestButton.setRideParameters(rideParams);
        rideRequestButton.loadRideInformation();


        LatLng clatlong = new LatLng(curlocation.getLatitude(), curlocation.getLongitude());
        LatLng dlatlong = new LatLng(dlocation.getLatitude(), dlocation.getLongitude());
        GoogleDirection.withServerKey("AIzaSyDXPStB0SkGvzwAsef7OkfjyWMjoMAyIx0")
                .from(clatlong)
                .to(dlatlong)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        if (direction.getRouteList().size() > 0)
                        {
                            Route route = direction.getRouteList().get(0);
                            Leg leg = route.getLegList().get(0);
                            ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                            PolylineOptions polylineOptions = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 5, Color.RED);
                            mMap.addPolyline(polylineOptions);
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Could not find a route. Please try again", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        // Do something here
                    }
                });

    }

    private Location getCurrentLocation() {
        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Location location = null;
        if (!(isGPSEnabled || isNetworkEnabled))
            Toast.makeText(getApplicationContext(), "Error getting location", Toast.LENGTH_SHORT).show();
        else {
            if (isNetworkEnabled) {
                try {
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            10, 5000, mLocationListener);
                    location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    addMapMarker("Current Location", "You are here", location);
                    return location;

                } catch (SecurityException e) {
                    Toast.makeText(getApplicationContext(), "error" + e, Toast.LENGTH_SHORT).show();
                }

            }

            if (isGPSEnabled) {
                try {
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            10, 5000, mLocationListener);
                    location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    addMapMarker("Current Location", "You are here", location);
                    return location;

                } catch (SecurityException e) {
                    Toast.makeText(getApplicationContext(), "error" + e, Toast.LENGTH_SHORT).show();
                }
            }
        }
        return null;
    }


    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(this);
        mMap.setOnInfoWindowClickListener(this);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    99);
        }
        Location location = new Location("");
        location.setLatitude(fullSaillat);
        location.setLongitude(fullSaillong);
        addMapMarker("Full Sail University", "Blackmoor", location);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMapLongClickListener(this);


        //zoomInCamera();

        // Add a marker in Sydney and move the camera
        //LatLng fullsail = new LatLng(28.595504, -81.304142);
        //mMap.addMarker(new MarkerOptions().position(fullsail).title("Marker in Full Sail"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(fullsail));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 99: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
            }
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        new AlertDialog.Builder(this).setTitle(marker.getTitle())
                .setMessage(marker.getSnippet())
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View contents = LayoutInflater.from(this).inflate(R.layout.info_window, null);
        ((TextView) contents.findViewById(R.id.title)).setText(marker.getTitle());
        ((TextView) contents.findViewById(R.id.snippet)).setText(marker.getSnippet());

        return contents;
    }

    public void zoomInCamera() {
        if (mMap == null) {
            return;
        }
        LatLng fullsail = new LatLng(fullSaillat, fullSaillong);
        CameraUpdate cameraMovement = CameraUpdateFactory.newLatLngZoom(fullsail, 16);
        mMap.animateCamera(cameraMovement);

    }

    private Marker addMapMarker(String title, String snippet, Location location) {
        if (mMap == null) {
            return null;
        }

        MarkerOptions options = new MarkerOptions();
        options.title(title);
        options.snippet(snippet);
        LatLng Location = new LatLng(location.getLatitude(), location.getLongitude());
        options.position(Location);

        return mMap.addMarker(options);

    }
}

