package com.chefbierfles.mongodb.core.models;

import com.chefbierfles.mongodb.core.annotations.DatabaseObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import lombok.Getter;
import lombok.val;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;

public class DatabaseCollection<C extends MongoObject<?>> {

    private final @Getter MongoCollection<Document> mongoCollection;
    private final @Getter Class<C> classType;
    private final DatabaseEngine databaseEngine;

    public DatabaseCollection(DatabaseEngine databaseEngine, Class<C> classType) {
        this.databaseEngine = databaseEngine;
        this.classType = classType;
        val classAnnotation = classType.getAnnotation(DatabaseObject.class);
        this.mongoCollection = databaseEngine.getDatabase().getCollection(classAnnotation.collectionName());
    }

    public <C extends MongoObject<?>, K> CompletableFuture<C> getAsync(Class<C> clazz, String fieldName, K value) {
        return CompletableFuture.supplyAsync(() -> get(clazz, fieldName, value));
    }

    @Nullable
    public <C extends MongoObject<?>, K> CompletableFuture<List<C>> getAllAsync(Class<C> clazz, String fieldName, K value) {
        return CompletableFuture.supplyAsync(() -> getAll(clazz, fieldName, value));
    }

    public <C extends MongoObject<?>, K> CompletableFuture<C> getAsync(Class<C> clazz, K id) {
        return CompletableFuture.supplyAsync(() -> get(clazz, "_id", id));
    }

    @Nullable
    public <C extends MongoObject<?>, K> CompletableFuture<List<C>> getAllAsync(Class<C> clazz, K id) {
        return CompletableFuture.supplyAsync(() -> getAll(clazz, "_id", id));
    }

    public <O extends MongoObject<?>> void createAsync(O object) {
        CompletableFuture.runAsync(() -> create(object));
    }

    public <O extends MongoObject<?>> void updateAsync(O object) {
        CompletableFuture.runAsync(() -> update(object));
    }

    public <O extends MongoObject<?>> void createAsync(Class<O> clazz, Collection<O> objects) {
        CompletableFuture.runAsync(() -> create(clazz, objects));
    }

    public <O extends MongoObject<?>> void updateAsync(Class<O> clazz, Collection<O> objects) {
        CompletableFuture.runAsync(() -> update(clazz, objects));
    }

    public <O extends MongoObject<?>> void saveAsync(O object) {
        CompletableFuture.runAsync(() -> save(object));
    }

    public <O extends MongoObject<?>> void saveAsync(Class<O> clazz, List<O> objects) {
        CompletableFuture.runAsync(() -> save(clazz, objects));
    }

    public <O extends MongoObject<?>> void deleteAsync(O object) {
        CompletableFuture.runAsync(() -> deleteAsync(object));
    }

    @Nullable
    public <C extends MongoObject<?>, K> C get(Class<C> classType, K id) {
        return get(classType, "_id", id);
    }

    @Nullable
    public <C extends MongoObject<?>, K> C get(Class<C> classType, String fieldName, K value) {
        Object copyOfValue = value;
        if (value instanceof UUID) {
            copyOfValue = value.toString();
        }
        val document = mongoCollection.find(eq(fieldName, copyOfValue)).first();
        if (document == null) return null;
        return databaseEngine.getGson().fromJson(document.toJson(databaseEngine.getJsonWriterSettings()), classType);
    }

    @Nullable
    public <C extends MongoObject<?>, K> List<C> getAll(Class<C> classType, K id) {
        return getAll(classType, "_id", id);
    }

    @Nullable
    public <C extends MongoObject<?>, K> List<C> getAll(Class<C> classType, String fieldName, K value) {
        return new ArrayList<C>() {{
            Object copyOfValue = value;
            if (value instanceof UUID) {
                copyOfValue = value.toString();
            }
            try (val mongoCursor = mongoCollection.find(eq(fieldName, copyOfValue)).iterator()) {
                while(mongoCursor.hasNext()) {
                    add(databaseEngine.getGson().fromJson(mongoCursor.next().toJson(databaseEngine.getJsonWriterSettings()), classType));
                }
            }
        }};
    }

    public <O extends MongoObject<?>> void create(Class<O> classType, Collection<O> objects) {
        val documents = objects.stream().map(o -> Document.parse(databaseEngine.getGson().toJson(o))).collect(Collectors.toList());
        mongoCollection.insertMany(documents);
    }

    public <O extends MongoObject<?>> void create(O clazz) {
        val document = Document.parse(databaseEngine.getGson().toJson(clazz));
        mongoCollection.insertOne(document);
    }

    public <O extends MongoObject<?>> void update(Class<O> classType, Collection<O> objects) {
        val replaceModels = objects.stream().map(o -> new ReplaceOneModel<>(eq("_id", o.getId()), Document.parse(databaseEngine.getGson().toJson(o)))).collect(Collectors.toList());
        mongoCollection.bulkWrite(replaceModels);
    }

    public <O extends MongoObject<?>> void update(O clazz) {
        val document = Document.parse(databaseEngine.getGson().toJson(clazz));
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
        val document = Document.parse(databaseEngine.getGson().toJson(clazz));
        mongoCollection.deleteOne(eq("_id", document.get("_id")));
    }
}
