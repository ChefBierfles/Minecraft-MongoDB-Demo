package com.chefbierfles.mongodb.core.models;

import com.chefbierfles.mongodb.core.annotations.DatabaseObject;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.DeleteOptions;
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

    public <C extends MongoObject<?>, K> CompletableFuture<Optional<C>> getAsync(Class<C> clazz, String fieldName, K value) {
        return CompletableFuture.supplyAsync(() -> get(clazz, fieldName, value)).exceptionally(e -> {
            e.printStackTrace();
            return Optional.empty();
        });
    }

    public <C extends MongoObject<?>, K> CompletableFuture<List<C>> getAllAsync(Class<C> clazz, String fieldName, K value) {
        return CompletableFuture.supplyAsync(() -> getAll(clazz, fieldName, value)).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public <C extends MongoObject<?>, K> CompletableFuture<Optional<C>> getAsync(Class<C> clazz, K id) {
        return CompletableFuture.supplyAsync(() -> get(clazz, "_id", id)).exceptionally(e -> {
            e.printStackTrace();
            return Optional.empty();
        });
    }

    public <C extends MongoObject<?>, K> CompletableFuture<List<C>> getAllAsync(Class<C> clazz, K id) {
        return CompletableFuture.supplyAsync(() -> getAll(clazz, "_id", id)).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public <O extends MongoObject<?>> CompletableFuture<Void> createAsync(O object) {
        return CompletableFuture.runAsync(() -> create(object)).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public <O extends MongoObject<?>> CompletableFuture<Void> updateAsync(O object) {
        return CompletableFuture.runAsync(() -> update(object)).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public <O extends MongoObject<?>> CompletableFuture<Void> createAsync(Class<O> clazz, Collection<O> objects) {
        return CompletableFuture.runAsync(() -> create(clazz, objects)).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public <O extends MongoObject<?>> CompletableFuture<Void> updateAsync(Class<O> clazz, Collection<O> objects) {
        return CompletableFuture.runAsync(() -> update(clazz, objects)).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public <O extends MongoObject<?>> CompletableFuture<Void> saveAsync(O object) {
        return CompletableFuture.runAsync(() -> save(object)).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public <O extends MongoObject<?>> CompletableFuture<Void> saveAsync(Class<O> clazz, List<O> objects) {
        return CompletableFuture.runAsync(() -> save(clazz, objects)).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public <O extends MongoObject<?>> CompletableFuture<Void> deleteAsync(O object) {
        return CompletableFuture.runAsync(() -> delete(object)).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public <C extends MongoObject<?>, K> Optional<C> get(Class<C> classType, K id) {
        return get(classType, "_id", id);
    }

    public <C extends MongoObject<?>, K> Optional<C> get(Class<C> classType, String fieldName, K value) {
        Object copyOfValue = value;
        if (value instanceof UUID) {
            copyOfValue = value.toString();
        }
        val classAnnotation = classType.getAnnotation(DatabaseObject.class);
        val mongoCollection = getDatabase().getCollection(classAnnotation.collectionName());
        val document = mongoCollection.find(eq(fieldName, copyOfValue)).first();
        if (document == null) return Optional.empty();
        return Optional.of(getGson().fromJson(document.toJson(getJsonWriterSettings()), classType));
    }

    public <C extends MongoObject<?>, K> List<C> getAll(Class<C> classType, K id) {
        return getAll(classType, "_id", id);
    }

    public <C extends MongoObject<?>, K> List<C> getAll(Class<C> classType, String fieldName, K value) {
        val classAnnotation = classType.getAnnotation(DatabaseObject.class);
        val mongoCollection = getDatabase().getCollection(classAnnotation.collectionName());
        return new ArrayList<C>() {{
            Object copyOfValue = value;
            if (value instanceof UUID) {
                copyOfValue = value.toString();
            }
            try (val mongoCursor = mongoCollection.find(eq(fieldName, copyOfValue)).iterator()) {
                while(mongoCursor.hasNext()) {
                    add(getGson().fromJson(mongoCursor.next().toJson(getJsonWriterSettings()), classType));
                }
            }
        }};
    }

    public <O extends MongoObject<?>> void create(Class<O> classType, Collection<O> objects) {
        val classAnnotation = classType.getAnnotation(DatabaseObject.class);
        val mongoCollection = getDatabase().getCollection(classAnnotation.collectionName());
        val documents = objects.stream().map(o -> Document.parse(getGson().toJson(o))).collect(Collectors.toList());
        mongoCollection.insertMany(documents);
    }

    public <O extends MongoObject<?>> void create(O clazz) {
        val classAnnotation = clazz.getClass().getAnnotation(DatabaseObject.class);
        val mongoCollection = getDatabase().getCollection(classAnnotation.collectionName());
        val document = Document.parse(getGson().toJson(clazz));
        mongoCollection.insertOne(document);
    }

    public <O extends MongoObject<?>> void update(Class<O> classType, Collection<O> objects) {
        val classAnnotation = classType.getAnnotation(DatabaseObject.class);
        val mongoCollection = getDatabase().getCollection(classAnnotation.collectionName());
        val replaceModels = objects.stream().map(o -> new ReplaceOneModel<>(eq("_id", o.getId()), Document.parse(getGson().toJson(o)))).collect(Collectors.toList());
        mongoCollection.bulkWrite(replaceModels);
    }

    public <O extends MongoObject<?>> void update(O clazz) {
        val classAnnotation = clazz.getClass().getAnnotation(DatabaseObject.class);
        val mongoCollection = getDatabase().getCollection(classAnnotation.collectionName());
        val document = Document.parse(getGson().toJson(clazz));
        mongoCollection.replaceOne(eq("_id", document.get("_id")), document, new ReplaceOptions().upsert(true));
    }

    public <O extends MongoObject<?>> void save(O clazz) {
        val objectFound = get(clazz.getClass(), "_id", clazz.getId());
        if (objectFound == null) create(clazz);
        else update(clazz);
    }

    public <O extends MongoObject<?>> void save(Class<O> classType, Collection<O> objects) {
        val existingObjectIdsFound = objects.stream().map(o -> get(classType, "_id", o.getId())).collect(Collectors.toList());
        val objectsToUpdate = objects.stream().filter(o -> existingObjectIdsFound.contains(o.getId())).collect(Collectors.toList());
        val objectsToCreate = objects.stream().filter(o -> !existingObjectIdsFound.contains(o.getId())).collect(Collectors.toList());
        create(classType, objectsToCreate);
        update(classType, objectsToUpdate);
    }

    public <O extends MongoObject<?>> void delete(O clazz) {
        val classAnnotation = clazz.getClass().getAnnotation(DatabaseObject.class);
        val mongoCollection = getDatabase().getCollection(classAnnotation.collectionName());
        val document = Document.parse(getGson().toJson(clazz));
        mongoCollection.deleteOne(eq("_id", document.get("_id")));
    }
}
