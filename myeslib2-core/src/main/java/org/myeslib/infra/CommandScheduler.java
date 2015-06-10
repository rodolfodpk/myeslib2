package org.myeslib.infra;

import org.myeslib.data.Command;

import java.time.Instant;

public interface CommandScheduler {

    void schedule(Command command);

    void schedule(Command command, Instant instant);

}
