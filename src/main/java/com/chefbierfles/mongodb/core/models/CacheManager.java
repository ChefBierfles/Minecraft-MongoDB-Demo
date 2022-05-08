package com.chefbierfles.mongodb.core.models;

import com.chefbierfles.mongodb.core.annotations.DatabaseEntity;
import com.chefbierfles.mongodb.models.House;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import lombok.Getter;
import lombok.val;

import java.util.concurrent.TimeUnit;

public class CacheManager<C extends MongoObject<?>> {

    private @Getter final Class<C> classType;
    private final Database database;
    private @Getter
    LoadingCache<?, C> cachedObjects;

    public CacheManager(Class<C> classType, Database database) {
        this.database = database;
        this.classType = classType;

        initLoadingCacheConfiguration();
    }

    private void initLoadingCacheConfiguration() {
        val annotation = classType.getAnnotation(DatabaseEntity.class);
        if (!annotation.useCache()) return;
        val cacheBuilder = CacheBuilder.newBuilder();
        if (annotation.expiryInSecondsAfterAccess() > 0)
            cacheBuilder.expireAfterAccess(annotation.expiryInSecondsAfterAccess(), TimeUnit.SECONDS);
        if (annotation.maximumSize() > 0) cacheBuilder.maximumSize(annotation.maximumSize());
        cacheBuilder.removalListener((RemovalListener<Object, MongoObject<?>>) notification -> {
            if (!notification.wasEvicted()) return;
            // Add custom behavior here
            database.saveAsync(notification.getValue());
        });
        cachedObjects = cacheBuilder
                .build(new CacheLoader<Object, C>() {
                    @Override
                    public C load(Object key) {
                        return database.get(classType, "_id", key);
                    }
                });
    }
}
