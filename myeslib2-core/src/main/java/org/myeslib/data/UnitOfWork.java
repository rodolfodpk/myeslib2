package org.myeslib.data;

import org.myeslib.core.Event;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public interface UnitOfWork extends Serializable, Comparable<UnitOfWork> {

    UUID getId();

    UUID getCommandId();

    Long getVersion();

    List<Event> getEvents();

}
