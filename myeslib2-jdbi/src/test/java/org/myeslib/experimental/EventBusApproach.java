package org.myeslib.experimental;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.myeslib.core.data.Snapshot;
import org.myeslib.core.storage.SnapshotReader;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.myeslib.jdbi.helpers.SampleDomain.*;

public class EventBusApproach {

//    static JdbcConnectionPool pool ;
//    static DBI dbi ;
//    static Gson gson ;
//    static AggregateRootTablesMetadata metadata ;
//    static Function<UnitOfWork, String> toStringFunction;
//    static JdbiUuidReaderDao arReader;
//
//    private Map<UUID,Snapshot<InventoryItemAggregateRoot>> lastSnapshotMap;
//    private Supplier<InventoryItemAggregateRoot> newInstanceSupplier;
//
//    @BeforeClass
//    public static void setup() throws IOException {
//        pool = JdbcConnectionPool.create("jdbc:h2:mem:test;MODE=Oracle", "scott", "tiger");
//        dbi = new DBI(pool);
//        gson = new SampleDomainGsonFactory().create();
//        metadata = new AggregateRootTablesMetadata("inventory_item");
//        toStringFunction = new UowToStringFunction(gson);
//        arReader = new JdbiUuidReaderDao(metadata, dbi, new UowFromStringFunction(gson));
//        initDb();
//    }
//
//    @Before
//    public void init() {
//        lastSnapshotMap = new HashMap<>();
//        newInstanceSupplier = () -> new InventoryItemAggregateRoot();
//    }
//
//    @Test
//    public void test1() throws InterruptedException {
//
//        JdbiSnapshotReader<UUID, InventoryItemAggregateRoot> snapshotReader = new JdbiSnapshotReader<>(lastSnapshotMap, arReader, newInstanceSupplier);
//
//        EventBus bus = new EventBus();
//        bus.register(new CommandSubscriber(bus, snapshotReader, id -> id.toString()));
//        bus.register(new EventSubscriber(bus));
//
//        // create
//
//        UUID key = UUID.randomUUID() ;
//        CreateInventoryItem command1 = new CreateInventoryItem(UUID.randomUUID(), key);
//        bus.post(command1);
//
//    }
//
//    static void initDb() throws IOException {
//        Handle h = dbi.open();
//        for (String statement : statements()) {
//            h.execute(statement);
//        }
//    }
//
//    static Iterable<String> statements() throws IOException {
//        URL url = Resources.getResource("database/V1__Create_inventory_item_tables.sql");
//        String content = Resources.toString(url, Charsets.UTF_8);
//        return Splitter.on(CharMatcher.is(';'))
//                .trimResults()
//                .omitEmptyStrings()
//                .split(content);
//    }

}

class CommandSubscriber {

    final EventBus bus;
    final SnapshotReader snapshotReader;
    final ItemDescriptionGeneratorService service;

    CommandSubscriber(EventBus bus, SnapshotReader snapshotReader, ItemDescriptionGeneratorService service) {
        this.bus = bus;
        this.snapshotReader = snapshotReader;
        this.service = service;
    }

    @Subscribe
    public void on(CreateInventoryItem command) {
        System.out.println("command " + command);
        Snapshot<InventoryItemAggregateRoot> snapshot = snapshotReader.getSnapshot(command.getId());
        InventoryItemAggregateRoot aggregateRoot = snapshot.getAggregateInstance();
        checkArgument(aggregateRoot.getId() == null, "item already exists");
        checkNotNull(service);
        String description = service.generate(command.getId());
        InventoryItemCreated event = new InventoryItemCreated(command.getId(), description);
        bus.post(event);
        // TODO publish UnitOfWork instead of events
        //throw new RuntimeException("erro");
    }

}

class EventSubscriber {

    final EventBus bus;

    EventSubscriber(EventBus bus) {
        this.bus = bus;
    }

    @Subscribe
    public void on(InventoryItemCreated event) {
        //this.available = this.available + event.howMany;
        System.out.println("event " + event);
    }

}