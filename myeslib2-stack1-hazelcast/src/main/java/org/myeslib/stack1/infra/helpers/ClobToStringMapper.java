package org.myeslib.stack1.infra.helpers;

import com.google.common.io.CharStreams;
import org.skife.jdbi.v2.util.TypedMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClobToStringMapper extends TypedMapper<String> {

    private static final Logger log = LoggerFactory.getLogger(ClobToStringMapper.class);

    public static final ClobToStringMapper FIRST = new ClobToStringMapper();

    public ClobToStringMapper() {
        super();
    }

    public ClobToStringMapper(int index) {
        super(index);
    }

    public ClobToStringMapper(String name) {
        super(name);
    }

    @Override
    protected String extractByName(ResultSet r, String name) throws SQLException {
        final Clob clob = r.getClob(name);
        final Reader stream = clob.getCharacterStream();
        try {
            final String result = CharStreams.toString(stream);
            stream.close();
            return result;
        } catch (IOException e) {
            log.error("error on extract Clob by name {} ", e);
        } finally {
            clob.free();
            try {
                stream.close();
            } catch (IOException e) {
                log.error("error on extract Clob by name {} ", e);
            }
        }
        return null;
    }

    @Override
    protected String extractByIndex(ResultSet r, int index) throws SQLException {
        final Clob clob = r.getClob(index);
        final Reader stream = clob.getCharacterStream();
        try {
            final String result = CharStreams.toString(stream);
            stream.close();
            return result;
        } catch (IOException e) {
            log.error("error on extract Clob by name {} ", e);
        } finally {
            clob.free();
            try {
                stream.close();
            } catch (IOException e) {
                log.error("error on extract Clob by name {} ", e);
            }
        }
        return null;
    }
}
