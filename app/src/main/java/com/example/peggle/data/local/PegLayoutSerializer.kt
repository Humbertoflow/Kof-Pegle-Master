package com.example.peggle.data.local

import com.example.peggle.data.PegTemplate
import com.example.peggle.model.PegShape
import com.example.peggle.model.PegType
import org.json.JSONArray
import org.json.JSONObject

object PegLayoutSerializer {

    fun serialize(templates: List<PegTemplate>): String {
        val array = JSONArray()
        for (t in templates) {
            val obj = JSONObject().apply {
                put("x", t.xRatio.toDouble())
                put("y", t.yRatio.toDouble())
                put("type", t.defaultType.name)
                put("shape", t.shape.name)
                put("w", t.width.toDouble())
                put("h", t.height.toDouble())
                put("rot", t.rotation.toDouble())
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun deserialize(json: String): List<PegTemplate> {
        if (json.isBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            val list = ArrayList<PegTemplate>(array.length())
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val type = try {
                    PegType.valueOf(obj.optString("type", PegType.BLUE.name))
                } catch (_: Exception) {
                    PegType.BLUE
                }
                val shape = try {
                    PegShape.valueOf(obj.optString("shape", PegShape.CIRCLE.name))
                } catch (_: Exception) {
                    PegShape.CIRCLE
                }
                list.add(
                    PegTemplate(
                        xRatio = obj.optDouble("x", 0.5).toFloat(),
                        yRatio = obj.optDouble("y", 0.5).toFloat(),
                        defaultType = type,
                        shape = shape,
                        width = obj.optDouble("w", 14.0).toFloat(),
                        height = obj.optDouble("h", 14.0).toFloat(),
                        rotation = obj.optDouble("rot", 0.0).toFloat()
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }
}
