package fr.traqueur.cachelink.collections;

import com.google.gson.Gson;
import fr.traqueur.cachelink.CacheFactory;
import fr.traqueur.cachelink.Constants;
import fr.traqueur.cachelink.serialization.Serializer;
import fr.traqueur.cachelink.updating.Operation;
import fr.traqueur.cachelink.updating.UpdateMessage;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CacheMap<K,V> extends ConcurrentHashMap<K,V> {

    private final Gson gson;
    private final String cacheId;
    private final String cacheName;
    private final RedisCommands<String, String> redisCommands;
    private final Serializer<K> keySerializer;
    private final Serializer<V> valueSerializer;
    private final boolean isMultiServer;

    public CacheMap(String cacheName, Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        super();
        this.cacheId = UUID.randomUUID().toString();
        this.cacheName = cacheName;
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;

        CacheFactory cacheFactory = CacheFactory.getInstance();
        if(cacheFactory != null) {
            this.gson = cacheFactory.getGson().create();
            var pubSubConnection = cacheFactory.getRedisPubSubConnection();
            var redisConnection = cacheFactory.getRedisConnection();
            RedisPubSubCommands<String, String> pubSubCommands = pubSubConnection != null ? pubSubConnection.sync() : null;
            this.redisCommands = redisConnection != null ? redisConnection.sync() : null;
            this.isMultiServer = this.redisCommands != null;
            if (this.isMultiServer && pubSubConnection != null && pubSubCommands != null) {
                this.setupMultiServer(pubSubConnection, pubSubCommands);
            }
        } else {
            this.redisCommands = null;
            this.isMultiServer = false;
            this.gson = null;
        }
    }

    private void setupMultiServer(StatefulRedisPubSubConnection<String, String> pubSubConnection, RedisPubSubCommands<String, String> pubSubCommands) {
        // Synchronize local cache with existing data
        synchronizeCache();

        // Subscribe to the Redis channel for cache updates
        pubSubConnection.addListener(new RedisPubSubListener<>() {
            @Override
            public void message(String channel, String message) {
                if (Constants.CHANNEL.equals(channel)) {
                    CacheMap.this.handlePubSubMessage(message);
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
                this.put(this.keySerializer.deserialize(updateMessage.key()), this.valueSerializer.deserialize(updateMessage.value()));
                break;
            case REMOVE:
                this.remove(this.keySerializer.deserialize(updateMessage.key()));
                break;
            case CLEAR:
                this.clear();
                break;
        }
    }

    private void synchronizeCache() {
        // Retrieve all keys for this cacheName
        redisCommands.keys(cacheName + ":*").forEach(redisKey -> {
            String rawValue = redisCommands.get(redisKey);
            String key = redisKey.substring(redisKey.indexOf(":") + 1);
            K deserializedKey = keySerializer.deserialize(key);
            V deserializedValue = valueSerializer.deserialize(rawValue);
            this.put(deserializedKey, deserializedValue);
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

    @Override
    public V put(K key, V value) {
        String serializedKey = keySerializer.serialize(key);
        String serializedValue = valueSerializer.serialize(value);
        if (isMultiServer) {
            this.redisCommands.set(cacheName + ":" + serializedKey, serializedValue);
            publishUpdate(Operation.PUT, serializedKey, serializedValue);
        }
        return super.put(key, value);
    }

    @Override
    public V remove(Object key) {
        String serializedKey = keySerializer.serialize((K) key);
        if (isMultiServer) {
            this.redisCommands.del(cacheName + ":" + serializedKey);
            publishUpdate(Operation.REMOVE, serializedKey, null);
        }
        return super.remove(key);
    }

    @Override
    public void clear() {
        if (isMultiServer) {
            this.redisCommands.keys(cacheName + ":*").forEach(redisCommands::del);
            publishUpdate(Operation.CLEAR, null, null);
        }
        super.clear();
    }
}
