package org.myeslib.stack1.infra;

import org.myeslib.core.EventSourced;
import org.myeslib.infra.Snapshot;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;

public class HazelcastCacheFactory<K, E extends EventSourced> {

    public Cache<K, Snapshot<E>> cache(String cacheName, Class keyClass, Class valueClass) {

        // Retrieve the CachingProvider which is automatically backed by
        // the chosen Hazelcast server or client provider
        CachingProvider cachingProvider = Caching.getCachingProvider();

        // Create a CacheManager
        CacheManager cacheManager = cachingProvider.getCacheManager();

        // Create a simple but typesafe configuration for the cache
        CompleteConfiguration<K, Snapshot<E>> config =
                new MutableConfiguration<K, Snapshot<E>>()
                        .setTypes( keyClass, valueClass);

        // Create and get the cache
        Cache<K, Snapshot<E>> cache = cacheManager.createCache( cacheName, config );
        // Alternatively to request an already existing cache
        // Cache<String, String> cache = cacheManager
        // .getCache( name, String.class, String.class );

        return cache;

    }

}
