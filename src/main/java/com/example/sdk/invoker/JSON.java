package com.example.sdk.invoker;

import java.lang.reflect.Type;

/**
 * Service Provider Interface for JSON serialization.
 * Inject an implementation of this interface into ApiClient to handle JSON parsing.
 */
public interface JSON {
    /**
     * Serialize the given object to a JSON string.
     */
    String serialize(Object obj);

    /**
     * Deserialize the given JSON string to the target type.
     */
    <T> T deserialize(String json, Type type);

    abstract class TypeReference<T> {
        private final Type type;

        protected TypeReference() {
            Type superClass = getClass().getGenericSuperclass();
            if (superClass instanceof Class<?>) {
                throw new IllegalArgumentException("TypeReference constructed without actual type information");
            }
            this.type = ((java.lang.reflect.ParameterizedType) superClass).getActualTypeArguments()[0];
        }

        public Type getType() {
            return type;
        }
    }
}
