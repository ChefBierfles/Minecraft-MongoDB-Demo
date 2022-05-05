package com.chefbierfles.mongodb.core.models;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mongodb.client.MongoCollection;
import lombok.Getter;
import org.bson.Document;

import java.lang.reflect.ParameterizedType;
import java.util.concurrent.TimeUnit;

public class CacheCollection<C extends MongoObject<?>> {

    private final Class<C> persistentClass;
    private final @Getter Database database;

    public @Getter MongoCollection<Document> mongoCollection;
    private LoadingCache<Object, MongoObject<?>> cachedObjects = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build(new CacheLoader<Object, MongoObject<?>>() {
                @Override
                public MongoObject<?> load(Object key) {
                    return database.get(persistentClass, "_id", key);
                }
            });

    public CacheCollection(Database database) {
        this.persistentClass = (Class<C>) ((ParameterizedType) this.getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
        this.database = database;
    }
}
