package sampledomain.aggregates.inventoryitem.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import org.h2.jdbcx.JdbcConnectionPool;
import org.myeslib.infra.WriteModelDao;
import org.myeslib.infra.dao.config.DbMetadata;
import org.myeslib.stack1.infra.dao.Stack1JdbiDao;
import org.myeslib.stack1.infra.helpers.jdbi.DatabaseHelper;
import org.skife.jdbi.v2.DBI;
import sampledomain.aggregates.inventoryitem.InventoryItem;

import java.util.UUID;

public class InventoryItemJdbiModule extends AbstractModule {

    @Provides
    @Singleton
    public DBI dbi() {
        return new DBI(JdbcConnectionPool.create("jdbc:h2:mem:test;MODE=Oracle", "scott", "tiger"));
    }

    @Provides
    @Singleton
    public DatabaseHelper databaseHelper(DBI dbi){
        return new DatabaseHelper(dbi, "database/V1__Create_inventory_item_tables.sql");
    }

    @Override
    protected void configure() {

        bind(new TypeLiteral<DbMetadata<InventoryItem>>() {
        }).toInstance(new DbMetadata<>("inventory_item"));

        bind(new TypeLiteral<WriteModelDao<UUID, InventoryItem>>() {
        }).to(new TypeLiteral<Stack1JdbiDao<UUID, InventoryItem>>() {
        }).asEagerSingleton();
    }
}
