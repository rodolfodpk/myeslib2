package org.myeslib.jdbi.data;

import org.junit.Test;
import org.myeslib.data.UnitOfWork;

import java.util.ArrayList;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UnitOfWorkTest {

    @Test
    public void versionShouldBeSnapshotVersionPlusOne() {
        UnitOfWork uow = JdbiUnitOfWork.create(UUID.randomUUID(), UUID.randomUUID(), 0L, new ArrayList<>());
        assertThat(uow.getVersion(), is(1L));
    }

    @Test(expected = NullPointerException.class)
    public void nullCmdIdMustFail() {
        UnitOfWork uow = JdbiUnitOfWork.create(null, UUID.randomUUID(), 0L, new ArrayList<>());
    }

    @Test(expected = NullPointerException.class)
    public void nullAggregateIdIdMustFail() {
        UnitOfWork uow = JdbiUnitOfWork.create(UUID.randomUUID(), null, 0L, new ArrayList<>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidSnapshotVersionMustFail() {
        UnitOfWork uow = JdbiUnitOfWork.create(UUID.randomUUID(), UUID.randomUUID(), -1L, new ArrayList<>());
    }

    @Test(expected = NullPointerException.class)
    public void nullEventsListMustFail() {
        UnitOfWork uow = JdbiUnitOfWork.create(UUID.randomUUID(), UUID.randomUUID(), 0L, null);
    }

}
