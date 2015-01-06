package org.myeslib.jdbi.helpers.h2;

import com.google.common.io.CharStreams;
import org.skife.jdbi.v2.util.TypedMapper;

import java.io.IOException;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClobToStringMapper extends TypedMapper<String> {

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
        Clob clob = r.getClob(name);
        try {
            return CharStreams.toString(clob.getCharacterStream());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            clob.free();
        }
        return null;
    }

    @Override
    protected String extractByIndex(ResultSet r, int index) throws SQLException {
        Clob clob = r.getClob(index);
        try {
            return CharStreams.toString(clob.getCharacterStream());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            clob.free();
        }
        return null;
    }
}
