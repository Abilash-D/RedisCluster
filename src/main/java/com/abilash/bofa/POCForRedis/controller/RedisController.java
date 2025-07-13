package com.abilash.bofa.POCForRedis.controller;

import java.time.Duration;

import com.abilash.bofa.POCForRedis.CacheSchemes.CacheSchemesImp;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/redis")
@RequiredArgsConstructor
public class RedisController {

    private final CacheSchemesImp cacheSchemesImp;
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
    @GetMapping("/local/{key}")
    public String getFromLocalCache(@PathVariable String key) {
        return cacheSchemesImp.getFromLocalCache(key);
    }

    // Simulates near cache (Caffeine first, Redis fallback)
    @GetMapping("/near/{key}")
    public String getNearCache(@PathVariable String key) {
        return cacheSchemesImp.getFromNearCache(key);
    }

    @PostMapping("/near")
    public String putNearCache(@RequestParam String key, @RequestParam String value) {
        cacheSchemesImp.putNearCache(key,value);
        return value;
    }

    // Simulates replicated cache (write to all manually)
    @PostMapping("/replicated")
    public String putReplicated(@RequestParam String key, @RequestParam String value) {
        //this implementations cant be done
        return "Replicated cant be done dynamically in redis!!!";
    }

    // Distributed cache already works via default RedisTemplate
    @PostMapping("/distributed")
    public String putDistributed(@RequestParam String key, @RequestParam String value) {
        return cacheSchemesImp.putDistributed(key,value);
    }

    @GetMapping("/distributed/{key}")
    public String getDistributed(@PathVariable String key) {
        return cacheSchemesImp.getDistributed(key);
    }


}
