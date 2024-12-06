package fr.traqueur.cachelink;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import fr.traqueur.cachelink.impl.LocalCacheMap;
import fr.traqueur.cachelink.impl.MultiServerCacheMap;
import fr.traqueur.cachelink.updating.UpdateMessage;
import fr.traqueur.cachelink.updating.UpdateMessageTypeAdapter;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

import java.lang.reflect.Type;

public class CacheFactory<K,V> {

    private final StatefulRedisConnection<String, String> redisConnection;
    private final StatefulRedisPubSubConnection<String, String> redisPubSubConnection;

    private final Class<K> keyClass;
    private final Class<V> valueClass;

    private final GsonBuilder gson;

    public CacheFactory(Class<K> keyClass, Class<V> valueClass) {
        this(null, keyClass, valueClass);
    }

    public CacheFactory(CacheConfiguration configuration, Class<K> keyClass, Class<V> valueClass) {
        this.keyClass = keyClass;
        this.valueClass = valueClass;
        this.gson = new GsonBuilder();
        this.gson.registerTypeAdapter(UpdateMessage.class, new UpdateMessageTypeAdapter());
        if (configuration != null) {
            RedisURI.Builder redisUri = RedisURI.Builder
                    .redis(configuration.host(), configuration.port());
            if (configuration.password() != null) {
                redisUri.withPassword(configuration.password().toCharArray());
            }
            RedisClient redisClient = RedisClient.create(redisUri.build());
            this.redisConnection = redisClient.connect();
            this.redisPubSubConnection = redisClient.connectPubSub();
        } else {
            this.redisConnection = null;
            this.redisPubSubConnection = null;
        }
    }

    public CacheFactory<K, V> registerTypeAdapterFactory(TypeAdapterFactory factory) {
        this.gson.registerTypeAdapterFactory(factory);
        return this;
    }

    public CacheFactory<K, V> registerTypeAdapterFactory(Type type, Object object) {
        this.gson.registerTypeAdapter(type, object);
        return this;
    }

    public CacheFactory<K, V> registerTypeHierarchyAdapter(Class<?> type, Object object) {
        this.gson.registerTypeHierarchyAdapter(type, object);
        return this;
    }

    public CacheMap<K, V> createCacheMap(String cacheName) {
        if (redisConnection != null) {
            return new MultiServerCacheMap<>(cacheName, redisPubSubConnection, redisConnection, keyClass, valueClass, this.gson.create());
        } else {
            return new LocalCacheMap<>();
        }
    }

}
