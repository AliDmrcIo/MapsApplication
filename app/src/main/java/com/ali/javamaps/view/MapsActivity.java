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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener {//bu implements den sonraki 2.ifadeyi ben yazdım ki uzun tıklama için gereklidir. Sonra bana bu inteface ile alakalı hata verecek ve bazı methodları uygulamamı isteyecek

    //Burada maps kullanacaksak android studio bizden gidip Google Maps API key edinmemiz gerektiğini söylüyor ve biz de gidip AndroidManifst.xml dosyasında yazan adresten api keimizi aldık ve manifest xmlinde meta-data nın altında bulunan android:value="AIzaSyAooZkel6tKV54LLX1Fs0P9HaKCas8T8qo" /> yerine kopyaladık

    //Sqlin bu insert intoları select*from ları falanları filanlarından kurtulmak ve daha kolay bu sql i kullanmak için room kütüphanesi yazlmış ve onu kullanacağız biz de. Burada kullanmak için öncelikle gradle.app'e gidip dependencies altında 5 satır kod kopyaladım. Oraya bakabilirsin. Onları Kullanabilmek için onları oraya kopyalamam gerekliydi ve RXjava denen bir şeyi de öğreneceğiz. Dependecy altında kopyalanacak o 5 satır kodu bulabilmek için google'a room database android yazıp androidin sitesine gidebiliriz. Orada yazıyor ne kopyalanacağı
    //Bu room'un 3 tane ana temel yapısı var. Room Database(Normal database kısmı) Data Access Object(Veriye erişme objesi. Verileri alırken ya da silerken bu arayüzü kullancağız.) ve son olarak Entities(Bu da modelimiz, kolonlarda ne olacak değerleri ne olacak vs, daha önce yaptığımız create table if not exists falan işleri)
    //Bu entites kısmını yapmak için aynı google'a room database android yazdığımızda çıkan sayfadan altta tanımlamamız gereken sınıfları vs tanımlamalıyız. Bunlar karışmasın diye gidip şimdi bir package açıcam

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

    private CompositeDisposable compositeDisposable=new CompositeDisposable();//disposible yani tek kullanımlık. Bu yaptığımız Completablelar, flowablelar, singellar observeballar, bunların hepsi kullan at bir torbada tutulabiliyor. Yani bunların varlığı hafızada yer kaplamadan, 1 kere kullandıktan sonra oraya(en altta yazdım onDestroy methodunda) atılabiliyor


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        registerLauncher();

        db= Room.databaseBuilder(getApplicationContext(),PlaceDatabase.class,"Places")
                //.allowMainThreadQueries() MainThread'de işlem yapılmasına izin ver dedik, artık çöken uygulamamız çökmeyecek
                .build();//Database bu şekilde main class'ta initialize ediliyor. oluşturduğumuz database class'ını kullanmak ve oradan bir nesne oluşturmak için bunu yazdık. İnitialize ettik aslında yani, şunun gibi bişe. PlaceDatabase db=new PlaceDatabase(); gibi bir şey bu yaptığımız.
        placeDao=db.placeDao(); //bu da daoya erişebileceğimiz bir initialize etme yöntemi. Dao da da böyle initialize edilir

        selectedLatitude=0.0;
        selectedLongitude=0.0;

        binding.deletButton.setEnabled(false);
    }


    @Override
    public void onMapReady(GoogleMap googleMap){//onCreate yerine bu projede bir çok yerde onMapReady içerisine bir şeyler yazacağız.
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);//en aşağıdaki onMapLongClick methodunun çalışabilmesi için yazılması gereken bir method

        binding.saveButton.setEnabled(false);
        binding.deletButton.setEnabled(false);

        Intent intent=getIntent();//buraya gelecek olan intent'in new veya old olmasına göre farklı reaksiyonlar alacağız

        String Intentinfo=intent.getStringExtra("info");

        if(Intentinfo.equals("new")){//yeni bir şey konulmak isteniyor yani, eğer menüden 'Add Location' a basmışsa kullanıcı yeni bir lokasyon eklemek istiyor

            binding.saveButton.setVisibility(View.VISIBLE);//save butonu gösterilsin
            binding.deletButton.setVisibility(View.GONE);//delete butonunu yeni bir şey eklenmek istediği zaman tamamen yok et

            //kullanıcının nerede olduğunu almak için 2 tane şey kullanmalıyız. 1) LocationManager   2) LocationListener
            locationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);//android işletim sisteminden lokasyon servislerini alyor
            locationListener=new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {//konum değiştiğinde ne olacağını yazdığımız yer
                    //bu move to camera kodlarından dolayı uygulama düzgün çalışmıyor. Uygulama açılıyor ve sonrasında haritayı kullanıcı hareket ettirse dahi bu kodlar yüzünden kamera hep bulunduğumuz konuma gidiyor. Yüzden şimdi buraya uygulama sadece ilk açıldığında kamerayı konumuma götür algoritması yazacağız.

                    SharedPreferences sharedPreferences=MapsActivity.this.getSharedPreferences("com.ali.javamaps",MODE_PRIVATE);
                    boolean info=sharedPreferences.getBoolean("info",false);//uygulama ilk açılığında info diye bi şey olmadığı için false döndürecek ama sonrasında info olacağından true olacak

                    if(info==false){
                        LatLng userLocation=new LatLng(location.getLatitude(),location.getLongitude());//Location veritipini Latlng'a çevirdik çünkü moveCamera fonksiyonu içerisinde bizden Latlng veritipini istyor ama bizim locationumuz location veritipinde
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15));//kamerayı kullanıcının bulunduğu yere götür ve zoomla
                        mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location!"));

                        sharedPreferences.edit().putBoolean("info",true).apply();//şidmi sharedPreferences içerisindeki değeri false aptık ve bu kod bloğu tekrar çalıştığında burası true olacağından doalyı if bloğuna girmeyecek kod ve bize yaşattığı kameranın oynamaması sorununu bu algoritmayla çözmüş olduk

                    }


                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {//bazen bunu yazmazsak sorun oluyor diye biz de yazdık. LocationListener içerisinde onSt yazarsak zaten direk burayı çıkartıyor. Ezber yani bura, takılcak bir şey yok.
                    LocationListener.super.onStatusChanged(provider, status, extras);
                }
            };



            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener); kullanıcıdan lokasyon değişimlerini iste


            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                //kullanıcıdan izin al
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){//burası android tarafından kullanici iknasi icin kullanilan alan
                    //kullanıcıdan izin al
                    Snackbar.make(binding.getRoot(),"Permission needed for maps",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //kullanıcıdan izin al
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);

                        }
                    }).show();


                }else{
                    //kullanıcıyı ikna et, edilmezse tekrar izin iste
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);//iznimizi istedik

                }

            }else{
                //izin verilmiş zaten. kullanıcıdan verileri al
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);//konumu alma

                //uygulama kapatılıp açıldıktan sonra kamera direkt dünya ölçeğine çıkıyor ve çok uzakta kalıyor sonrasında neredeysek oraya odaklıyor ancak biz direkt son bilinen konum(last known location) 'da uygulamayı başlatmak istiyoruz. o yüzden şimdi bazı kodlar yazacağız
                Location lastLocation= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);//son bilinen konumu bu şekilde alabiliyoruz

                if(lastLocation!=null){//eğer son lokasyon verisi boş değilse if'i çalıştır
                    LatLng lastLocationLatlng=new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocationLatlng,15));
                }

                mMap.setMyLocationEnabled(true);//konumumun doğruluğundan emin ol dedik ve o haritada ki mavi çember çıktı

            }


        }else{  //kaydettiği lokasyona bakmak istiyor

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
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);//konumu alma

                        Location lastLocation= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);//son bilinen konumu bu şekilde alabiliyoruz

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


    @Override//  mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location!"));

    public void onMapLongClick(@NonNull LatLng latLng) {//uzun basma özelliği için ennn yukarıdaki satırda(MapsActivity de) implemente ettiğimiz GoogleMap.OnMapLongClickListener satırı hata verince methodları ugula dedik ve bura yazıldı. İçeride ki Latlang latlang da bize nereye tıklandığının enlem boylamını veriyor.
        mMap.clear();//Her tıklanmadan sonra haritayı markerlardan temizliyor. Bu da her işaretlenen yerin haritada kalmamasını, her işaretlemeden sonra eskisinin silinmesini sağlıyor.
        mMap.addMarker(new MarkerOptions().position(latLng).title("You choose here!"));

        selectedLatitude=latLng.latitude;
        selectedLongitude=latLng.longitude;

        binding.saveButton.setEnabled(true);//kullanıcı haritada bir yer seçene kadar save butonu aktif olmayacak
        binding.deletButton.setEnabled(true);
    }

    public void save(View view){//save butonunun onclick methodu

        Place place=new Place(binding.placeNameText.getText().toString(),selectedLatitude,selectedLongitude);

        //Threading -> MainThread ya da UI Thread denen şeyler var. Ana thread: Ana işlemlerin yapıldığı yer. Kullanıcı arayüzüyle ilgili işlemleri yapıyoruz burada ve eğer burada yüklü işlemler yaparsak eğer kullanıcı arayüzünü bloklayabilir kullanıcının uygulamasını çökertebiliriz. O yüzden tavsiye edilmez. 2tane Thread var. 1) Default (Cpu intentisve)   2)IO (Network, database). Bizden burada istenmeyen şey cpu intensive default olanda işlem yapmamız ama şimdi biz io yani networkte işlem yapıcaz. Network demek te internetten bir veri istemek mesela.

        //placeDao.insert(place).subscribeOn(Schedulers.io()).subscribe();//burada thread'i defaulttan alıp io'a verdik ve uygulamanın kullanıcı arayüzünü çökertmesini engelledik.

        //disposable (kullan at / tek kullanımlık)

        compositeDisposable.add(placeDao.insert(place)
                .subscribeOn(Schedulers.io())//placeDao'yu io thread'de yani networkte yap, kullanıcının arayüzünü çökertme
                .observeOn(AndroidSchedulers.mainThread()) //io threadde yap ama mainthread de göster, orada gözlemlicem
                .subscribe(MapsActivity.this::handleResponse));//mapsActivity.this'e subscribe olacağım demek. arkaplanda işlemi yapıcam ama sonucu mainthreadde gözlemlicem demek bu

        //burada aslında 6 satır önce placeDao.insert diye başladığımız kodda yaptığımızın aynısına yakınını yaptık ama o kullandıktan sonra sil ki yer kaplamasın olayı için buraya yazdık. Dispoisble'lar tek kullanımlıktı biliyorsun
}

    private void handleResponse(){//subscribe işlemi bittikten sonra bir fonksiyon çalıştırmak istiyorsak, örneğin mainActivty'e geri dönmek istiyorum. Buradan yapcaz sonra hemen 2 satır yukarıdaki subscribe() içerisine bu fonksiyonu parantezsiz bir şekilde vericez
        Intent intent=new Intent(MapsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//flag ekleyip herşeyi temizledik
        startActivity(intent);
    }

    public void delete(View view){//delete butonunun onclick metodu

            compositeDisposable.add(placeDao.delete(selectedPlace)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(MapsActivity.this::handleResponse));

    }

    @Override
    protected void onDestroy() {//bunu yazarsam daha önce yazdığım tüm kollar çöpe atılıyor ve hafızada yer tutmuyor. Bu flowable, compotable, singular falan bunlar desposable olarak 1 defa kullanıldıktan sonra çöpe atılmak suretiyle yer kaplamaması sağlanıyor.
        super.onDestroy();
        compositeDisposable.clear();//daha önce yaptığım veriler buradan çöpe atılıyor ve hafızamda yer tutmuyor. RxJava'nın kalıntıları atılıyor(flowable, compotable, singular, observable)
    }
}