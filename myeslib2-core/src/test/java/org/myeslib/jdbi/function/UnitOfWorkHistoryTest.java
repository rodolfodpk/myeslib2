package org.myeslib.jdbi.function;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.myeslib.core.Command;
import org.myeslib.core.Event;
import org.myeslib.data.UnitOfWork;
import org.myeslib.data.UnitOfWorkHistory;
import org.myeslib.jdbi.function.test.CommandJustForTest;
import org.myeslib.jdbi.function.test.EventJustForTest;

import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UnitOfWorkHistoryTest {

    @Test
    public void empty() {
        UnitOfWorkHistory transactions = new UnitOfWorkHistory();
        assertThat(transactions.getLastVersion(), is(new Long(0)));
        assertThat(transactions.getAllEvents().size(), is(0));
    }

    @Test(expected = NullPointerException.class)
    public void nullCommand() {
        UnitOfWorkHistory transactions = new UnitOfWorkHistory();
        transactions.add(UnitOfWork.create(UUID.randomUUID(), null, 0L, Arrays.asList(Mockito.mock(Event.class))));
    }

    @Test(expected = NullPointerException.class)
    public void nullEventsList() {
        UnitOfWorkHistory transactions = new UnitOfWorkHistory();
        transactions.add(UnitOfWork.create(UUID.randomUUID(), Mockito.mock(Command.class), 0L, null));
    }

    @Test
    public void firstTransaction() {
        UUID id = UUID.randomUUID();
        UnitOfWorkHistory transactions = new UnitOfWorkHistory();
        Command command = new CommandJustForTest(UUID.randomUUID(), id);
        Event event1 = new EventJustForTest(id, 1);
        transactions.add(UnitOfWork.create(UUID.randomUUID(), command, 0L, Arrays.asList(event1)));

        assertThat(transactions.getUnitsOfWork().size(), is(1));
        assertThat(transactions.getUnitsOfWork().get(0).getCommand(), sameInstance(command));

        assertThat(transactions.getAllEvents().size(), is(1));
        assertThat(transactions.getAllEvents().get(0), sameInstance(event1));
    }

    @Test
    public void twoEvents() {

        UnitOfWorkHistory transactions = new UnitOfWorkHistory();

        UUID id = UUID.randomUUID();
        Command command = new CommandJustForTest(UUID.randomUUID(), id);
        Event event1 = new EventJustForTest(id, 1);
        Event event2 = new EventJustForTest(id, 1);

        transactions.add(UnitOfWork.create(UUID.randomUUID(), command, 0L, Arrays.asList(event1, event2)));

        assertThat(transactions.getLastVersion(), is(1L));
        assertThat(transactions.getUnitsOfWork().size(), is(1));
        assertThat(transactions.getUnitsOfWork().get(0).getCommand(), sameInstance(command));

        assertThat(transactions.getAllEvents().size(), is(2));

        assertThat("all events are within history",
                transactions.getAllEvents().containsAll(Arrays.asList(event1, event2)));

    }

    @Test(expected = NullPointerException.class)
    public void nullEvent() {

        UUID id = UUID.randomUUID();
        UnitOfWorkHistory transactions = new UnitOfWorkHistory();
        Command command = new CommandJustForTest(UUID.randomUUID(), id);
        Event event1 = (Event) null;
        transactions.add(UnitOfWork.create(UUID.randomUUID(), command, 1L, Arrays.asList(event1)));

    }

}
