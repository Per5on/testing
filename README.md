# BG Prices — Android testing build

Native Android app prototype for comparing prices in Bulgaria.

## Scope
- Android only; Huawei-specific support is deferred to a separate build.
- EUR only.
- Categories: groceries, technology, furniture/home improvement, and petrol stations.
- Category and retailer reference data is stored in the backend catalogue.
- The Categories tab is designed to show only retailers with a mapped active branch in the selected locality or radius.
- No live retailer prices are claimed until a verified retailer feed or reviewed price submission is available.

## Location and store data
- Backend endpoint: `GET /api/v1/catalog?locality=Veliko%20Tarnovo` or `GET /api/v1/catalog?latitude=43.0757&longitude=25.6172&radiusKm=10`.
- Category metadata and retailer reference records are versioned in PostgreSQL migrations.
- The backend imports mapped populated places and recognized retailer branches from OpenStreetMap using a scheduled 12-hour refresh.
- Import matching is restricted to known retailer names/brands. Branches without usable locality/address information are skipped rather than assigned invented coordinates.
- OpenStreetMap data is community-maintained and may be incomplete. The app exposes the source and refresh interval; OSM attribution is shown in Profile & Settings.
- The first import runs shortly after backend startup. Configure `OSM_INITIAL_DELAY_MS` and `OSM_REFRESH_MS` to change timing.
- The importer retains existing data if an Overpass request fails. It does not delete stale branches automatically.
- Retailer prices and promotions are a separate pipeline. Preferred order: official retailer API/feed or permitted partner feed; otherwise reviewed admin/user submissions with source and validity timestamps. Do not scrape retailer websites unless their terms permit it.
- A 12-hour interval is appropriate for the category/retailer directory and mapped branch data, but price freshness should follow each retailer's feed/update cadence. Price snapshots should retain source, collection time, and promotion validity.

## Connecting Android to the backend
Set the GitHub Actions repository variable `BG_PRICES_API_BASE_URL` to the deployed backend base URL, ending in `/` (for example, `https://api.example.com/`). The Android app defaults to an unconfigured state rather than showing a fabricated list of available local stores.

## Builds
Build workflows are currently **manual only**. Do not start a new Android or backend build until the project owner gives the go-ahead. After approval, open the repository's **Actions** tab and run the required workflow with **Run workflow**. Android APK artifacts use unique names, monotonically increasing Android `versionCode` values, and 90-day retention; previous artifacts are not overwritten.

## Run locally
Open the `android` folder in Android Studio (JDK 17, Android SDK 35). To connect a local backend, pass `-PBG_PRICES_API_BASE_URL=http://10.0.2.2:8080/` for the Android emulator or the reachable LAN URL for a physical device.
