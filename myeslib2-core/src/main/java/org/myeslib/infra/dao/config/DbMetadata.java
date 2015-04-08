package org.myeslib.infra.dao.config;

import org.myeslib.core.EventSourced;

public class DbMetadata<E extends EventSourced> {

    public final String aggregateRootName;
    public final String aggregateRootTable;
    public final String unitOfWorkTable;
    public final String commandTable;

    public DbMetadata(String aggregateRootName) {
        this.aggregateRootName = aggregateRootName;
        this.aggregateRootTable = aggregateRootName.concat("_AR");
        this.unitOfWorkTable = aggregateRootName.concat("_UOW");
        this.commandTable = aggregateRootName.concat("_CMD");
    }

}
