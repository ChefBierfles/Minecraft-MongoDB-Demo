package com.chefbierfles.mongodb;

import com.chefbierfles.mongodb.core.models.DatabaseEngine;
import com.chefbierfles.mongodb.models.DemoPlayer;
import com.chefbierfles.mongodb.models.Time;
import lombok.Getter;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class MongoDemoPlugin extends JavaPlugin implements CommandExecutor, Listener {

    private final DatabaseEngine database;
    private final @Getter
    ConcurrentHashMap<UUID, DemoPlayer> cachedPlayers = new ConcurrentHashMap<>();

    public MongoDemoPlugin() {
        database = new DatabaseEngine("demo-plugin");
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

}
