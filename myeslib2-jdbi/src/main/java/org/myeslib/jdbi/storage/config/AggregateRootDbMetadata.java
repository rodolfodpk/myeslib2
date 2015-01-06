package org.myeslib.jdbi.storage.config;

public class AggregateRootDbMetadata {

    public final String aggregateRootName;
    public final String aggregateRootTable;
    public final String unitOfWorkTable;

    public AggregateRootDbMetadata(String aggregateRootName) {
        this.aggregateRootName = aggregateRootName;
        this.aggregateRootTable = aggregateRootName.concat("_AR");
        this.unitOfWorkTable = aggregateRootName.concat("_UOW");
    }

}
