package org.myeslib.core;

import org.myeslib.data.UnitOfWork;

import java.util.Optional;

/**
 * If the result of command handling _must_ be accessible (thus violating CQRS), the Command Handler can also implement
 * this interface in order to expose the resulting UnitOfWork.
 */
public interface StatefulCommandHandler {

    Optional<UnitOfWork> getUnitOfWork();

}
