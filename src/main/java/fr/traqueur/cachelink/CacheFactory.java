package fr.traqueur.cachelink;

import com.google.gson.Gson;
import fr.traqueur.cachelink.impl.LocalCacheMap;
import fr.traqueur.cachelink.impl.MultiServerCacheMap;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;

public class CacheFactory<K,V> {

    private final StatefulRedisConnection<String, String> redisConnection;
    private final Class<K> keyClass;
    private final Class<V> valueClass;

    private Gson gson;

    public CacheFactory(Class<K> keyClass, Class<V> valueClass) {
        this(null, keyClass, valueClass);
    }

    public CacheFactory(CacheConfiguration configuration, Class<K> keyClass, Class<V> valueClass) {
        this.keyClass = keyClass;
        this.valueClass = valueClass;
        this.gson = new Gson();
        if (configuration != null) {
            RedisURI.Builder redisUri = RedisURI.Builder
                    .redis(configuration.host(), configuration.port());
            if (configuration.password() != null) {
                redisUri.withPassword(configuration.password().toCharArray());
            }
            RedisClient redisClient = RedisClient.create(redisUri.build());
            this.redisConnection = redisClient.connect();
        } else {
            this.redisConnection = null;
        }
    }

    public CacheFactory<K, V> withGson(Gson gson) {
        this.gson = gson;
        return this;
    }

    public CacheMap<K, V> createCacheMap() {
        if (redisConnection != null) {
            return new MultiServerCacheMap<>(redisConnection, keyClass, valueClass, this.gson);
        } else {
            return new LocalCacheMap<>();
        }
    }

}
