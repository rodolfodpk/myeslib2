package org.myeslib.data;


import java.io.Serializable;
import java.util.UUID;

public class UnitOfWorkId  implements Serializable {

    private final UUID uuid;

    public UnitOfWorkId(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID uuid() {
        return uuid;
    }

    public static UnitOfWorkId create(UUID uuid) {
        return new UnitOfWorkId(uuid);
    }

    public static UnitOfWorkId create() {
        return new UnitOfWorkId(UUID.randomUUID());
    }

    public String toString() {
        return uuid.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnitOfWorkId that = (UnitOfWorkId) o;

        if (!uuid.equals(that.uuid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
