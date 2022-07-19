package com.chefbierfles.mongodb.models;

import com.chefbierfles.mongodb.core.annotations.DatabaseObject;
import com.chefbierfles.mongodb.core.models.MongoObject;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@DatabaseObject(collectionName = "house", useCache = true, expiryInSecondsAfterAccess = 60)
public class House extends MongoObject<UUID> {

    private final @Getter String name;
    private final @Getter Set<DemoPlayer> members = new HashSet<>();

    public House(UUID id, String name) {
        super(id);
        this.name = name;
    }
}
