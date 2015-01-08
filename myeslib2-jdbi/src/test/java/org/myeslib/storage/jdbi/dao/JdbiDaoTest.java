package org.myeslib.storage.jdbi.dao;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.myeslib.core.data.UnitOfWork;
import org.myeslib.core.data.UnitOfWorkHistory;
import org.myeslib.storage.helpers.DbAwareBaseTestClass;
import org.myeslib.storage.helpers.SampleDomainGsonFactory;
import org.myeslib.storage.jdbi.dao.config.DbMetadata;
import org.myeslib.storage.jdbi.dao.config.UowSerialization;

import java.util.Arrays;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.myeslib.storage.helpers.SampleDomain.*;

public class JdbiDaoTest extends DbAwareBaseTestClass {

    Gson gson;
    UowSerialization functions;
    DbMetadata dbMetadata;
    JdbiDao<UUID> dao;

    @BeforeClass
    public static void setup() throws Exception {
        initDb();
    }

    @Before
    public void init() throws Exception {
        gson = new SampleDomainGsonFactory().create();
        functions = new UowSerialization(
                gson::toJson,
                (json) -> gson.fromJson(json, UnitOfWork.class));
        dbMetadata = new DbMetadata("inventory_item");
        dao = new JdbiDao<>(functions, dbMetadata, dbi);
    }

    @Test
    public void firstTransactionOnEmptyHistory() {

        UUID id = UUID.randomUUID();

        UnitOfWorkHistory toSave = new UnitOfWorkHistory();
        UnitOfWork newUow = UnitOfWork.create(UUID.randomUUID(), new IncreaseInventory(UUID.randomUUID(), id, 1, 0L), Arrays.asList(new InventoryIncreased(1)));
        toSave.add(newUow);

        dao.append(id, newUow);

        UnitOfWorkHistory fromDb = dao.getFull(id);

        assertEquals(toSave, fromDb);

    }

    @Test
    public void appendNewOnPreviousVersion() {

        UUID id = UUID.randomUUID();

        UnitOfWork existingUow = UnitOfWork.create(UUID.randomUUID(), new IncreaseInventory(UUID.randomUUID(), id, 1, 0L), Arrays.asList(new InventoryIncreased(1)));
        UnitOfWorkHistory existing = new UnitOfWorkHistory();
        existing.add(existingUow);

        UnitOfWork newUow = UnitOfWork.create(UUID.randomUUID(), new DecreaseInventory(UUID.randomUUID(), id, 1, 1L), Arrays.asList(new InventoryDecreased(1)));

        dao.append(id, existingUow);

        dao.append(id, newUow);

        UnitOfWorkHistory fromDb = dao.getFull(id);

        assertEquals(fromDb.getLastVersion().intValue(), 2);


    }

    @Test(expected = Exception.class)
    public void databaseIsHandlingOptimisticLocking() {

        UUID id = UUID.randomUUID();

        UnitOfWork existingUow = UnitOfWork.create(UUID.randomUUID(), new IncreaseInventory(UUID.randomUUID(), id, 1, 0L), Arrays.asList(new InventoryIncreased(1)));

        UnitOfWorkHistory existing = new UnitOfWorkHistory();

        existing.add(existingUow);

        dao.append(id, existingUow);

        UnitOfWork newUow = UnitOfWork.create(UUID.randomUUID(), new DecreaseInventory(UUID.randomUUID(), id, 1, 0L), Arrays.asList(new InventoryDecreased(1)));

        dao.append(id, newUow);

    }

    // TODO batchCommitTest and batchRollbackTest

}
