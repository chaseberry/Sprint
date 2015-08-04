package edu.csh.chase.sprint

import com.squareup.okhttp.RequestBody
import edu.csh.chase.sprint.json.JsonArray
import edu.csh.chase.sprint.json.JsonBase
import edu.csh.chase.sprint.json.JsonObject
import edu.csh.chase.sprint.parameters.JsonBody

class JsonRequestSerializer : RequestSerializer {

    override fun isValidType(requestData: Any?): Boolean {
        return when (requestData) {
            is JsonBase -> true
            is Map<*, *> -> true
            is List<Any?> -> true
            else -> false
        }
    }

    override fun serialize(requestData: Any?): RequestBody? {
        if (!isValidType(requestData)) {
            return null
        }
        return when (requestData) {
            is Map<*, *> -> {
                try {
                    val castedData = requestData as? Map<String, Any?>
                    if (castedData == null) {
                        null
                    } else {
                        JsonBody(JsonObject(castedData))
                    }
                } catch(except: ClassCastException) {
                    null
                }

            }
            is List<Any?> -> JsonBody(JsonArray(requestData))
            is JsonBase -> JsonBody(requestData)
            else -> null
        }
    }

}