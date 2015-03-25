package org.myeslib.infra.commandbus.failure;

import com.google.auto.value.AutoValue;
import org.myeslib.data.Command;
import org.myeslib.stack1.infra.helpers.AutoGsonAnnotation;

import java.util.Optional;

@AutoValue
@AutoGsonAnnotation(autoValueClass = AutoValue_ApplyEventsErrorMessage.class)
public abstract class ApplyEventsErrorMessage implements CommandErrorMessage {

    public abstract CommandErrorMessageId getId();
    public abstract Command getCommand();
    public abstract Optional<String> getDescription();

    public static Builder builder() {
        return new AutoValue_ApplyEventsErrorMessage.Builder().id(CommandErrorMessageId.builder().build());
    }

    @AutoValue.Builder
    public interface Builder {
        Builder id(CommandErrorMessageId id);
        Builder command(Command command);
        Builder description(Optional<String> desc);
        ApplyEventsErrorMessage build();
    }
}
