package org.myeslib.jdbi.function;

import org.junit.Test;
import org.myeslib.core.Event;
import org.myeslib.data.UnitOfWork;
import org.myeslib.jdbi.function.test.CommandJustForTest;
import org.myeslib.jdbi.function.test.EventJustForTest;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UnitOfWorkTest {

    @Test
    public void versionShouldBeCommandVersionPlusOne() {
        Long snapshotVersion = 0L;
        List<Event> events = Arrays.asList(new EventJustForTest(UUID.randomUUID(), 1));
        CommandJustForTest command = new CommandJustForTest(UUID.randomUUID(), UUID.randomUUID());
        UnitOfWork uow = UnitOfWork.create(UUID.randomUUID(), command.getCommandId(), snapshotVersion, events);
        assertThat(uow.getVersion(), is(1L));
    }

    @SuppressWarnings("unused")
    @Test(expected = NullPointerException.class)
    public void nullEvent() {
        List<Event> events = Arrays.asList(null);
        CommandJustForTest command = new CommandJustForTest(UUID.randomUUID(), UUID.randomUUID());
        UnitOfWork uow = new UnitOfWork(UUID.randomUUID(), command.getCommandId(), null, events);
    }
}
