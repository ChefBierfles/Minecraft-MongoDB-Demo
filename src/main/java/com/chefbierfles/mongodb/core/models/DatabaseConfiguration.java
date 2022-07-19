package com.chefbierfles.mongodb.core.models;

import com.chefbierfles.mongodb.typeadapters.ItemStackAdapter;
import com.chefbierfles.mongodb.typeadapters.LocationAdapter;
import com.chefbierfles.mongodb.typeadapters.UUIDAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import lombok.val;
import org.bson.UuidRepresentation;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class DatabaseConfiguration {

    protected final @Getter JsonWriterSettings jsonWriterSettings = JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build();
    protected final @Getter UuidRepresentation uuidRepresentation = UuidRepresentation.STANDARD;
    protected final @Getter
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(UUID.class, new UUIDAdapter())
            .registerTypeAdapter(Location.class, new LocationAdapter(getGson()))
            .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackAdapter())
            .disableHtmlEscaping()
            .create();

    protected final @Getter MongoClientSettings mongoClientSettings;

    public DatabaseConfiguration(ConnectionString connectionString) {
        mongoClientSettings = MongoClientSettings.builder()
                .retryReads(true)
                .retryWrites(true)
                .uuidRepresentation(uuidRepresentation)
                .applyConnectionString(connectionString).build();
    }
}
