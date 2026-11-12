# BG Prices — Android testing build

Native Android app prototype for comparing prices in Bulgaria.

## Scope of this first build
- Android only; Huawei-specific support is intentionally deferred to a separate release.
- EUR only.
- Category browsing for groceries, technology, furniture/home improvement, and petrol stations.
- Retailer directory seeded for the requested Bulgarian market.
- Demo catalogue only: this build does **not** fetch live retailer prices yet.

## Build
GitHub Actions builds the debug APK on pushes to `main` and on pull requests.
Open the repository's **Actions** tab, select **Android debug build**, then download the `bg-prices-debug-apk` artifact from a successful run.

## Run locally
Open the `android` folder in Android Studio (JDK 17, Android SDK 35), sync Gradle, then run the app on an emulator or Android device.

## Next steps
Connect retailer feeds/admin price entry, store and location filtering, basket comparison, barcode matching, and moderation. Do not treat sample data as current retail prices.
