package io.github.truongbn.redistestcontainers.controller;

import java.time.Duration;
import java.util.UUID;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/redis")
@RequiredArgsConstructor
public class RedisController {
    private final RedisTemplate<String, String> redisTemplate;
    @GetMapping(path = "/object/{key}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getObject(@PathVariable("key") String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @PostMapping(path = "/object")
    public void pushObject(@RequestBody RedisRequest redisRequest) {
        redisTemplate.opsForValue().set(redisRequest.getKey(), redisRequest.getValue(),
                Duration.ofMinutes(1));
    }

    @DeleteMapping(path = "/object/{key}")
    public boolean deleteObject(@PathVariable("key") String key) {
        return redisTemplate.delete(key);
    }
    @Data
    public static class RedisRequest {
        private final String key;
        private final String value;
    }


    //redis differnt scheme implementation

    // Simulates local cache (no Redis involved)
    @Cacheable(cacheNames = "localCache", key = "#key")
    @GetMapping("/local/{key}")
    public String getFromLocalCache(@PathVariable String key) {
        return "GeneratedLocalValue-" + UUID.randomUUID();
    }

    // Simulates near cache (Caffeine first, Redis fallback)
    @Cacheable(cacheNames = "nearCache", key = "#key")
    @GetMapping("/near/{key}")
    public String getNearCache(@PathVariable String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @CachePut(cacheNames = "nearCache", key = "#key")
    @PostMapping("/near")
    public String putNearCache(@RequestParam String key, @RequestParam String value) {
        redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(10));
        return value;
    }

    // Simulates replicated cache (write to all manually)
    @PostMapping("/replicated")
    public String putReplicated(@RequestParam String key, @RequestParam String value) {
        redisTemplate.opsForValue().set(key + ":node1", value);
        redisTemplate.opsForValue().set(key + ":node2", value);
        redisTemplate.opsForValue().set(key + ":node3", value);
        return "Replicated";
    }

    // Distributed cache already works via default RedisTemplate
    @PostMapping("/distributed")
    public String putDistributed(@RequestParam String key, @RequestParam String value) {
        redisTemplate.opsForValue().set(key, value);
        return "Distributed key written";
    }

    @GetMapping("/distributed/{key}")
    public String getDistributed(@PathVariable String key) {
        return redisTemplate.opsForValue().get(key);
    }


}
