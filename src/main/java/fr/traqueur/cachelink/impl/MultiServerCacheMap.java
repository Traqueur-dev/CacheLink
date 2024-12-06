package fr.traqueur.cachelink.impl;

import com.google.gson.Gson;
import fr.traqueur.cachelink.CacheMap;
import fr.traqueur.cachelink.Constants;
import fr.traqueur.cachelink.updating.Operation;
import fr.traqueur.cachelink.updating.UpdateMessage;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class MultiServerCacheMap<K, V> implements CacheMap<K, V> {



    private final String cacheId;
    private final String cacheName;
    private final Map<K, V> localCache;
    private final RedisCommands<String, String> redisCommands;
    private final Class<K> keyClass;
    private final Class<V> valueClass;
    private final Gson gson;

    public MultiServerCacheMap(String cacheName,
                               StatefulRedisPubSubConnection<String, String> pubSubConnection,
                               StatefulRedisConnection<String, String> redisCommands,
                               Class<K> keyClass, Class<V> valueClass, Gson gson) {

        this.cacheId = UUID.randomUUID().toString();
        this.cacheName = cacheName;
        this.localCache = new HashMap<>();
        RedisPubSubCommands<String, String> pubSubCommands = pubSubConnection.sync();
        this.redisCommands = redisCommands.sync();
        this.keyClass = keyClass;
        this.valueClass = valueClass;
        this.gson = gson;

        // Synchronize local cache with existing data
        synchronizeCache();

        // Subscribe to the Redis channel for cache updates
        pubSubConnection.addListener(new RedisPubSubListener<>() {
            @Override
            public void message(String channel, String message) {
                if (Constants.CHANNEL.equals(channel)) {
                    handlePubSubMessage(message);
                }
            }

            @Override
            public void message(String s, String k1, String s2) {}

            @Override
            public void subscribed(String s, long l) {}

            @Override
            public void psubscribed(String s, long l) {}

            @Override
            public void unsubscribed(String s, long l) {}

            @Override
            public void punsubscribed(String s, long l) {}
        });
        pubSubCommands.subscribe(Constants.CHANNEL);

        incrementInstanceCount();
        registerShutdownHook();
    }

    @Override
    public void put(K key, V value) {
        localCache.put(key, value);
        String redisKey = cacheName + ":" + gson.toJson(key);
        redisCommands.set(redisKey, gson.toJson(value)); // Store in Redis
        publishUpdate(Operation.PUT, gson.toJson(key), gson.toJson(value));
    }

    @Override
    public V get(K key) {
        return localCache.get(key);
    }

    @Override
    public V getOrDefault(K key, V defaultValue) {
        return localCache.getOrDefault(key, defaultValue);
    }

    @Override
    public void remove(K key) {
        localCache.remove(key);
        String redisKey = cacheName + ":" + gson.toJson(key);
        redisCommands.del(redisKey);
        publishUpdate(Operation.REMOVE, gson.toJson(key), null);
    }

    @Override
    public boolean containsKey(K key) {
        return localCache.containsKey(key);
    }

    @Override
    public boolean containsValue(V value) {
        return localCache.containsValue(value);
    }

    @Override
    public int size() {
        return localCache.size();
    }

    @Override
    public void clear() {
        localCache.clear();
        redisCommands.keys(cacheName + ":*").forEach(redisCommands::del); // Remove all keys for this cacheName
        publishUpdate(Operation.CLEAR, null, null);
    }

    @Override
    public boolean isEmpty() {
        return localCache.isEmpty();
    }

    @Override
    public Collection<K> keys() {
        return localCache.keySet();
    }

    @Override
    public Collection<V> values() {
        return localCache.values();
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        localCache.forEach(action);
    }

    @Override
    public void putAll(CacheMap<K, V> map) {
        map.forEach(this::put);
    }

    private void publishUpdate(Operation operation, String key, String value) {
        UpdateMessage message = new UpdateMessage(cacheId, cacheName, operation, key, value);
        redisCommands.publish(Constants.CHANNEL, gson.toJson(message));
    }

    private void handlePubSubMessage(String message) {
        UpdateMessage updateMessage = gson.fromJson(message, UpdateMessage.class);

        if (!cacheName.equals(updateMessage.cacheName())) {
            return;
        }

        // Ignore messages sent by this cache instance
        if (cacheId.equals(updateMessage.cacheId())) {
            return;
        }

        switch (updateMessage.operation()) {
            case PUT:
                localCache.put(gson.fromJson(updateMessage.key(), keyClass),
                        gson.fromJson(updateMessage.value(), valueClass));
                break;
            case REMOVE:
                localCache.remove(gson.fromJson(updateMessage.key(), keyClass));
                break;
            case CLEAR:
                localCache.clear();
                break;
        }
    }

    private void synchronizeCache() {
        // Retrieve all keys for this cacheName
        redisCommands.keys(cacheName + ":*").forEach(redisKey -> {
            String valueJson = redisCommands.get(redisKey);
            K deserializedKey = gson.fromJson(redisKey.substring(cacheName.length() + 1), keyClass);
            V deserializedValue = gson.fromJson(valueJson, valueClass);
            localCache.put(deserializedKey, deserializedValue);
        });
    }

    private void incrementInstanceCount() {
        String counterKey = "active-instances:" + cacheName;
        redisCommands.incr(counterKey);
    }

    private void decrementInstanceCount() {
        String counterKey = "active-instances:" + cacheName;
        long count = redisCommands.decr(counterKey);

        if (count <= 0) {
            redisCommands.keys(cacheName + ":*").forEach(redisCommands::del);
            redisCommands.del(counterKey);
        }
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::decrementInstanceCount));
    }
}
