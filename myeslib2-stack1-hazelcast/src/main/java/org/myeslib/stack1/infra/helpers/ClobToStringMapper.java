package org.myeslib.stack1.infra.helpers;

import org.skife.jdbi.v2.util.TypedMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
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
        try {
            final String result = clobToString(clob);
            return result;
        } finally {
            clob.free();
        }
    }

    @Override
    protected String extractByIndex(ResultSet r, int index) throws SQLException {
        final Clob clob = r.getClob(index);
        try {
            final String result = clobToString(clob);
            return result;
        } finally {
            clob.free();
        }
    }

    private String clobToString(java.sql.Clob data)
    {
        final StringBuilder sb = new StringBuilder();
        try
        {
            final Reader         reader = data.getCharacterStream();
            final BufferedReader br     = new BufferedReader(reader);
            int b;
            while(-1 != (b = br.read())){
                sb.append((char)b);
            }
            br.close();
        }
        catch (SQLException e){
            log.error("SQL. Could not convert CLOB to string",e);
            throw new RuntimeException(e.getCause());
        }
        catch (IOException e) {
            log.error("IO. Could not convert CLOB to string",e);
            throw new RuntimeException(e.getCause());
        }

        return sb.toString();
    }

}
