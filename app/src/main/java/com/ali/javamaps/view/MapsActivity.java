package com.ali.javamaps.view;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ComposePathEffect;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import com.ali.javamaps.R;
import com.ali.javamaps.model.Place;
import com.ali.javamaps.roomDataBase.PlaceDao;
import com.ali.javamaps.roomDataBase.PlaceDatabase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ali.javamaps.databinding.ActivityMapsBinding;
import com.google.android.material.snackbar.Snackbar;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

        private GoogleMap mMap;
    private ActivityMapsBinding binding;
    LocationManager locationManager;
    LocationListener locationListener;
    ActivityResultLauncher<String> permissionLauncher;//kullanıcıdan izin isteme

    PlaceDatabase db;
    PlaceDao placeDao;

    Place selectedPlace;

    double selectedLatitude;
    double selectedLongitude;

    private CompositeDisposable compositeDisposable=new CompositeDisposable();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        registerLauncher();

        db= Room.databaseBuilder(getApplicationContext(),PlaceDatabase.class,"Places")
                .build();
        placeDao=db.placeDao();

        selectedLatitude=0.0;
        selectedLongitude=0.0;

        binding.deletButton.setEnabled(false);
    }


    @Override
    public void onMapReady(GoogleMap googleMap){
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);

        binding.saveButton.setEnabled(false);
        binding.deletButton.setEnabled(false);

        Intent intent=getIntent();

        String Intentinfo=intent.getStringExtra("info");

        if(Intentinfo.equals("new")){

            binding.saveButton.setVisibility(View.VISIBLE);
            binding.deletButton.setVisibility(View.GONE);

            
            locationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener=new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    
                    SharedPreferences sharedPreferences=MapsActivity.this.getSharedPreferences("com.ali.javamaps",MODE_PRIVATE);
                    boolean info=sharedPreferences.getBoolean("info",false);
                    if(info==false){
                        LatLng userLocation=new LatLng(location.getLatitude(),location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15));
                        mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location!"));

                        sharedPreferences.edit().putBoolean("info",true).apply();

                    }


                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    LocationListener.super.onStatusChanged(provider, status, extras);
                }
            };

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                //kullanıcıdan izin al
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                    //kullanıcıdan izin al
                    Snackbar.make(binding.getRoot(),"Permission needed for maps",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //kullanıcıdan izin al
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);

                        }
                    }).show();


                }else{
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);

                }

            }else{
               
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                Location lastLocation= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if(lastLocation!=null){
                    LatLng lastLocationLatlng=new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocationLatlng,15));
                }

                mMap.setMyLocationEnabled(true);

            }


        }else{  

            mMap.clear();
            selectedPlace = (Place) intent.getSerializableExtra("place");

            LatLng latLng=new LatLng(selectedPlace.latitude,selectedPlace.longitude);

            mMap.addMarker(new MarkerOptions().position(latLng).title("The location that you saved to visit"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

            binding.placeNameText.setText(selectedPlace.name);
            binding.saveButton.setVisibility(View.GONE);
            binding.deletButton.setVisibility(View.VISIBLE);
        }

    }

    private void registerLauncher(){

        permissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {

                if(result==true){

                    if(ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                        Location lastLocation= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        if(lastLocation!=null){//eğer son lokasyon verisi boş değilse if'i çalıştır
                            LatLng lastLocationLatlng=new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocationLatlng,15));


                        }

                    }


                }else{
                    Toast.makeText(MapsActivity.this, "Izin versene lan gavat", Toast.LENGTH_SHORT).show();

                }

            }
        });


    }


    @Override

    public void onMapLongClick(@NonNull LatLng latLng) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title("You choose here!"));

        selectedLatitude=latLng.latitude;
        selectedLongitude=latLng.longitude;

        binding.saveButton.setEnabled(true);
        binding.deletButton.setEnabled(true);
    }

    public void save(View view){

        Place place=new Place(binding.placeNameText.getText().toString(),selectedLatitude,selectedLongitude);

        compositeDisposable.add(placeDao.insert(place)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()) 
                .subscribe(MapsActivity.this::handleResponse));

}

    private void handleResponse(){
        Intent intent=new Intent(MapsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void delete(View view){

            compositeDisposable.add(placeDao.delete(selectedPlace)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(MapsActivity.this::handleResponse));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}
