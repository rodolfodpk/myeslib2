package org.myeslib.jdbi.storage.dao.config;

public class DbMetadata {

    public final String aggregateRootName;
    public final String aggregateRootTable;
    public final String unitOfWorkTable;

    public DbMetadata(String aggregateRootName) {
        this.aggregateRootName = aggregateRootName;
        this.aggregateRootTable = aggregateRootName.concat("_AR");
        this.unitOfWorkTable = aggregateRootName.concat("_UOW");
    }

}
