package fr.traqueur.cachelink;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Cache<V> {

    V get(int i);

    void add(V value);

    void remove(V value);

    void clear();

    boolean contains(V value);

    boolean removeIf(Predicate<? super V> consumer);

    boolean isEmpty();

    void addAll(Collection<V> values);

    void forEach(Consumer<V> consumer);


}
