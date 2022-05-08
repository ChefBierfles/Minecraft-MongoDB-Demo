package com.chefbierfles.mongodb.models;

import com.chefbierfles.mongodb.core.annotations.DatabaseEntity;
import com.chefbierfles.mongodb.core.models.MongoObject;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

@DatabaseEntity(collectionName = "demoPlayers", useCache = true)
public class DemoPlayer extends MongoObject<UUID> {

    private final @Getter String name;
    private final @Getter long lastLoginTime;

    public DemoPlayer(UUID id, String name, long lastLoginTime) {
        super(id);
        this.name = name;
        this.lastLoginTime = lastLoginTime;
    }
}
