# Fold Penaltı — Android

Samsung Galaxy Z Fold için 3D penaltı oyunu. Oyun `app/src/main/assets/index.html` içinde (tek dosya, Three.js dahil);
`MainActivity` tam ekran bir WebView açar ve Jetpack WindowManager ile menteşe konumunu sayfaya bildirir
(`window.__hinge(oran)`), böylece Flex modunda üst ekran saha, alt ekran vuruş alanı olur.

## APK üretmek

### Seçenek A — GitHub Actions (bilgisayara kurulum gerekmez)
1. GitHub'da boş bir repo aç, bu klasörün içeriğini yükle (`.github` klasörü dahil).
2. **Actions** sekmesi → "Build APK" → tamamlanınca **Artifacts** altından `FoldPenalti-apk` indir.
3. Zip içindeki `FoldPenalti.apk` dosyasını telefona at, "bilinmeyen kaynak" izni verip kur.

### Seçenek B — Android Studio
Klasörü aç (Hedgehog veya yenisi, JDK 17), **Build → Build APK(s)**. Çıktı: `app/build/outputs/apk/debug/app-debug.apk`.

### Seçenek C — komut satırı
```
./gradlew assembleDebug
```

## Oyunu güncellemek
`index.html` dosyasını değiştirip tekrar derlemek yeterli; Android tarafına dokunmak gerekmez.
`versionCode` değerini `app/build.gradle` içinde artırmayı unutma, yoksa telefon üzerine kurmaz.

Debug imzası ile imzalıdır: kurulur ve çalışır, ama Play Store için `release` imzası gerekir.
