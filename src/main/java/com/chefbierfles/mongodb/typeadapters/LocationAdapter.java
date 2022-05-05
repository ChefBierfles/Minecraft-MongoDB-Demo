package com.chefbierfles.mongodb.typeadapters;

import com.chefbierfles.mongodb.core.models.Database;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Location;

import java.io.IOException;
import java.util.HashMap;

public class LocationAdapter extends TypeAdapter<Location> {

    private final Database demoDatabase;

    public LocationAdapter(Database demoDatabase) {
        this.demoDatabase = demoDatabase;
    }

    @Override
    public Location read(JsonReader jsonReader) throws IOException {
        return Location.deserialize(demoDatabase.getGson().fromJson(jsonReader.nextString(), HashMap.class));
    }

    @Override
    public void write(JsonWriter jsonWriter, Location location) throws IOException {
        jsonWriter.value(demoDatabase.getGson().toJson(location.serialize()));
    }
}

