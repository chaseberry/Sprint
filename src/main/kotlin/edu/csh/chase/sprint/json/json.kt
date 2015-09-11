package edu.csh.chase.sprint.json

object json {

    /**
     * Constructs a JsonArray from a list of elements
     * If any provided element is not a valid json type it will be skipped
     *
     * @param elements A list of Any? elements to put into a JsonArray
     * @return A JsonArray containing only valid elements from the provided list
     */
    fun get(vararg elements: Any?): JsonArray {
        return JsonArray(*elements)
    }

    /**
     * Constructs a JsonObject from a list of Pair<String, Any?>
     * Provided pairs with invalid json types will be ignored
     *
     * @param json A list of key, value pairs
     * @return A JsonObject with only valid pairs from the provided lambda
     */
    fun invoke(vararg elements: Pair<String, Any?>): JsonObject {
        return JsonObject(*elements)
    }

}