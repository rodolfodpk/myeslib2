package org.myeslib.jdbi.infra.helpers;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.Resources;
import lombok.extern.slf4j.Slf4j;
import org.h2.jdbcx.JdbcConnectionPool;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.io.IOException;
import java.net.URL;

@Slf4j
public abstract class DbAwareBaseTestClass {

    protected static final JdbcConnectionPool pool = JdbcConnectionPool.create("jdbc:h2:mem:test;MODE=Oracle", "scott", "tiger");
    ;
    protected static final DBI dbi = new DBI(pool);

    protected static void initDb() throws Exception {
        Handle h = dbi.open();
        for (String statement : statements()) {
            log.debug("executing {}", statement);
            h.execute(statement);
        }
        h.close();
    }

    private static Iterable<String> statements() throws IOException {
        URL url = Resources.getResource("database/V1__Create_inventory_item_tables.sql");
        String content = Resources.toString(url, Charsets.UTF_8);
        return Splitter.on(CharMatcher.is(';'))
                .trimResults()
                .omitEmptyStrings()
                .split(content);
    }

}
