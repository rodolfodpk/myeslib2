package org.myeslib.jdbi.core;

import com.google.auto.value.AutoValue;
import org.myeslib.core.CommandId;
import org.myeslib.jdbi.infra.helpers.gson.autovalue.AutoGson;

import java.util.UUID;

@AutoValue
@AutoGson(autoValueClass = AutoValue_JdbiCommandId.class)
public abstract class JdbiCommandId implements CommandId {

    JdbiCommandId() {}

    public static JdbiCommandId create(UUID uuid) {
        return new AutoValue_JdbiCommandId(uuid);
    }

    public static JdbiCommandId create() {
        return new AutoValue_JdbiCommandId(UUID.randomUUID());
    }

    public String toString() {
        return uuid().toString();
    }

}
