package fr.traqueur.cachelink.impl;

import fr.traqueur.cachelink.Cache;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class LocalCache<V> implements Cache<V> {

    private final List<V> values = new CopyOnWriteArrayList<>();

    @Override
    public V get(int i) {
        return this.values.get(i);
    }

    @Override
    public void add(V value) {
        this.values.add(value);
    }

    @Override
    public void remove(V value) {
        this.values.remove(value);
    }

    @Override
    public void clear() {
        this.values.clear();
    }

    @Override
    public boolean contains(V value) {
        return this.values.contains(value);
    }

    @Override
    public boolean removeIf(Predicate<? super V> consumer) {
        return this.values.removeIf(consumer);
    }

    @Override
    public boolean isEmpty() {
        return this.values.isEmpty();
    }

    @Override
    public void addAll(Collection<V> values) {
        this.values.addAll(values);
    }

    @Override
    public void forEach(Consumer<V> consumer) {
        this.values.forEach(consumer);
    }
}
