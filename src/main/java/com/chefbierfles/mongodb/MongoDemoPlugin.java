package com.chefbierfles.mongodb;

import com.chefbierfles.mongodb.core.models.Database;
import com.chefbierfles.mongodb.models.DemoPlayer;
import lombok.Getter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class MongoDemoPlugin extends JavaPlugin implements CommandExecutor, Listener {

    private final Database database;
    private final @Getter
    ConcurrentHashMap<UUID, DemoPlayer> cachedPlayers = new ConcurrentHashMap<>();

    public MongoDemoPlugin() {
        database = new Database("demo-plugin");
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }
}
