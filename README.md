# BG Prices Preview

Native Android preview application for comparing prices in Bulgaria.

## Product scope
- Android/Kotlin/Jetpack Compose only in this build.
- Huawei-specific app distribution and HMS integration are not built in this stage.
- The backend database and API are designed to be shared by future Android distribution channels.
- EUR only.
- Categories: groceries, technology, furniture/home improvement, and petrol stations.
- The Categories tab displays retailers with mapped active branches in the selected locality/radius and retailers with verified nationwide online offers.
- The preview database includes a small, time-limited snapshot of offers manually checked on official Bulgarian retailer websites. These are national online prices, not confirmed prices or stock at a physical branch; they are not a live retailer feed.
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

The products and price-observation schema supports branch prices and nationwide online prices. The preview seed is in `backend/src/main/resources/db/demo/sample_offers_bg_2026-11-12.sql` (25 product/offer examples from Lidl, Ardes, Technopolis, IKEA and Praktiker). Every row retains its official retailer source URL, EUR price, collection timestamp, validity window and an explicit note that branch stock is not confirmed. Sample prices expire quickly; they are for preview demonstration only and must not be treated as a live feed. Missing prices are not treated as zero.

Submissions are anonymous and stored with PENDING status. The preview does not expose moderation actions or account management endpoints. The API applies bounded in-memory request limits; production deployment still requires a trusted edge proxy, authentication/authorization for administrative workflows, and deployment-level monitoring.

## Location and store data
- Category and retailer reference records are seeded through PostgreSQL migrations.
- The branch and locality directory is imported from OpenStreetMap. Only recognized retailer aliases are imported.
- Store features without coordinates or a reliable mapped locality within 25 km are skipped. If OSM has no street address, the branch is retained with locality-level address text rather than an invented street address. Bulgarian and Latin-script locality names are matched against the same mapped settlement.
- OpenStreetMap data is community-maintained and may be incomplete. The app exposes source and refresh information; coverage depends on mapped data.
- The OSM importer is a one-shot job activated with Spring profile `osm-import`; it is not yet scheduled or executed against the hosted database. The intended refresh cadence is every 12 hours after a dedicated job runner is configured.
- The one-shot job uses connection/read timeouts and preserves existing rows when an Overpass request fails.
- Import batching, stale-branch reconciliation and runtime monitoring remain to be completed before production.
- Retailer prices and promotions are a separate pipeline. Preferred order: official retailer API/feed or permitted partner feed; otherwise reviewed admin/user submissions with source and validity timestamps. Do not scrape retailer websites unless their terms permit it.

## Run a real-data local demo

The repository includes a local PostgreSQL + API setup. It starts with migrated reference tables; the optional sample-offer SQL file inserts only source-backed national online offers and does not invent physical branches or coordinates.

1. Install Docker Desktop or Docker Engine with the Compose plugin.
2. Copy `.env.example` to `.env` and keep the local-only defaults unless you need to change them.
3. Start PostgreSQL and the API:

   ```sh
   docker compose up --build -d api
   ```

4. Run the one-shot OpenStreetMap import (this needs outbound internet access and may take several minutes):

   ```sh
   docker compose --profile import run --rm osm-import
   ```

5. Optionally load the sourced preview offers (against the local database):

   ```sh
   psql "$DATABASE_URL" -f backend/src/main/resources/db/demo/sample_offers_bg_2026-11-12.sql
   ```

6. Check the service and imported locality catalogue:

   ```sh
   curl http://localhost:8080/actuator/health
   curl "http://localhost:8080/api/v1/localities?query=Veliko"
   curl "http://localhost:8080/api/v1/catalog?locality=Veliko%20Tarnovo"
   ```

The importer uses OpenStreetMap community data and retains OSM source IDs. Some branches will be absent where the map does not contain a reliable retailer match or a locality within 25 km. OSM is not a retailer price feed. The preview's national online offer samples come from the official retailer pages recorded in the SQL seed; they do not assert branch-level availability. Product photos are not yet implemented. OSM attribution is required in any public demo.

The local Compose database is for development/demo use only. Do not expose its PostgreSQL port publicly or reuse the example password in a hosted environment. For the Android emulator, build with `-PBG_PRICES_API_BASE_URL=http://10.0.2.2:8080/`. For a physical Android device, use the development computer's reachable LAN address. The preview APK distributed to users must use the hosted HTTPS API URL, not localhost or a private LAN address.

## Backend configuration
- PostgreSQL connection: `DB_URL`, `DB_USER`, `DB_PASSWORD`.
- Connection pool maximum: `DB_POOL_MAX` (default 5).
- Overpass endpoint: `OSM_OVERPASS_URL`.
- Local development database defaults are for development only. Production secrets must be provided through the hosting provider's secret manager.
- Backend container definition: `backend/Dockerfile`. The preview API is deployed on Render's free Frankfurt web-service plan and uses the Neon PostgreSQL project in Frankfurt. No custom domain, DNS, Cloudflare, Control D or production security edge has been configured.

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
- Automated OSM import scheduling and reconciliation of stale branches.
- Live retailer APIs/feeds and ongoing price refresh.
- Product images and bounded image caching.
- Production security edge, custom domain and operational monitoring.
- Verified retailer price feeds and production price ingestion.
