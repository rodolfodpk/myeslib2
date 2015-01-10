package org.myeslib.data;

// experimental
public class Id<T> {

    private final Class<T> classOfId;
    private final T value;

    public Id(Class<T> classOfId, T value) {
        this.classOfId = classOfId;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Id id = (Id) o;

        if (!classOfId.equals(id.classOfId)) return false;
        if (!value.equals(id.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = classOfId.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Id{" +
                "classOfId=" + classOfId +
                ", value=" + value +
                '}';
    }
}