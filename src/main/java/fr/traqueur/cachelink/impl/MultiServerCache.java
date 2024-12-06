package fr.traqueur.cachelink.impl;

import fr.traqueur.cachelink.Cache;
import fr.traqueur.cachelink.JsonSerializer;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MultiServerCache<V> implements Cache<V> {

    private final RedisCommands<String, String> redisCommands;
    private final Class<V> valueClass;

    public MultiServerCache(StatefulRedisConnection<String, String> connection, Class<V> valueClass) {
        this.redisCommands = connection.sync();
        this.valueClass = valueClass;
    }

    private Set<String> getKeys() {
        return new HashSet<>(redisCommands.keys("*"));
    }

    @Override
    public V get(int i) {
        List<String> keys = new ArrayList<>(getKeys());
        if (i < keys.size()) {
            return JsonSerializer.deserialize(redisCommands.get(keys.get(i)), valueClass);
        }
        return null;
    }

    @Override
    public void add(V value) {
        UUID uuid = UUID.randomUUID();
        redisCommands.set(uuid.toString(), value.toString());
    }

    @Override
    public void remove(V value) {
        Set<String> keys = getKeys();
        for (String key : keys) {
            if (redisCommands.get(key).equals(value.toString())) {
                redisCommands.del(key);
            }
        }
    }

    @Override
    public void clear() {
        redisCommands.flushdb();
    }

    @Override
    public boolean contains(V value) {
        Set<String> keys = getKeys();
        for (String key : keys) {
            if (redisCommands.get(key).equals(value.toString())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean removeIf(Predicate<? super V> consumer) {
        Set<String> keys = getKeys();
        for (String key : keys) {
            if (consumer.test(JsonSerializer.deserialize(redisCommands.get(key), valueClass))) {
                redisCommands.del(key);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return redisCommands.dbsize() == 0;
    }

    @Override
    public void addAll(Collection<V> values) {
        for (V value : values) {
            add(value);
        }
    }

    @Override
    public void forEach(Consumer<V> consumer) {
        Set<String> keys = getKeys();
        for (String key : keys) {
            consumer.accept(JsonSerializer.deserialize(redisCommands.get(key), valueClass));
        }
    }
}