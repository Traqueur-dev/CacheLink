package fr.traqueur.cachelink;

import com.google.gson.GsonBuilder;
import fr.traqueur.cachelink.updating.UpdateMessage;
import fr.traqueur.cachelink.updating.UpdateMessageTypeAdapter;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

public class CacheFactory {

    private static CacheFactory instance;

    public static CacheFactory getInstance() {
        return instance;
    }

    private final StatefulRedisConnection<String, String> redisConnection;
    private final StatefulRedisPubSubConnection<String, String> redisPubSubConnection;

    private final GsonBuilder gson;

    public static void init(CacheConfiguration configuration) {
        if (instance != null) {
            throw new IllegalStateException("CacheFactory is already initialized");
        }
        instance = new CacheFactory(configuration);
    }

    private CacheFactory(CacheConfiguration configuration) {
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

    public StatefulRedisConnection<String, String> getRedisConnection() {
        return redisConnection;
    }

    public StatefulRedisPubSubConnection<String, String> getRedisPubSubConnection() {
        return redisPubSubConnection;
    }

    public GsonBuilder getGson() {
        return gson;
    }
}
