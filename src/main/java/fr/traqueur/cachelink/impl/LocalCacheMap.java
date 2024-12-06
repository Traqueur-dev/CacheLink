package fr.traqueur.cachelink.impl;

import fr.traqueur.cachelink.CacheMap;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class LocalCacheMap<K,V> implements CacheMap<K,V> {

    private final ConcurrentHashMap<K,V> map = new ConcurrentHashMap<>();

    @Override
    public void put(K key, V value) {
        this.map.put(key, value);
    }

    @Override
    public V get(K key) {
        return this.map.get(key);
    }

    @Override
    public V getOrDefault(K key, V defaultValue) {
        return this.map.getOrDefault(key, defaultValue);
    }

    @Override
    public void remove(K key) {
        this.map.remove(key);
    }

    @Override
    public boolean containsKey(K key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(V value) {
        return this.map.containsValue(value);
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public Collection<K> keys() {
        return this.map.keySet();
    }

    @Override
    public Collection<V> values() {
        return this.map.values();
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        this.map.forEach(action);
    }

    @Override
    public void putAll(CacheMap<K, V> map) {
        map.forEach(this::put);
    }
}
