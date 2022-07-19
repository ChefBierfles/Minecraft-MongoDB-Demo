package com.chefbierfles.mongodb.core.models;

import com.chefbierfles.mongodb.core.annotations.DatabaseObject;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import lombok.Getter;
import lombok.val;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;

public class DatabaseEngine extends DatabaseConfiguration {

    private @Getter
    final MongoDatabase database;
    private final Map<Class<? extends MongoObject>, MongoCollection<Document>> registeredCollections = new HashMap<>();

    public DatabaseEngine(String databaseName) {
        super(new ConnectionString("mongodb://admin:O5oHINE77BvE@cluster0-shard-00-00.zfbz8.mongodb.net:27017,cluster0-shard-00-01.zfbz8.mongodb.net:27017,cluster0-shard-00-02.zfbz8.mongodb.net:27017/unchained?replicaSet=atlas-n6lu65-shard-0&ssl=true&authSource=admin"));
        val mongoClient = MongoClients.create(mongoClientSettings);
        database = mongoClient.getDatabase(databaseName);
        registerDatabaseCollections();
    }

    private void registerDatabaseCollections() {
        val reflections = new Reflections("com.unchainedproject.unchained");
        reflections.getSubTypesOf(MongoObject.class).stream().filter(clazz -> clazz.isAnnotationPresent(DatabaseObject.class)).forEach((clazz) -> {
            val annotation = clazz.getAnnotation(DatabaseObject.class);
            val collectionName = annotation.collectionName();;
            registerCollection(clazz, collectionName);
        });
    }
    private void registerCollection(Class<? extends MongoObject> clazz, String collectionName) {
        Bukkit.getLogger().log(Level.INFO, String.format("Collection with name %s found for MongoObject %s", collectionName, clazz.getSimpleName()));
        registeredCollections.putIfAbsent(clazz, database.getCollection(collectionName));
    }
}
