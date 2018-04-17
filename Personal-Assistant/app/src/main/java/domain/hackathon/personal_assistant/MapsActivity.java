package domain.hackathon.personal_assistant;

import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMyLocationButtonClickListener {

    private GoogleMap mMap;
    private final double fullSaillat = 28.595504;
    private final double fullSaillong = -81.304142;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        auth = FirebaseAuth.getInstance();


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

        addMapMarker();
        mMap.setOnMyLocationButtonClickListener(this);


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
    public View getInfoWindow(Marker marker){
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

    private  void addMapMarker() {
        if (mMap == null) {
            return;
        }

        MarkerOptions options = new MarkerOptions();
        options.title("Full Sail University");
        options.snippet("Blackmoor");
        LatLng officeLocation = new LatLng(fullSaillat, fullSaillong);
        options.position(officeLocation);

        mMap.addMarker(options);
    }
}
