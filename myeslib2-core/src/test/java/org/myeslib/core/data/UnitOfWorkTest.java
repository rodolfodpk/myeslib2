package org.myeslib.core.data;

import org.junit.Test;
import org.myeslib.core.Event;
import org.myeslib.core.data.test.CommandJustForTest;
import org.myeslib.core.data.test.EventJustForTest;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UnitOfWorkTest {

    @Test
    public void versionShouldBeCommandVersionPlusOne() {
        List<Event> events = Arrays.asList(new EventJustForTest(UUID.randomUUID(), 1));
        CommandJustForTest command = new CommandJustForTest(UUID.randomUUID(), UUID.randomUUID(), 0L);
        UnitOfWork uow = UnitOfWork.create(UUID.randomUUID(), command, events);
        assertThat(uow.getTargetVersion(), is(0L));
        assertThat(uow.getVersion(), is(1L));
    }

    @SuppressWarnings("unused")
    @Test(expected = NullPointerException.class)
    public void nullEvent() {
        List<Event> events = Arrays.asList(null);
        CommandJustForTest command = new CommandJustForTest(UUID.randomUUID(), UUID.randomUUID(), 1L);
        UnitOfWork uow = new UnitOfWork(UUID.randomUUID(), command, 1L, events);
    }
}
