package com.chefbierfles.mongodb.core.models;

import com.chefbierfles.mongodb.core.annotations.DatabaseEntity;
import com.chefbierfles.mongodb.typeadapters.ItemStackAdapter;
import com.chefbierfles.mongodb.typeadapters.LocationAdapter;
import com.chefbierfles.mongodb.typeadapters.UUIDAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import lombok.Getter;
import lombok.val;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;

public class Database {

    private MongoDatabase database;
    private final JsonWriterSettings jsonWriterSettings = JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build();
    private final UuidRepresentation uuidRepresentation = UuidRepresentation.STANDARD;
    public final @Getter
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(UUID.class, new UUIDAdapter())
            .registerTypeAdapter(Location.class, new LocationAdapter(this))
            .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackAdapter())
            .disableHtmlEscaping()
            .create();

    private final Map<Class<? extends MongoObject>, MongoCollection<Document>> registeredCollections = new HashMap<>();
    private final Set<CacheManager> cacheManagers = new HashSet<>();

    public Database(String databaseName) {
        initMongoDatabase(databaseName);
    }

    private void initMongoDatabase(String databaseName) {
        val connectionString = new ConnectionString("mongodb://admin:O5oHINE77BvE@cluster0-shard-00-00.zfbz8.mongodb.net:27017,cluster0-shard-00-01.zfbz8.mongodb.net:27017,cluster0-shard-00-02.zfbz8.mongodb.net:27017/unchained?replicaSet=atlas-n6lu65-shard-0&ssl=true&authSource=admin");
        val mongoClientSettings = MongoClientSettings.builder()
                .retryReads(true)
                .retryWrites(true)
                .uuidRepresentation(uuidRepresentation)
                .applyConnectionString(connectionString).build();
        val mongoClient = MongoClients.create(mongoClientSettings);
        database = mongoClient.getDatabase(databaseName);

        val reflections = new Reflections("com.unchainedproject.unchained");

        reflections.getSubTypesOf(MongoObject.class).stream().filter(clazz -> clazz.isAnnotationPresent(DatabaseEntity.class)).forEach((clazz) -> {
            val annotation = clazz.getAnnotation(DatabaseEntity.class);
            val collectionName = annotation.collectionName();
            if (annotation.useCache()) registerCacheManager(clazz);
            Bukkit.getLogger().log(Level.INFO, "Registering database collections");
            registerCollection(clazz, collectionName);
        });
    }

    private void registerCacheManager(Class<? extends MongoObject> clazz) {
        cacheManagers.add(new CacheManager(clazz, this));
    }

    private void registerCollection(Class<? extends MongoObject> clazz, String collectionName) {
        Bukkit.getLogger().log(Level.INFO, String.format("Collection with name %s found for MongoObject %s", collectionName, clazz.getSimpleName()));
        registeredCollections.putIfAbsent(clazz, database.getCollection(collectionName));
    }

    public <C extends MongoObject<?>, K> CompletableFuture<C> getAsync(Class<C> clazz, String fieldName, K value) {
        return CompletableFuture.supplyAsync(() -> get(clazz, fieldName, value));
    }

    @Nullable
    public <C extends MongoObject<?>, K> CompletableFuture<List<C>> getAllAsync(Class<C> clazz, String fieldName, K value) {
        return CompletableFuture.supplyAsync(() -> getAll(clazz, fieldName, value));
    }

    @Nullable
    public <C extends MongoObject<?>, K> CompletableFuture<List<C>> getAllAsync(Class<C> clazz) {
        return CompletableFuture.supplyAsync(() -> getAll(clazz));
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
    public <C extends MongoObject<?>, K> C get(Class<C> clazz, String fieldName, K value) {
        if (clazz.getAnnotation(DatabaseEntity.class).useCache())
            return (C) getCacheManager(clazz).getCachedObjects().getUnchecked(value);
        Object copyOfValue = value;
        if (value instanceof UUID) {
            copyOfValue = value.toString();
        }
        val document = registeredCollections.get(clazz).find(eq(fieldName, copyOfValue)).first();
        if (document == null) return null;
        return gson.fromJson(document.toJson(jsonWriterSettings), clazz);
    }

    @Nullable
    public <C extends MongoObject<?>, K> List<C> getAll(Class<C> clazz, String fieldName, K value) {
        return new ArrayList<C>() {{
            Object copyOfValue = value;
            if (value instanceof UUID) {
                copyOfValue = value.toString();
            }
            try (val mongoCursor = registeredCollections.get(clazz).find(eq(fieldName, copyOfValue)).iterator()) {
                while(mongoCursor.hasNext()) {
                    add(gson.fromJson(mongoCursor.next().toJson(jsonWriterSettings), clazz));
                }
            }
        }};
    }

    @Nullable
    public <C extends MongoObject<?>> List<C> getAll(Class<C> clazz) {
        val mongoCollection = registeredCollections.get(clazz);
        return new ArrayList<C>() {{
            try (val mongoCursor = mongoCollection.find().iterator()) {
                while(mongoCursor.hasNext()) {
                    add(gson.fromJson(mongoCursor.next().toJson(jsonWriterSettings), clazz));
                }
            }
        }};
    }

    private <O extends MongoObject<?>> CacheManager getCacheManager(Class<O> clazz) {
        return cacheManagers.stream().filter(c -> c.getClassType() == clazz).findFirst().get();
    }

    public <O extends MongoObject<?>> void create(Class<O> clazz, Collection<O> objects) {
        val documents = objects.stream().map(o -> Document.parse(gson.toJson(o))).collect(Collectors.toList());
        registeredCollections.get(clazz).insertMany(documents);
    }

    public <O extends MongoObject<?>> void create(O object) {
        val document = Document.parse(gson.toJson(object));
        registeredCollections.get(object.getClass()).insertOne(document);
    }

    public <O extends MongoObject<?>> void update(Class<O> clazz, Collection<O> objects) {
        val replaceModels = objects.stream().map(o -> new ReplaceOneModel<>(eq("_id", o.getId()), Document.parse(gson.toJson(o)))).collect(Collectors.toList());
        registeredCollections.get(clazz).bulkWrite(replaceModels);
        if (clazz.getAnnotation(DatabaseEntity.class).useCache()) {
            val cacheManager = getCacheManager(clazz);
            objects.forEach(o -> cacheManager.getCachedObjects().refresh(o.getId()));
        }
    }

    public <O extends MongoObject<?>> void update(O object) {
        val document = Document.parse(gson.toJson(object));
        val clazz = object.getClass();
        registeredCollections.get(object.getClass()).replaceOne(eq("_id", document.get("_id")), document, new ReplaceOptions().upsert(true));
        if (!clazz.getAnnotation(DatabaseEntity.class).useCache()) return;
        val cachedObjects = getCacheManager(clazz).getCachedObjects();
        if (!cachedObjects.asMap().containsKey(object.getId())) return;
        cachedObjects.refresh(object.getId());
    }

    public <O extends MongoObject<?>> void save(O object) {
        val objectFound = get(object.getClass(), "_id", object.getId());
        if (objectFound == null) create(object);
        else update(object);
    }

    public <O extends MongoObject<?>> void save(Class<O> clazz, Collection<O> objects) {
        val existingObjectIdsFound = objects.stream().map(o -> get(clazz, "_id", o.getId())).collect(Collectors.toList());
        val objectsToUpdate = objects.stream().filter(o -> o.getId() != null && existingObjectIdsFound.contains(o.getId())).collect(Collectors.toList());
        val objectsToCreate = objects.stream().filter(o -> o.getId() == null && !existingObjectIdsFound.contains(o.getId())).collect(Collectors.toList());
        create(clazz, objectsToCreate);
        update(clazz, objectsToUpdate);
    }

    public <O extends MongoObject<?>> void delete(O object) {
        val document = Document.parse(gson.toJson(object));
        val clazz = object.getClass();
        registeredCollections.get(object.getClass()).deleteOne(eq("_id", document.get("_id")));
        if (clazz.getAnnotation(DatabaseEntity.class).useCache()) {
            val cacheManager = getCacheManager(clazz);
            cacheManager.getCachedObjects().invalidate(object.getId());
        }
    }
}
