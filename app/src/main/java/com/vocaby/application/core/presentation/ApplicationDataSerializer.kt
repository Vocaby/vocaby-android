package com.vocaby.application.core.presentation

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import com.vocaby.app.ApplicationData
import java.io.InputStream
import java.io.OutputStream

object ApplicationDataSerializer : Serializer<ApplicationData> {
    override val defaultValue: ApplicationData = ApplicationData.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): ApplicationData {
        try {
            return ApplicationData.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: ApplicationData, output: OutputStream) = t.writeTo(output)
}