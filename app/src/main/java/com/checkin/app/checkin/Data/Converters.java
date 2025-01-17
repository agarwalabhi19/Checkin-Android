package com.checkin.app.checkin.Data;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.checkin.app.checkin.User.UserModel.GENDER;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import io.objectbox.converter.PropertyConverter;

public class Converters {
    public static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String TAG = Converters.class.getSimpleName();

    public static JsonNode getJsonNode(@NonNull String json) {
        try {
            return objectMapper.readTree(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static <T> T getObjectFromJson(String json, TypeReference typeReference) {
        T res;
        try {
            res = objectMapper.readValue(json, typeReference);
        } catch (IOException exception) {
            res = null;
            Log.e(TAG, "JSON invalid! " + json);
        }
        return res;
    }

    public static class MapConverter<X, Y> implements PropertyConverter<Map<X, Y>, String> {

        @Override
        public Map<X, Y> convertToEntityProperty(String databaseValue) {
            TypeReference type = new TypeReference<Map<X, Y>>() {
            };
            return getObjectFromJson(databaseValue, type);
        }

        @Override
        public String convertToDatabaseValue(Map<X, Y> entityProperty) {
            try {
                return objectMapper.writeValueAsString(entityProperty);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return "";
        }
    }

    public static class ListConverter<T> implements PropertyConverter<List<T>, String> {

        @Override
        public List<T> convertToEntityProperty(String databaseValue) {
            TypeReference type = new TypeReference<List<T>>() {
            };
            return getObjectFromJson(databaseValue, type);
        }

        @Override
        public String convertToDatabaseValue(List<T> entityProperty) {
            try {
                return objectMapper.writeValueAsString(entityProperty);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return "";
        }
    }

    public static class GenderConverter implements PropertyConverter<GENDER, Character> {
        @Override
        public GENDER convertToEntityProperty(Character databaseValue) {
            return GENDER.getByTag(databaseValue);
        }

        @Override
        public Character convertToDatabaseValue(GENDER entityProperty) {
            switch (entityProperty) {
                case MALE:
                    return 'm';
                case FEMALE:
                    return 'f';
            }
            return null;
        }
    }
}