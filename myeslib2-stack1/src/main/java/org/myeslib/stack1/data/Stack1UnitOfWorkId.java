package org.myeslib.stack1.data;

import com.google.auto.value.AutoValue;
import org.myeslib.data.UnitOfWorkId;
import org.myeslib.stack1.infra.helpers.gson.autovalue.AutoGson;

import java.util.UUID;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Stack1UnitOfWorkId.class)
public abstract class Stack1UnitOfWorkId implements UnitOfWorkId {

    Stack1UnitOfWorkId() {}

    public static Stack1UnitOfWorkId create(UUID uuid) {
        return new AutoValue_Stack1UnitOfWorkId(uuid);
    }

    public static Stack1UnitOfWorkId create() {
        return new AutoValue_Stack1UnitOfWorkId(UUID.randomUUID());
    }

    public String toString() {
        return uuid().toString();
    }

}
