# Takip — Yapılacaklar + Fiyat Takip (Android)

İki modüllü Android uygulaması iskeleti:
- **Yapılacaklar**: basit todo listesi (ekle / işaretle / sil)
- **Fiyat Takip**: ürün ekle, pazaryeri fiyatlarını periyodik kontrol et, fiyat düşünce bildirim

## Teknoloji
- Kotlin + Jetpack Compose (Material 3)
- Room (yerel veritabanı)
- WorkManager (periyodik fiyat kontrolü, varsayılan 6 saat)
- Bottom navigation, basit servis locator (Hilt yok)

## Mevcut durum (v0.1)
Fiyatlar şimdilik **sahte kaynaktan** (`FakePriceSource`) geliyor — her kontrolde
fiyatları rastgele oynatır ki bildirim akışı baştan sona çalışsın.
Gerçek pazaryerleri (Hepsiburada, Trendyol, Amazon TR, N11, DJI Store) daha sonra
`PriceSource` arayüzünü implemente ederek eklenecek.

## Derleme

### Android Studio (önerilen)
1. Android Studio'da **Open** → bu klasörü seç.
2. İlk açılışta Gradle/AGP sürüm uyarısı çıkarsa "Upgrade"i kabul et.
3. Bir cihaz/emülatör seç → **Run**.

### Komut satırı
```bash
# local.properties oluştur (SDK yolu)
echo "sdk.dir=ANDROID_SDK_YOLU" > local.properties

./gradlew assembleDebug
# çıktı: app/build/outputs/apk/debug/app-debug.apk
```

> Not: Sürüm numaraları (`gradle/libs.versions.toml`) yazıldığı tarihte günceldi;
> ilk senkronda Android Studio küçük güncellemeler önerebilir, kabul etmen yeterli.

## Sıradaki adımlar
- [ ] Gerçek pazaryeri fiyat kaynakları (`PriceSource` implementasyonları)
- [ ] Fiyat geçmişi grafiği (ürün detay ekranı)
- [ ] Bildirime tıklayınca ürün sayfasını açma
- [ ] Kontrol aralığını ayarlardan değiştirme
- [ ] Backend (Flask) + FCM ile sunucu taraflı tarama (cihaz pili dostu)
