package org.myeslib.data;

import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UnitOfWorkTest {

    @Test
    public void versionShouldBeSnapshotVersionPlusOne() {
        UnitOfWork uow = UnitOfWork.create(UnitOfWorkId.builder().build(), CommandId.builder().build(), 0L, new ArrayList<>());
        assertThat(uow.getVersion(), is(1L));
    }

    @Test(expected = IllegalStateException.class)
    public void nullCmdIdMustFail() {
        UnitOfWork uow = UnitOfWork.create(null, CommandId.builder().build(), 0L, new ArrayList<>());
    }

    @Test(expected = IllegalStateException.class)
    public void nullAggregateIdIdMustFail() {
        UnitOfWork uow = UnitOfWork.create(UnitOfWorkId.builder().build(), null, 0L, new ArrayList<>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidSnapshotVersionMustFail() {
        UnitOfWork uow = UnitOfWork.create(UnitOfWorkId.builder().build(), CommandId.builder().build(), -1L, new ArrayList<>());
    }

    @Test(expected = NullPointerException.class)
    public void nullEventsListMustFail() {
        UnitOfWork uow = UnitOfWork.create(UnitOfWorkId.builder().build(), CommandId.builder().build(), 0L, null);
    }

}
