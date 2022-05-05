package com.chefbierfles.mongodb.typeadapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.UUID;

public class UUIDAdapter extends TypeAdapter<UUID> {

    @Override
    public UUID read(JsonReader jsonReader) throws IOException {
        return UUID.fromString(jsonReader.nextString());
    }

    @Override
    public void write(JsonWriter jsonWriter, UUID uuid) throws IOException {
        jsonWriter.value(uuid.toString());
    }
}
