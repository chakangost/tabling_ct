package com.tabling.homework.utils

import com.google.gson.*
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class GsonDateFormatAdapter : JsonSerializer<Date>, JsonDeserializer<Date> {

    private var dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)

    init {
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun serialize(src: Date?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(dateFormat.format(src))
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Date {
        try {
            return dateFormat.parse(json?.asString)
        } catch (e: ParseException) {
            throw JsonParseException(e)
        }
    }
}