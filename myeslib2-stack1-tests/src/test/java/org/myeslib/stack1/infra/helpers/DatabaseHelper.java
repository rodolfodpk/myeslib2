package org.myeslib.stack1.infra.helpers;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.Resources;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

import static org.myeslib.stack1.infra.helpers.Preconditions.checkNotNull;


public class DatabaseHelper {

    private final DBI dbi;
    private final String ddlScriptFile;

    static final Logger logger = LoggerFactory.getLogger(DatabaseHelper.class);

    public DatabaseHelper(DBI dbi, String ddlScriptFile) {
        checkNotNull(dbi);
        this.dbi = dbi;
        checkNotNull(ddlScriptFile);
        this.ddlScriptFile = ddlScriptFile;
    }

    public void initDb() {
        try {
            Handle h = dbi.open();
            for (String statement : statements()) {
                logger.debug("executing {}", statement);
                h.execute(statement);
            }
            h.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Iterable<String> statements() throws IOException {
        URL url = Resources.getResource(ddlScriptFile);
        String content = Resources.toString(url, Charsets.UTF_8);
        return Splitter.on(CharMatcher.is(';'))
                .trimResults()
                .omitEmptyStrings()
                .split(content);
    }
}

