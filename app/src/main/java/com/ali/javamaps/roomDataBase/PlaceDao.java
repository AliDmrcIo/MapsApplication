package com.ali.javamaps.roomDataBase;

import static android.icu.text.MessagePattern.ArgType.SELECT;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.ali.javamaps.model.Place;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

//Tüm bu bilgiler için bu sayfayı ziyaret edebilirsin https://developer.android.com/training/data-storage/room/async-queries#java


@Dao
public interface PlaceDao {//burası interface olmalı ve database de normalde yaptığımız insert into, create table if not exists, delete falan filan yaptığımız normal database'in kullanımını yaptığımız kısım. Tabi bunları room sayesinde 1,2 satır kodla yapıcaz, normalde eşşeğin si* i gibi uğraştırıyor

    @Query("SELECT * FROM Place")    //Bu yarattığımız database yani tabloyu gösterecek ve içerisine "SELECT * FROM Place" yazmamızın sebebi de * ile alakalı, yeri gelcek herşeyi göstermek istemeyeceğiz falan filan. Bu çift tırnak arasına filtreleme vs de yapabiliriz. WHERE id =2 falan gibi. Burada normalde SQl de yazdığımız select* from diye başlayan satırda ne yazabiliyorsak burada da yazabiliriz.
    Flowable<List<Place>> getAll(); //Query bana List veritipinde veriler döndürecek. Aşağıdakiler bir şey döndürmeyeceğinden void yazdım ama bu döndüreceğinden voidsiz yazdım


    @Insert
    Completable insert(Place place);//Insert into kısmını, @Insert yardımıyla kolay bir şekilde yapabileceğim

    @Delete
    Completable delete(Place place);//@Delete yardımıyla SQLite'ta yaptığımız Delete kodunu yapabilcez kolaylıkla

    //Buralarda yazan bu Completable kelimeleri yerine void yazıyordu. Ancak sonradan biz RXjava ile etkileşimde yapabilmek için bunu Completable ve yukarıya da Flowable yazdık. Bura void döndürüyordu ancak artık Completable diye bir sınıf döndürüyo. Yukarıda da sadece List döndürmeyecek bir de Flowable döndürecek. Peki biz bu Rxjava'ya neden ihtiyaç duyuyoruz? Çünkü biz eğer bu Rxjava işini kullanmazsak bu database işlemlerini MainThread'de yani kullanıcı arayüzünde yapmış oluyoruz ve bu da android tarafından istenmeyen bir şey, aslında önceden de bunu yaptık Runnable Handler falanda. Yani arka planda çalışsın bu database işleri diye RxJava kullanıyoruz, o da bizden bu Completable ve flowable işlerini yapmamızı istiyor.
    //aslında biz bu ön planda yürütme işlemini burada yapabiliriz verilerimiz küçük olduğundan -> Şu şekilde yaparız: db= Room.databaseBuilder(getApplicationContext(),PlaceDatabase.class,"Places").allowMainThreadQueries().build() MapsActivity de bulunan ve .allowMainThreadQueries() bulunan bu ifade aslında ben mainthread'de uygulamanın çalışmasını istiyorum demek ve bunu böyle yazınca sorun çıkmıyor ve çalışıyor ancak dediğimiz gibi aga bu bize android tarafından önerilirmiyor.



}
