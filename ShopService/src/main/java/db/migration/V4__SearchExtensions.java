package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V4__SearchExtensions extends BaseJavaMigration {
  @Override
  public void migrate(Context context) throws Exception {
    try (var statement = context.getConnection().createStatement()) {
      String db = context.getConnection().getMetaData().getDatabaseProductName();
      if (db.equals("PostgreSQL")) {
        statement.execute("CREATE EXTENSION IF NOT EXISTS fuzzystrmatch");
        statement.execute("CREATE EXTENSION IF NOT EXISTS pg_trgm");
        statement.execute(
            "CREATE INDEX products_name_trigram ON products USING gin(lower(name) gin_trgm_ops)");
      } else if (db.equals("H2")) {
        statement.execute(
            "CREATE ALIAS IF NOT EXISTS LEVENSHTEIN_LESS_EQUAL FOR 'com.shopapp.ShopService.service.EditDistance.distance'");
      } else throw new IllegalStateException("Search supports PostgreSQL and H2 only");
    }
  }
}
