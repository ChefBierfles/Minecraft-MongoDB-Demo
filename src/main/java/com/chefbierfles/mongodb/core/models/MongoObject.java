package com.chefbierfles.mongodb.core.models;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

public abstract class MongoObject<T> {

    @SerializedName("_id")
    private final @Getter T id;

    public MongoObject(T id) {
        this.id = id;
    }
}
