package org.myeslib.stack1.infra.dao;

import com.google.common.collect.LinkedListMultimap;
import org.myeslib.data.Command;
import org.myeslib.data.CommandId;
import org.myeslib.data.UnitOfWork;
import org.myeslib.infra.WriteModelDao;
import org.myeslib.stack1.infra.exceptions.ConcurrencyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Stack1MemDao<K> implements WriteModelDao<K> {

    static final Logger logger = LoggerFactory.getLogger(Stack1MemDao.class);

    private final LinkedListMultimap<K, UnitOfWork> uowMultiMap;
    private final Map<CommandId, Command> commandsMap;

    @Inject
    public Stack1MemDao() {
        this.uowMultiMap = LinkedListMultimap.create();
        this.commandsMap = new HashMap<>();
    }

    @Override
    public List<UnitOfWork> getFull(K id) {
        return uowMultiMap.get(id);
    }

    @Override
    public List<UnitOfWork> getPartial(K id, Long biggerThanThisVersion) {
        return uowMultiMap.get(id).stream().filter((UnitOfWork unitOfWork) -> unitOfWork.getVersion() > biggerThanThisVersion).collect(Collectors.toList());
    }

    @Override
    public void append(K targetId, CommandId commandId, Command command, UnitOfWork unitOfWork) {
        if (uowMultiMap.containsKey(targetId)) {
            final UnitOfWork last = uowMultiMap.get(targetId).stream().reduce((previous, current) -> current).get();
            if (unitOfWork.getVersion() != last.getVersion()+1) {
                throw new ConcurrencyException("new version " + unitOfWork.getVersion() + " should be current version " + last.getVersion() + " +1 !");
            }
        }
        uowMultiMap.put(targetId, unitOfWork);
        commandsMap.put(commandId, command);
    }

    @Override
    public Command getCommand(CommandId commandId) {
        return commandsMap.get(commandId);
    }
}
