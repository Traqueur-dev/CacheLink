package fr.traqueur.cachelink.impl;

import fr.traqueur.cachelink.CacheMap;
import fr.traqueur.cachelink.JsonSerializer;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class MultiServerCacheMap<K, V> implements CacheMap<K, V> {

    private final RedisCommands<String, String> redisCommands;
    private final Class<K> keyClass;
    private final Class<V> valueClass;

    public MultiServerCacheMap(StatefulRedisConnection<String, String> connection, Class<K> keyClass, Class<V> valueClass) {
        this.redisCommands = connection.sync();
        this.keyClass = keyClass;
        this.valueClass = valueClass;
    }

    @Override
    public void put(K key, V value) {
        redisCommands.set(JsonSerializer.serialize(key), JsonSerializer.serialize(value));
    }

    @Override
    public V get(K key) {
        String valueJson = redisCommands.get(JsonSerializer.serialize(key));
        return valueJson == null ? null : JsonSerializer.deserialize(valueJson, valueClass);
    }

    @Override
    public V getOrDefault(K key, V defaultValue) {
        String valueJson = redisCommands.get(JsonSerializer.serialize(key));
        return valueJson != null ? JsonSerializer.deserialize(valueJson, valueClass) : defaultValue;
    }

    @Override
    public void remove(K key) {
        redisCommands.del(JsonSerializer.serialize(key));
    }

    @Override
    public boolean containsKey(K key) {
        return redisCommands.exists(JsonSerializer.serialize(key)) > 0;
    }

    @Override
    public boolean containsValue(V value) {
        return values().contains(value);
    }

    @Override
    public int size() {
        return redisCommands.dbsize().intValue();
    }

    @Override
    public void clear() {
        redisCommands.flushdb();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Collection<K> keys() {
        return redisCommands.keys("*").stream()
                .map(key -> JsonSerializer.deserialize(key, keyClass))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<V> values() {
        return keys().stream()
                .map(k -> JsonSerializer.deserialize(redisCommands.get(JsonSerializer.serialize(k)), valueClass))
                .collect(Collectors.toList());
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        keys().forEach(key -> action.accept(key, JsonSerializer.deserialize(redisCommands.get(JsonSerializer.serialize(key)), valueClass)));
    }

    @Override
    public void putAll(CacheMap<K, V> map) {
        map.forEach(this::put);
    }
}
