package com.chefbierfles.mongodb.typeadapters;

import com.chefbierfles.mongodb.core.models.DatabaseEngine;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.val;
import org.bson.json.JsonWriterSettings;
import org.bukkit.Location;

import java.io.IOException;
import java.util.HashMap;

public class LocationAdapter extends TypeAdapter<Location> {

    private final Gson gson;

    public LocationAdapter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Location read(JsonReader jsonReader) throws IOException {
        return Location.deserialize(gson.fromJson(jsonReader.nextString(), HashMap.class));
    }

    @Override
    public void write(JsonWriter jsonWriter, Location location) throws IOException {
        if (location == null)
            jsonWriter.nullValue();
        else
            jsonWriter.value(gson.toJson(location.serialize()));
    }
}

