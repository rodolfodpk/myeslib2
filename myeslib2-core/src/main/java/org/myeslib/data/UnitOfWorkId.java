package org.myeslib.data;

import com.google.auto.value.AutoValue;
import org.myeslib.stack1.infra.helpers.AutoGsonAnnotation;

import java.util.UUID;

@AutoValue
@AutoGsonAnnotation(autoValueClass = AutoValue_UnitOfWorkId.class)
public abstract class UnitOfWorkId {

    public abstract UUID uuid();

    public static Builder builder() {
        return new AutoValue_UnitOfWorkId.Builder().uuid(UUID.randomUUID());
    }

    @AutoValue.Builder
    interface Builder {
        Builder uuid(UUID uuid);
        UnitOfWorkId build();
    }

    public static UnitOfWorkId create() {
        return new AutoValue_UnitOfWorkId.Builder().uuid(UUID.randomUUID()).build();
    }

    @Override
    public String toString() {
        return uuid().toString();
    }

}
