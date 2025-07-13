package com.abilash.bofa.POCForRedis.CacheSchemes;

import com.abilash.bofa.POCForRedis.CacheLoader.CacheLoaderImp;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheSchemesImp {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private CacheLoaderImp cacheLoader;
    //local cachescheme
//    @Cacheable(cacheNames = "localCache", key = "#key")
    public String getFromLocalCache(String key) {
        //call loader class from db if not present
        return cacheLoader.get(key,"local");
    }

    //near cache
//    @Cacheable(cacheNames = "nearCache", key = "#key")
    public String getFromNearCache(String key) {
        return cacheLoader.get(key,"near");
    }
//    @CachePut(cacheNames = "nearCache", key = "#key")
    public String putNearCache(String key,String value) {
        cacheLoader.put(key,value,"near");
        return value;
    }

    //distributed cache
    public String putDistributed(String key, String value) {
        cacheLoader.put(key,value,"distributed");
        return "Distributed key written";
    }
    public String getDistributed(String key) {
        return cacheLoader.get(key,"distributed");

    }

}
