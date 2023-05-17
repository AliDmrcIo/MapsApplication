package com.ali.javamaps.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

//burası room oluştururken steplerden biri olan Entitiy step'i
//tableName demek tablomuzun adı, database de ki excel gibi olan sütun ve satırların oluşturduğu tablonun adıdır

@Entity(tableName = "Place") //internet sitesinde de gösterildiği üzere bunu buraya yazmazsak, room bunu kullanacağımı anlamaz. O yüzden önemli.
public class Place implements Serializable {//bunu seri olarak yollamak için implements serializable dedik. RecyclerAdapter Class'ında ki intent.putExtra("place",placeList.get(position)); satırı hata vermesin diye bu implementasyonu yaptık


    @PrimaryKey(autoGenerate = true) //yine sitede(https://developer.android.com/training/data-storage/room) gösterildiği üzere eğer room için id oluşturacaksan bu @PrimaryKey yazmalısın diyor, o yüzden yaptık. autoGenerate = true diyerekte bütün idlerin bizim için olutşurulmasını sağladık, bunu yazdıktan sonra hiçbir şey yapmamıza gerek yok
    public int id;

    @ColumnInfo(name="name") //sütunların isimlerini veriyoruz bu şekilde. Burada ki ismi, kullanıcının activity_maps.xml'de name editText'ine girdiği isime eşitlicem
    public String name;

    @ColumnInfo(name="latitude") //buradaki enlem ve boylam bilgilerini de activity_maps.xml'de kullanıcının tıklayıp kaydetmek istediği yerin enlem ve boları olarak alıcam, yani kullancıının işaretlediği yerlere eşitlicem bunları
    public double latitude;

    @ColumnInfo(name="longitude")
    public double longitude;

    public Place(String name, double latitude, double longitude){//constructor'ımızı da oluştuduk. id istemedim çünkü id zaten benim için otomatik oluşturuluyor. tanımlamama gerek yok


        this.name=name;
        this.latitude=latitude;
        this.longitude=longitude;
    }
}
