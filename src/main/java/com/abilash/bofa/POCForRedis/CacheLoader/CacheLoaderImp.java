package com.abilash.bofa.POCForRedis.CacheLoader;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class CacheLoaderImp {

    @Autowired
    private final RedisTemplate<String, String> redisTemplate;

    // Local in-memory cache (simulating localCache scheme)
    private final Cache<String, String> localCache = Caffeine.newBuilder()
            .expireAfterWrite(3, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    @Value("${cache.loader.default-ttl:60}")
    private long redisTTL; // TTL in seconds

    /**
     * Load value from local -> redis -> then fallback db
     */
    public String get(String key, String cacheType) {
        switch (cacheType.toLowerCase()) {
            case "local":
                return getFromLocalThenRedisThenDB(key, true);
            case "near":
                return getFromLocalThenRedisThenDB(key, false);
            case "distributed":
                return getFromRedisThenDB(key);
            case "directdb":
                return loadFromDB(key);
            default:
                throw new IllegalArgumentException("Unsupported cache type: " + cacheType);
        }
    }

    /**
     * Get from local, then Redis, then DB. Optionally push to local again.
     */
    private String getFromLocalThenRedisThenDB(String key, boolean isLocalOnly) {
        String val = localCache.getIfPresent(key);
        if (val != null) return val;

        val = redisTemplate.opsForValue().get(key);
        if (val != null) {
            localCache.put(key, val); // populate local
            return val;
        }

        // Fallback to DB
        val = loadFromDB(key);
        if (val != null) {
            redisTemplate.opsForValue().set(key, val, Duration.ofSeconds(redisTTL));
            if (isLocalOnly) localCache.put(key, val);
        }
        return val;
    }

    /**
     * Distributed cache (no local)
     */
    private String getFromRedisThenDB(String key) {
        String val = redisTemplate.opsForValue().get(key);
        if (val != null) return val;

        val = loadFromDB(key);
        if (val != null)
            redisTemplate.opsForValue().set(key, val, Duration.ofSeconds(redisTTL));

        return val;
    }

    /**
     * Simulate DB lookup — Replace with actual DB query
     */
    private String loadFromDB(String key) {
        System.out.println("Loading from DB for key: " + key);
        return "Value fetched from DB for key: " + key;
    }

    // Optional for write-through cache support
    public void put(String key, String value, String cacheType) {
        switch (cacheType.toLowerCase()) {
            case "local":
                localCache.put(key, value);
                break;
            case "near":
                localCache.put(key, value);
                redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(redisTTL));
                break;
            case "distributed":
                redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(redisTTL));
                break;
            default:
                throw new IllegalArgumentException("Unsupported cache type: " + cacheType);
        }
    }
}

