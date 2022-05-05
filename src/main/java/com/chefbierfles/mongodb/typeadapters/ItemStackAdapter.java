package com.chefbierfles.mongodb.typeadapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.val;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ItemStackAdapter extends TypeAdapter<ItemStack> {

    public ItemStack read(JsonReader jsonReader) throws IOException {
        try {
            val inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(jsonReader.nextString()));
            val dataInput = new BukkitObjectInputStream(inputStream);
            val bytes = (byte[]) dataInput.readObject();
            val itemStack = ItemStack.deserializeBytes(bytes);
            dataInput.close();
            return itemStack;
        } catch (Exception exc) {
            exc.printStackTrace();
            return null;
        }
    }

    public void write(JsonWriter jsonWriter, ItemStack itemStack) throws IOException {
        try {
            if (itemStack == null) return;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(itemStack.serializeAsBytes());
            dataOutput.close();
            jsonWriter.value(Base64Coder.encodeLines(outputStream.toByteArray()));
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}