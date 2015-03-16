package org.myeslib.data;

import org.junit.Test;
import org.myeslib.data.CommandId;
import org.myeslib.data.UnitOfWork;
import org.myeslib.data.UnitOfWorkId;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UnitOfWorkTest {

    @Test
    public void versionShouldBeSnapshotVersionPlusOne() {
        UnitOfWork uow = UnitOfWork.create(UnitOfWorkId.create(), CommandId.create(), 0L, new ArrayList<>());
        assertThat(uow.getVersion(), is(1L));
    }

    @Test(expected = NullPointerException.class)
    public void nullCmdIdMustFail() {
        UnitOfWork uow = UnitOfWork.create(null, CommandId.create(), 0L, new ArrayList<>());
    }

    @Test(expected = NullPointerException.class)
    public void nullAggregateIdIdMustFail() {
        UnitOfWork uow = UnitOfWork.create(UnitOfWorkId.create(), null, 0L, new ArrayList<>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidSnapshotVersionMustFail() {
        UnitOfWork uow = UnitOfWork.create(UnitOfWorkId.create(), CommandId.create(), -1L, new ArrayList<>());
    }

    @Test(expected = NullPointerException.class)
    public void nullEventsListMustFail() {
        UnitOfWork uow = UnitOfWork.create(UnitOfWorkId.create(), CommandId.create(), 0L, null);
    }

}
