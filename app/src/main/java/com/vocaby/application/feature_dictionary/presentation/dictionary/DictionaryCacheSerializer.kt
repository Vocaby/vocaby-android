package com.vocaby.application.feature_dictionary.presentation.dictionary

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import com.vocaby.app.DictionaryCache
import java.io.InputStream
import java.io.OutputStream

object DictionaryCacheSerializer : Serializer<DictionaryCache> {
    override val defaultValue: DictionaryCache = DictionaryCache.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): DictionaryCache {
        try {
            return DictionaryCache.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: DictionaryCache, output: OutputStream) = t.writeTo(output)
}