package org.myeslib.data;

import autovalue.shaded.com.google.common.common.collect.ImmutableList;
import com.google.auto.value.AutoValue;
import org.myeslib.stack1.infra.helpers.AutoGsonAnnotation;

import java.io.Serializable;
import java.util.List;

import static autovalue.shaded.com.google.common.common.base.Preconditions.checkArgument;

@AutoValue
@AutoGsonAnnotation(autoValueClass = AutoValue_UnitOfWork.class)
public abstract class UnitOfWork implements Serializable, Comparable<UnitOfWork> {

    public abstract UnitOfWorkId id();
    public abstract CommandId commandId();
    public abstract Long version();
    public abstract List<? extends Event> events();

    @AutoValue.Validate
    void validate() {
        checkArgument(version() > 0, "version must be a positive number");
    }

    public static Builder builder() {
        return new AutoValue_UnitOfWork.Builder().id(UnitOfWorkId.builder().build());
    }

    @AutoValue.Builder
    interface Builder {
        Builder id(UnitOfWorkId id);
        Builder commandId(CommandId commandId);
        Builder version(Long version);
        Builder events(List<? extends Event> events);
        UnitOfWork build();
    }

    // legacy methods

    public static UnitOfWork create(UnitOfWorkId id, CommandId commandId, Long snapshotVersion, List<? extends Event> newEvents) {
        return new AutoValue_UnitOfWork.Builder().id(id).commandId(commandId).version(snapshotVersion+1).events(ImmutableList.copyOf(newEvents)).build();
    }

    public List<Event> getEvents() {
        return ImmutableList.copyOf(events());
    }

    public UnitOfWorkId getId() {
        return id();
    }

    public CommandId getCommandId() {
        return commandId();
    }

    public Long getVersion() {
        return version();
    }

    public int compareTo(UnitOfWork other) {
        if (version() < other.getVersion()) {
            return -1;
        } else if (version() > other.getVersion()) {
            return 1;
        }
        return 0;
    }
}
