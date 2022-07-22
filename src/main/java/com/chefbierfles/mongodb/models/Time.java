package com.chefbierfles.mongodb.models;

import com.chefbierfles.mongodb.core.annotations.DatabaseObject;
import com.chefbierfles.mongodb.core.models.MongoObject;
import lombok.*;

import java.util.UUID;

@DatabaseObject(collectionName = "times")
public class Time extends MongoObject<UUID> {

    private @Getter @Setter long timestamp;
    private @With @Setter String message;

    public Time(String message) {
        super(UUID.randomUUID());
        this.message = message;
        timestamp = System.currentTimeMillis();
    }
}
