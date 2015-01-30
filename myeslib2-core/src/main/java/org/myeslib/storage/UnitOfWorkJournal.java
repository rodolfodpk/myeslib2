package org.myeslib.storage;

import org.myeslib.data.CommandResults;

public interface UnitOfWorkJournal<K> {

    void append(CommandResults<K> commandResults);

}