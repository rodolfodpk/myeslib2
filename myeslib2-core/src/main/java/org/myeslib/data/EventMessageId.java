package org.myeslib.data;

import com.google.auto.value.AutoValue;
import org.myeslib.stack1.infra.helpers.AutoGsonAnnotation;

import java.util.UUID;

@AutoValue
@AutoGsonAnnotation(autoValueClass = AutoValue_EventMessageId.class)
public abstract class EventMessageId {

    public abstract UUID uuid();

    public static Builder builder() {
        return new AutoValue_EventMessageId.Builder().uuid(UUID.randomUUID());
    }

    @AutoValue.Builder
    interface Builder {
        Builder uuid(UUID uuid);
        EventMessageId build();
    }

    public static EventMessageId create() {
        return new AutoValue_EventMessageId.Builder().uuid(UUID.randomUUID()).build();
    }

    @Override
    public String toString() {
        return uuid().toString();
    }

}
