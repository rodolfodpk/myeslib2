package org.myeslib.stack1.infra.helpers;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.Resources;
import lombok.extern.slf4j.Slf4j;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.io.IOException;
import java.net.URL;

import static com.google.common.base.Preconditions.checkNotNull;


@Slf4j
public class DatabaseHelper {

    private final DBI dbi;
    private final String ddlScriptFile;

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
                log.debug("executing {}", statement);
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

