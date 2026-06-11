# Codex Memory / AI Handoff

Bu dosya repo içeriği ve mevcut bağlamdan oluşturulmuştur. ChatGPT/Codex geçmiş konuşmalarının tamamına erişim yoksa bazı kararlar eksik olabilir.

## Project Overview

`Gürültü Ölçer` / `Noise Meter`, Android mikrofonunu kullanarak ortam ses seviyesini yaklaşık dB cinsinden anlık ölçen ve bunu görsel olarak sunan bir Android uygulamasıdır.

Ana problem:
- Kullanıcının bulunduğu ortamın ses seviyesini hızlıca ve sade bir arayüzle görmesi
- Yüksek gürültüyü fark etmesi
- Temel bir ses seviyesi aracı kullanması

Hedef kullanıcı:
- Günlük kullanım için ortam sesini takip etmek isteyen Android kullanıcıları
- Ev/ofis/okul/çalışma ortamında ses seviyesini yaklaşık olarak görmek isteyen kullanıcılar
- Basit bir dB ölçer arayan, profesyonel kalibrasyon gerektirmeyen son kullanıcılar

## Current Status

- Proje aktif geliştirme aşamasında ve Google Play dağıtımına yönelik hazırlanmış durumda.
- Repo içinde Play Store materyalleri, gizlilik/politika dokümanları, AdMob entegrasyonu ve release `.aab` üretim akışı mevcut.
- `app/build.gradle.kts` içindeki güncel sürüm:
  - `versionCode = 22`
  - `versionName = 0.0.6.6`
- Mevcut branch: `main`
- Repo geçmişi, kapalı test / release hazırlığı / mağaza varlıkları / reklam entegrasyonu üzerinde yoğun çalışıldığını gösteriyor.
- Exact canlı yayın durumu (`internal`, `closed`, `production`) repo içinden kesin doğrulanamıyor; Play Console’dan kontrol edilmesi gerekir.
- Çalışma ağacında şu an commit edilmemiş yerel değişiklikler var:
  - `min / max / average dB` istatistikleri
  - daha büyük canlı grafik görünümü
  - reklam resource ayrımı (`debug` test IDs / `release` prod IDs)

## Tech Stack

- Platform: Android
- Dil: Kotlin
- UI: Jetpack Compose + Material 3
- Build: Gradle Kotlin DSL
- AndroidX:
  - `activity-compose`
  - `lifecycle-runtime-ktx`
  - `appcompat`
- Google SDK / Servisler:
  - Google Mobile Ads / AdMob (`play-services-ads`)
  - Google Play In-App Updates (`app-update`, `app-update-ktx`)
- Ses ölçüm katmanı:
  - `MediaRecorder` tabanlı yaklaşık gürültü ölçümü
- Dil yönetimi:
  - `AppCompatDelegate.setApplicationLocales`
- Local persistence:
  - `SharedPreferences` (uygulama dili seçimi için)

Kullanılmayan ama özellikle dikkat çekenler:
- Firebase yok
- Room yok
- DataStore yok
- Billing yok
- RevenueCat yok
- Network backend / özel API yok

## App / Product Features

Mevcut ürün özellikleri:
- Mikrofon ile canlı ses seviyesi ölçümü
- dB değeri için anlık ana gösterge
- Gürültü sınıflandırması:
  - sessiz
  - rahat / comfort
  - canlı / active
  - yüksek
- Son birkaç saniyeyi gösteren canlı dalga/bar görünümü
- Daha büyük canlı grafik ekranı (yerel değişiklik olarak mevcut)
- Min / Max / Ortalama dB istatistikleri (yerel değişiklik olarak mevcut)
- Eşik alarmı:
  - aç/kapat
  - belirli dB üstü uyarı
- Çoklu dil desteği:
  - Türkçe
  - İngilizce
  - İspanyolca
  - Fransızca
  - Hintçe
  - Basitleştirilmiş Çince
- Uygulama içinden dil değiştirme
- Ayarlar ekranı
- Yasal bilgilendirme bağlantısı
- Hakkında bölümü
- `Reklam izleyerek destek ol` akışı
  - görünür UI sade
  - iç mantıkta arka arkaya reward zinciri kullanılabiliyor
- Alt banner reklam alanı
- Google Play `Güncellemeleri denetle` butonu

## Important Decisions Made

### Ürün / UX kararları

- Uygulama profesyonel bir ölçüm cihazı gibi konumlandırılmıyor.
  - README ve mağaza metinlerinde bunun yaklaşık ölçüm olduğu belirtiliyor.
- Ayarlar ayrı panel / sayfa mantığına taşınmış.
  - Karışık ana ekran yerine daha temiz ürün akışı hedeflenmiş.
- Dil seçimi kullanıcı için görünür ve uygulama içinden değiştirilebilir tutulmuş.
- `Reklam izleyerek destek ol` akışı donation yerine rewarded ads yönünde tasarlanmış.
- Kullanıcı isteği nedeniyle destek akışının görünür metni sade tutulmalı.
  - UI’da sürekli `3 reklam` / `0/3` gibi metin gösterimi istenmiyor.

### Teknik kararlar

- Ölçüm için `MediaRecorder.maxAmplitude` tabanlı basit yaklaşım seçilmiş.
  - Neden: hızlı, minimum bağımlılık, canlı ölçüm için yeterli.
  - Sonuç: farklı cihazlarda ölçüm farkı normal; profesyonel doğruluk beklenmemeli.
- Dil ayarı için `SharedPreferences` + `AppCompatDelegate` kullanılmış.
  - Ayrı bir persistence/mimari katman eklenmemiş.
- AdMob entegrasyonu kullanılıyor.
  - Banner + rewarded destekleniyor.
- Debug ve release reklam kimlikleri ayrılmış.
  - Debug: Google test ad IDs
  - Release: gerçek AdMob IDs
- Reklam ID’leri çevrilebilir locale dosyalarında tutulmamalı.
  - Çünkü dil değişimi yanlış ID çözümüne ve `no fill` / yanlış reklam davranışına yol açabiliyor.
  - Bu yüzden non-localized resource yaklaşımı tercih edilmeli.
- Uygulama içi güncelleme kontrolünde `Flexible update` yaklaşımı seçilmiş.
  - `Immediate update` zorlayıcı olduğu için tercih edilmemiş.

### Bilinçli olarak ertelenen / tamamlanmamış konular

- Profesyonel kalibrasyon / cihaz bazlı doğruluk çözümü yok
- Ölçüm geçmişi kalıcı saklama yok
- Sonuç paylaşımı yok
- Uygulama içi diğer uygulamalar / publisher sayfası yönlendirmesi henüz kodda yok
- Otomatik değerlendirme / share / growth loop tam kurulmuş değil

## Completed Actions

Repo ve commit geçmişinden görülen tamamlanmış işler:

- İlk sürüm ve temel görünüm oluşturuldu
- README yeniden düzenlendi
- Uygulama ikonu güncellendi
- Çoklu dil desteği eklendi
- Ayarlar deneyimi yeniden tasarlandı
- Yasal metinler ve gizlilik politikası dokümanları eklendi
- Google Pages / static docs tarafında gizlilik/politika sayfaları üretildi
- AdMob entegrasyonu eklendi
- Google Play yayın checklist’i oluşturuldu
- Store listing taslağı eklendi
- `Güncellemeleri denetle` özelliği eklendi
- Tek tıkla support ad mantığı iyileştirildi
- Debug/release reklam ayrımı yapıldı
- Çeviri ve metin tutarlılığı üzerinde tekrar tekrar düzeltmeler yapıldı

Commit geçmişinden önemli değişiklikler:
- `080b83ad` Initial version with README and Play Store notes
- `7923a7e6` Add publishable privacy policy page for Play Console
- `ce04fe8a` Fix Turkish encoding and add English privacy policy
- `86377842` Add GitHub Actions workflow for GitHub Pages deployment
- `21278902` Finalize app: localization, threshold alarm, release prep
- `52a22da7` Update settings UX, localization, legal texts, icon, and release 0.0.7.5
- `b3d883dd` 04.06.2026 update flow and ad support improvements

Google Play / store / ASO ile ilgili repo içi tamamlanmış materyaller:
- `docs/play-store-checklist.md`
- `docs/store-listing-tr.md`
- `docs/privacy-policy.html`
- `docs/privacy-policy-en.html`
- `docs/store-assets/icon-512.png`
- `docs/store-assets/icon-1024.png`

## Open Tasks / Next Actions

En mantıklı sıradaki işler:

1. Yerel değişiklikleri gözden geçirip karara bağla:
- `min / max / average dB`
- büyük canlı grafik ekranı
- reklam resource ayrımı

2. Bu yerel değişiklikler isteniyorsa:
- test et
- versiyon yükselt
- release `.aab` al
- commit / push yap

3. Growth / kullanıcı kazanımı için uygulama içi linkler ekle:
- geliştirici sayfasını aç
- uygulamayı paylaş
- uygulamayı değerlendir
- diğer uygulamalarımız

4. Ölçüm geçmişi / istatistik kalıcılığı ekle

5. Sonuç paylaşma özelliği ekle

6. AdMob release davranışını Play’den kurulu sürümde doğrula

7. Mağaza materyallerini güncel ürün özelliklerine göre tekrar hizala
- ekran görüntüleri
- feature graphic
- uzun açıklama / kısa açıklama

8. `codex-memory.md` sonraki büyük ürün değişikliklerinde güncel tutulmalı

## Known Issues / Risks

Bilinen teknik riskler:

- Ölçüm doğruluğu cihazdan cihaza değişir
  - `MediaRecorder.maxAmplitude` profesyonel ölçüm değildir
- Mikrofon verisi farklı telefon/tabletlerde farklı sonuç verebilir
- Rewarded veya banner reklamlar `no fill` verebilir
  - özellikle yeni reklam birimi / yeni hesap / bölgesel envanter / debug install durumlarında
- Google Play üzerinden kurulu olmayan build’lerde In-App Updates gerçek davranışı görülmeyebilir
- Yerel geliştirme sırasında Wi‑Fi ADB portu değişebiliyor
- Locale dosyalarında karışık dil string’leri geçmişte sorun olmuş
- Çeviriler tekrar gözden geçirilmeli; yeni string eklerken tüm diller birlikte güncellenmeli

Google Play / politika riskleri:

- `RECORD_AUDIO` nedeniyle gizlilik politikası zorunlu
- Data Safety formunda AdMob kaynaklı veri işleme tarafı doğru beyan edilmeli
- `AD_ID` beyanı ve `Contains ads` alanı doğru tutulmalı
- Çocuk odaklı dağıtım yapılacaksa AdMob içerik ve yaş derecelendirmesi ayrıca ele alınmalı

Repo hijyeni riskleri:

- Çalışma ağacı sık sık kirli kalabiliyor; commit öncesi hangi değişikliğin release’e girdiği net ayrılmalı
- Repo içinde keystore ile ilgili dosyalar var; içerikleri veya sırları asla kopyalama/yazma

## Build & Run Instructions

Temel komutlar:

```powershell
.\gradlew.bat :app:assembleDebug
```

Release bundle:

```powershell
.\gradlew.bat :app:bundleRelease
```

Çıktılar:
- Debug APK:
  - `app\build\outputs\apk\debug\app-debug.apk`
- Release AAB:
  - `app\build\outputs\bundle\release\app-release.aab`

Wi‑Fi ADB ile kurulum örneği:

```powershell
& "C:\Users\LENOVO\AppData\Local\Android\Sdk\platform-tools\adb.exe" connect 192.168.0.29:PORT
& "C:\Users\LENOVO\AppData\Local\Android\Sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
& "C:\Users\LENOVO\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell monkey -p com.mdstudio.gurultuolcer -c android.intent.category.LAUNCHER 1
```

Notlar:
- Release build için `keystore.properties` mevcutsa signing config otomatik kullanılabilir.
- Debug build test reklamları kullanmalı.
- Release build gerçek AdMob reklam kimliklerini kullanmalı.

Test durumu:
- Repo içinde belirgin otomatik test seti görünmüyor.
- Pratik doğrulama:
  - uygulamayı çalıştır
  - mikrofon iznini kontrol et
  - ölçüm başlat/durdur
  - reklam alanlarını kontrol et
  - dil değişimlerini doğrula
  - Play update check butonunu kontrol et

## Release / Google Play Notes

- Paket adı: `com.mdstudio.gurultuolcer`
- Hedef Android:
  - `minSdk = 26`
  - `targetSdk = 35`
  - `compileSdk = 35`
- Repo içinde Play’e dönük materyaller var:
  - privacy policy
  - store listing draft
  - store checklist
  - icon assets
- AdMob kullanımı mevcut:
  - app ID / banner / rewarded
- İzinler:
  - `RECORD_AUDIO`
  - `INTERNET`
  - `ACCESS_NETWORK_STATE`
  - `AD_ID`

Store / ASO notları:
- Ürün adı iki dilde lokalize
- Kısa ve uzun açıklama taslakları docs altında mevcut
- Icon ve feature asset akışı repo içinde dokümante edilmiş
- Uygulama sade yardımcı araç olarak konumlandırılıyor; aşırı iddialı metinlerden kaçınılmalı

Dağıtım notu:
- Repo ve önceki çalışma izleri kapalı test ve yayın hazırlığı yapıldığını gösteriyor.
- Exact aktif track durumu repo içinden doğrulanamaz; Play Console’dan teyit edilmesi gerekir.

Reklam ve politika notları:
- Debug build’de test reklam, Play/release build’de gerçek reklam kullanılmalı
- Ad safety tarafında içerik derecelendirmesi ve sensitive-category blocking tercih edilmeli
- Tek tek reklam manuel seçimi yapılmıyormuş gibi varsay; politika ve kategori kısıtlama üzerinden ilerle

## AI Agent Instructions

Bu projeyi devralan AI ajanı için kurallar:

- Mevcut mimariyi bozmadan ilerle
- Gereksiz büyük refactor yapma
- Önce küçük ve güvenli değişiklikler yap
- Değişiklikten önce mevcut davranışı oku ve anla
- Dil dosyalarına ekleme yapıyorsan bütün desteklenen locale klasörlerini birlikte kontrol et
- Reklam/resource değişikliği yapıyorsan debug ve release ayrımını tekrar doğrula
- Play’e giden sürümde versionCode/versionName değişikliği kullanıcı talebiyle tam eşleşmeli
- Secret, API key, token, keystore şifresi gibi hassas verileri asla yazma
- Repo içinde hassas dosyalar olsa bile içeriklerini hafıza/rapor dosyalarına kopyalama
- Kullanıcı Wi‑Fi debug ile yükleme isterse build + install + relaunch zincirini tamamla
- Her işlemden sonra kısa ve somut özet ver

## Context Gaps

Repo içinden kesin anlaşılamayan ve kullanıcıdan gerekirse sorulması gerekenler:

- Uygulama şu an Play Console’da hangi track’te aktif:
  - internal
  - closed
  - open
  - production
- Gerçek canlı kullanıcı sayısı / hedef KPI’lar
- Uygulamanın uzun vadeli monetization planı:
  - sadece AdMob mı
  - reklamsız sürüm / IAP düşünüyor mu
- Ölçüm geçmişi, paylaşım ve publisher page yönlendirmesi isteniyor mu
- Çocuklara yönelik hedef kitle seçimi yapıldı mı
- Gelecek sürümde hangi özellik öncelikli:
  - geçmiş
  - paylaşım
  - developer page
  - rating prompt
  - daha gelişmiş alarm
