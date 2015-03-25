package org.myeslib.data;

import com.google.auto.value.AutoValue;
import org.myeslib.stack1.infra.helpers.AutoGsonAnnotation;

import java.util.UUID;

@AutoValue
@AutoGsonAnnotation(autoValueClass = AutoValue_CommandId.class)
public abstract class CommandId {

    public abstract UUID uuid();

    public static Builder builder() {
        return new AutoValue_CommandId.Builder().uuid(UUID.randomUUID());
    }

    @AutoValue.Builder
    public interface Builder {
        Builder uuid(UUID uuid);
        CommandId build();
    }

    public static CommandId create() {
        return new AutoValue_CommandId.Builder().uuid(UUID.randomUUID()).build();
    }

    @Override
    public String toString() {
        return uuid().toString();
    }
}
