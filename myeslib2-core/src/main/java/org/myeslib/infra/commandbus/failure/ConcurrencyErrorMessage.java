package org.myeslib.infra.commandbus.failure;

import com.google.auto.value.AutoValue;
import org.myeslib.data.Command;
import org.myeslib.stack1.infra.helpers.AutoGsonAnnotation;

import java.util.Optional;

@AutoValue
@AutoGsonAnnotation(autoValueClass = AutoValue_ConcurrencyErrorMessage.class)
public abstract class ConcurrencyErrorMessage implements CommandErrorMessage {

    public abstract CommandErrorMessageId id();
    public abstract Command command();
    public abstract Long newVersion();
    public abstract Long currentVersion();

    public static Builder builder() {
        return new AutoValue_ConcurrencyErrorMessage.Builder().id(CommandErrorMessageId.builder().build());
    }

    @AutoValue.Builder
    public interface Builder {
        Builder id(CommandErrorMessageId id);
        Builder command(Command command);
        Builder newVersion(Long newVersion);
        Builder currentVersion(Long currentVersion);
        ConcurrencyErrorMessage build();
    }

    // interface methods

    @Override
    public Optional<String> getDescription() {
        return Optional.of("new version " + newVersion() + ", currentVersion " + currentVersion());
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
