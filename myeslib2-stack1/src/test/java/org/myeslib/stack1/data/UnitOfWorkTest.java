package org.myeslib.stack1.data;

import org.junit.Test;
import org.myeslib.data.UnitOfWork;
import org.myeslib.stack1.core.Stack1CommandId;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UnitOfWorkTest {

    @Test
    public void versionShouldBeSnapshotVersionPlusOne() {
        UnitOfWork uow = Stack1UnitOfWork.create(Stack1UnitOfWorkId.create(), Stack1CommandId.create(), 0L, new ArrayList<>());
        assertThat(uow.getVersion(), is(1L));
    }

    @Test(expected = NullPointerException.class)
    public void nullCmdIdMustFail() {
        UnitOfWork uow = Stack1UnitOfWork.create(null, Stack1CommandId.create(), 0L, new ArrayList<>());
    }

    @Test(expected = NullPointerException.class)
    public void nullAggregateIdIdMustFail() {
        UnitOfWork uow = Stack1UnitOfWork.create(Stack1UnitOfWorkId.create(), null, 0L, new ArrayList<>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidSnapshotVersionMustFail() {
        UnitOfWork uow = Stack1UnitOfWork.create(Stack1UnitOfWorkId.create(), Stack1CommandId.create(), -1L, new ArrayList<>());
    }

    @Test(expected = NullPointerException.class)
    public void nullEventsListMustFail() {
        UnitOfWork uow = Stack1UnitOfWork.create(Stack1UnitOfWorkId.create(), Stack1CommandId.create(), 0L, null);
    }

}
