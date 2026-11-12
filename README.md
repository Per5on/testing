# BG Prices Preview

Native Android preview application for comparing prices in Bulgaria.

## Product scope
- Android/Kotlin/Jetpack Compose only in this build.
- Huawei-specific app distribution and HMS integration are not built in this stage.
- The backend database and API are designed to be shared by future Android distribution channels.
- EUR only.
- Categories: groceries, technology, furniture/home improvement, and petrol stations.
- The Categories tab displays only retailers with a mapped active branch in the selected locality or radius.
- No live retailer prices are claimed until a verified retailer feed or reviewed price observation is available.
- User account and secure personal profile functionality are intentionally deferred. The preview does not collect or store account profile records.

## Android preview
- App display name: **BG Prices Preview**.
- Build version name follows `0.1.<GitHub run number>.preview`.
- GitHub Actions produces a directly installable debug APK with a unique filename and immutable artifact name.
- APK artifacts are retained for 90 days.
- AAB generation and Google Play/AppGallery publishing are deferred to the future store-release stage.
- The preview app disables Android backup and cleartext network traffic in the main manifest. Cleartext is enabled only in the debug manifest to support a local development API.
- Local shopping lists are stored on-device using Room. They contain item names, quantities and category codes only.
- Category order, language and location preferences are stored locally. No account/profile data is stored.
- The basket is a local list at this stage; price comparison results require verified price data from the backend.

## Backend API
- `GET /api/v1/catalog?locality=Veliko%20Tarnovo`
- `GET /api/v1/catalog?latitude=43.0757&longitude=25.6172&radiusKm=10`
- `GET /api/v1/localities?query=Veliko`
- `GET /api/v1/products?categoryCode=groceries&query=milk&limit=30&offset=0`
- `GET /api/v1/prices/latest?productId=<uuid>`
- `GET /api/v1/prices/history?productId=<uuid>&branchId=<uuid>&limit=30`
- `POST /api/v1/submissions` accepts suggestion, note and opinion submissions for moderation.
- `GET /actuator/health` exposes health status without detailed infrastructure information.

The products and price-observation schema is ready for validated data ingestion. No sample products or prices are inserted. Prices must be positive EUR values, retain source and collection timestamp, and be verified before appearing in the latest-price endpoint. Missing prices are not treated as zero.

Submissions are anonymous and stored with PENDING status. The preview does not expose moderation actions or account management endpoints. The API applies bounded in-memory request limits; production deployment still requires a trusted edge proxy, authentication/authorization for administrative workflows, and deployment-level monitoring.

## Location and store data
- Category and retailer reference records are seeded through PostgreSQL migrations.
- The branch and locality directory is imported from OpenStreetMap. Only recognized retailer aliases are imported.
- Branches without usable locality/address data are skipped rather than assigned invented coordinates.
- OpenStreetMap data is community-maintained and may be incomplete. The app exposes source and refresh information; coverage depends on mapped data.
- The OSM importer is no longer scheduled inside the API service. It is a one-shot job activated with Spring profile `osm-import`; the intended future scheduler is Cloud Scheduler triggering a Cloud Run Job at a 12-hour cadence.
- The one-shot job uses connection/read timeouts and preserves existing rows when an Overpass request fails.
- Import batching, stale-branch reconciliation and runtime monitoring remain to be completed before production.
- Retailer prices and promotions are a separate pipeline. Preferred order: official retailer API/feed or permitted partner feed; otherwise reviewed admin/user submissions with source and validity timestamps. Do not scrape retailer websites unless their terms permit it.

## Backend configuration
- PostgreSQL connection: `DB_URL`, `DB_USER`, `DB_PASSWORD`.
- Connection pool maximum: `DB_POOL_MAX` (default 5).
- Overpass endpoint: `OSM_OVERPASS_URL`.
- Local development database defaults are for development only. Production secrets must be provided through the hosting provider's secret manager.
- Backend container definition: `backend/Dockerfile`. No hosting account, deployment, DNS change or cloud resource has been created.

## Build and test
Preview branch: `preview/2026-11`.

Android and backend workflows run on pushes to `preview/**` branches and can also be started manually. They do not run automatically on the main branch.

For local Android development, open the `android` folder in Android Studio with JDK 17 and Android SDK 35. To connect a local backend, pass `-PBG_PRICES_API_BASE_URL=http://10.0.2.2:8080/` for the Android emulator or a reachable LAN URL for a physical device.

For local backend development, use JDK 17 and run `gradle -p backend build`. Set `SPRING_PROFILES_ACTIVE=osm-import` only when intentionally executing the one-shot OpenStreetMap import job against a configured database.

## Deferred
- Huawei-specific HMS implementation and AppGallery release channel.
- User accounts and protected personal profile data.
- Google Play / AppGallery AAB release generation and signing.
- Final architecture diagram.
- Hosting deployment, DNS, Cloudflare/Control D configuration and production secrets.
- Verified retailer price feeds and production price ingestion.
