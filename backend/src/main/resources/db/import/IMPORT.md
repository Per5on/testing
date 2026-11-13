# Beta database import

Runtime catalogues must be loaded from PostgreSQL/API responses, not hardcoded Compose collections.

## Import files
- `branches.csv`: physical branch addresses transcribed from official retailer websites/apps. Include source URL and verification timestamp.
- `online-prices.csv`: national online offers with product page URL, collection timestamp and validity window.
- `fuel-prices.csv`: station-specific prices linked to a verified station branch and permitted source page.
- Templates are header-only until verified rows are available. Never add guessed addresses or prices.

## Import process
1. Create a `data_import_batches` row and load branch CSV rows into `branch_import_staging`.
2. Review every address and source URL; run the branch insert section of `promote_reviewed_imports.sql`.
3. Load price CSV rows into `price_import_staging`; use `NATIONAL_ONLINE` for website offers and `BRANCH` only for explicitly branch-specific prices.
4. Set `verified=true` only after checking the source and run the product/price insert section of `promote_reviewed_imports.sql`.
5. Load station-specific observations into `fuel_price_observations`, tied to a verified branch and source URL.
6. Deduplicate repeat imports by source, entity, value and observation timestamp.

## Data integrity
- Physical addresses are text fields and are not generated from town names or coordinates.
- A national online offer does not prove in-store stock.
- Fuel prices must be station-specific, not chain averages.
- The database records the source URL, collected timestamp and validity window so refresh jobs can revisit the same source.