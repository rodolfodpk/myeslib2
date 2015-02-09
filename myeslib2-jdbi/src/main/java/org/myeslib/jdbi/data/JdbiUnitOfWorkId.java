package org.myeslib.jdbi.data;

import com.google.auto.value.AutoValue;
import org.myeslib.data.UnitOfWorkId;
import org.myeslib.jdbi.infra.helpers.gson.autovalue.AutoGson;

import java.util.UUID;

@AutoValue
@AutoGson(autoValueClass = AutoValue_JdbiUnitOfWorkId.class)
public abstract class JdbiUnitOfWorkId implements UnitOfWorkId {

    JdbiUnitOfWorkId() {}

    public static JdbiUnitOfWorkId create(UUID uuid) {
        return new AutoValue_JdbiUnitOfWorkId(uuid);
    }

    public static JdbiUnitOfWorkId create() {
        return new AutoValue_JdbiUnitOfWorkId(UUID.randomUUID());
    }

    public String toString() {
        return uuid().toString();
    }

}
