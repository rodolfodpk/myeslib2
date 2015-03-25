package org.myeslib.data;

import com.google.auto.value.AutoValue;
import org.myeslib.stack1.infra.helpers.AutoGsonAnnotation;

@AutoValue
@AutoGsonAnnotation(autoValueClass = AutoValue_EventMessage.class)
public abstract class EventMessage {

    public abstract EventMessageId id();
    public abstract Event event();

    public static Builder builder() {
        return new AutoValue_EventMessage.Builder().id(EventMessageId.builder().build());
    }

    @AutoValue.Builder
    public interface Builder {
        Builder id(EventMessageId id);
        Builder event(Event event);
        EventMessage build();
    }

    public static EventMessage create(Event event) {
        return new AutoValue_EventMessage.Builder().id(EventMessageId.create()).event(event).build();
    }

}
