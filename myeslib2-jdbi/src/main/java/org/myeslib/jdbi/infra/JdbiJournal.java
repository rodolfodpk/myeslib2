package org.myeslib.jdbi.infra;

import com.google.common.eventbus.EventBus;
import org.myeslib.core.Command;
import org.myeslib.core.CommandId;
import org.myeslib.data.UnitOfWork;
import org.myeslib.infra.UnitOfWorkJournal;
import org.myeslib.jdbi.infra.dao.UnitOfWorkDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

public class JdbiJournal<K> implements UnitOfWorkJournal<K> {

    static final Logger logger = LoggerFactory.getLogger(JdbiJournal.class);

    private final UnitOfWorkDao<K> dao;
    private final EventBus[] queryModelBuses;

    public JdbiJournal(UnitOfWorkDao<K> dao, EventBus... queryModelBuses) {
        checkNotNull(dao);
        this.dao = dao;
        checkNotNull(queryModelBuses);
        this.queryModelBuses = queryModelBuses;
    }

    public JdbiJournal(UnitOfWorkDao<K> dao) {
        checkNotNull(dao);
        this.dao = dao;
        this.queryModelBuses = new EventBus[]{};
    }

    @Override
    public void append(K targetId, CommandId commandId, Command command, UnitOfWork unitOfWork) {
        try {
            dao.append(targetId, commandId, command, unitOfWork);
            for (EventBus bus : queryModelBuses) {
                bus.post(unitOfWork);
            }
        } catch (Exception e) {
            throw e ;
        }
    }

}
