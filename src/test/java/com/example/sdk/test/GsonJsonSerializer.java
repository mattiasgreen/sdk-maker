package com.example.sdk.test;

import com.example.sdk.invoker.JsonSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

public class GsonJsonSerializer implements JsonSerializer {
    private final Gson gson = new GsonBuilder().create();

    @Override
    public String serialize(Object obj) {
        return gson.toJson(obj);
    }

    @Override
    public <T> T deserialize(String json, Type type) {
        return gson.fromJson(json, type);
    }
}
