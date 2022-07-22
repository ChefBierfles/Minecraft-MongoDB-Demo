package com.chefbierfles.mongodb.models;

import com.chefbierfles.mongodb.core.annotations.DatabaseObject;
import com.chefbierfles.mongodb.core.models.MongoObject;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.UUID;

@DatabaseObject(collectionName = "demoPlayers")
public class DemoPlayer extends MongoObject<UUID> {

    private final @Getter String name;
    private @Getter @Setter long lastLoginTime;

    private @Getter @Setter Location location;

    public DemoPlayer(UUID id, String name, long lastLoginTime) {
        super(id);
        this.name = name;
        this.lastLoginTime = lastLoginTime;
    }
}
