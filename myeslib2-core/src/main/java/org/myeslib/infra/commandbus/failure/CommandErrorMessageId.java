package org.myeslib.infra.commandbus.failure;

import com.google.auto.value.AutoValue;
import org.myeslib.stack1.infra.helpers.AutoGsonAnnotation;

import java.util.UUID;

@AutoValue
@AutoGsonAnnotation(autoValueClass = AutoValue_CommandErrorMessageId.class)
public abstract class CommandErrorMessageId {

    public abstract UUID uuid();

    public static Builder builder() {
        return new AutoValue_CommandErrorMessageId.Builder().uuid(UUID.randomUUID());
    }

    @AutoValue.Builder
    public interface Builder {
        Builder uuid(UUID uuid);
        CommandErrorMessageId build();
    }

    public static CommandErrorMessageId create(UUID uuid) {
        return builder().uuid(uuid).build();
    }

    public static CommandErrorMessageId create() {
        return builder().build();
    }

    @Override
    public String toString() {
        return uuid().toString();
    }
}
