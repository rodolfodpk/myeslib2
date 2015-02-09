package org.myeslib.stack1.core;

import com.google.auto.value.AutoValue;
import org.myeslib.core.CommandId;
import org.myeslib.stack1.infra.helpers.gson.autovalue.AutoGson;

import java.util.UUID;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Stack1CommandId.class)
public abstract class Stack1CommandId implements CommandId {

    Stack1CommandId() {}

    public static Stack1CommandId create(UUID uuid) {
        return new AutoValue_Stack1CommandId(uuid);
    }

    public static Stack1CommandId create() {
        return new AutoValue_Stack1CommandId(UUID.randomUUID());
    }

    public String toString() {
        return uuid().toString();
    }

}
