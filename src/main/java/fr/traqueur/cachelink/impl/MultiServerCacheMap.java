package fr.traqueur.cachelink.impl;

import com.google.gson.Gson;
import fr.traqueur.cachelink.CacheMap;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class MultiServerCacheMap<K, V> implements CacheMap<K, V> {

    private final RedisCommands<String, String> redisCommands;
    private final Class<K> keyClass;
    private final Class<V> valueClass;
    private final Gson gson;

    public MultiServerCacheMap(StatefulRedisConnection<String, String> connection, Class<K> keyClass, Class<V> valueClass, Gson gson) {
        this.redisCommands = connection.sync();
        this.gson = gson;
        this.keyClass = keyClass;
        this.valueClass = valueClass;
    }

    @Override
    public void put(K key, V value) {
        redisCommands.set(gson.toJson(key), gson.toJson(value));
    }

    @Override
    public V get(K key) {
        String valueJson = redisCommands.get(gson.toJson(key));
        return valueJson == null ? null : gson.fromJson(valueJson, valueClass);
    }

    @Override
    public V getOrDefault(K key, V defaultValue) {
        String valueJson = redisCommands.get(gson.toJson(key));
        return valueJson != null ? gson.fromJson(valueJson, valueClass) : defaultValue;
    }

    @Override
    public void remove(K key) {
        redisCommands.del(gson.toJson(key));
    }

    @Override
    public boolean containsKey(K key) {
        return redisCommands.exists(gson.toJson(key)) > 0;
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
                .map(key -> gson.fromJson(key, keyClass))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<V> values() {
        return keys().stream()
                .map(k -> gson.fromJson(redisCommands.get(gson.toJson(k)), valueClass))
                .collect(Collectors.toList());
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        keys().forEach(key -> action.accept(key, gson.fromJson(redisCommands.get(gson.toJson(key)), valueClass)));
    }

    @Override
    public void putAll(CacheMap<K, V> map) {
        map.forEach(this::put);
    }
}
