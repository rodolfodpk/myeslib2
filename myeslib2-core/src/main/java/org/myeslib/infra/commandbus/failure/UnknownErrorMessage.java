package org.myeslib.infra.commandbus.failure;

import com.google.auto.value.AutoValue;
import org.myeslib.data.Command;
import org.myeslib.stack1.infra.helpers.AutoGsonAnnotation;

import java.util.Optional;

@AutoValue
@AutoGsonAnnotation(autoValueClass = AutoValue_UnknownErrorMessage.class)
public abstract class UnknownErrorMessage implements CommandErrorMessage {

    public abstract CommandErrorMessageId id();
    public abstract Command command();
    public abstract Optional<String> description();

    public static Builder builder() {
        return new AutoValue_UnknownErrorMessage.Builder().id(CommandErrorMessageId.builder().build());
    }

    @AutoValue.Builder
    public interface Builder {
        Builder id(CommandErrorMessageId id);
        Builder command(Command command);
        Builder description(Optional<String> desc);
        UnknownErrorMessage build();
    }

    // interface methods

    @Override
    public Optional<String> getDescription() {
        return description();
    }

    @Override
    public CommandErrorMessageId getId(){
        return id();
    }

    @Override
    public Command getCommand() {
        return command();
    }

}
