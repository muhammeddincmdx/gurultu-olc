# Gürültü Ölçer

Android mikrofonu ile ortam ses seviyesini (dB) anlık ölçen, canlı görselleştirme sunan ve alt bölümde AdMob banner reklam gösteren uygulama.

## Sürüm Notu
- 25.04 ilk version genel görünüm sağlandı ve reklam alanı eklendi

## Android Desteği
- `minSdk = 26` -> Android 8.0 (Oreo) ve üzeri
- `targetSdk = 35`
- `compileSdk = 35`

## Kullanılan İzinler
- `RECORD_AUDIO`: Gürültü ölçümü için mikrofon erişimi
- `INTERNET`: AdMob reklam isteği için
- `ACCESS_NETWORK_STATE`: Ağ durumuna göre reklam davranışı için

## Google Play Store'a Yüklerken Veri Beyanı (Data Safety)
Bu projede `Google Mobile Ads SDK (AdMob)` kullanıldığı için, Play Console Data Safety formunda SDK'nin topladığı/paylaştığı veriler de beyan edilmelidir.

Bu uygulama özelinde dikkate alınacak başlıklar:
- Mikrofon verisi: Ölçüm için cihaza erişilir; mevcut kodda ses verisi sunucuya gönderilmez.
- AdMob tarafı (otomatik): IP adresi, kullanıcı etkileşimi (tıklama/gösterim), tanılama bilgileri, cihaz ve hesap tanımlayıcıları (ör. reklam kimliği / app set ID) reklam, analiz ve sahtekarlığı önleme amaçlarıyla işlenebilir.
- Veriler aktarım sırasında şifreli iletilir (SDK dokümantasyonuna göre).

Play Console'da ayrıca:
- Gizlilik Politikası URL'si eklenmeli.
- Uygulamanın veri toplama/paylaşım davranışı tüm dağıtılan sürümlerin toplamını yansıtacak şekilde işaretlenmeli.
- Çocuklara yönelik dağıtım varsa AdMob çocuk/yaş politikaları ayrıca yapılandırılmalı.

## Build
```bash
./gradlew assembleDebug
```
