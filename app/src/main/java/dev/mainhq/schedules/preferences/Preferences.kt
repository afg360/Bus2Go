package dev.mainhq.schedules.preferences

import android.content.Context
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import androidx.datastore.core.DataStore
import dev.mainhq.schedules.Favourites
import java.io.InputStream
import java.io.OutputStream

object SettingsSerializer : Serializer<Favourites> {
    override val defaultValue: Favourites = Favourites.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Favourites {
        try {
            return Favourites.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw Exception("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: Favourites, output: OutputStream) = t.writeTo(output)
}

val Context.settingsDataStore : DataStore<Favourites> by dataStore(
    fileName = "preferences_schema.proto",
    serializer = SettingsSerializer
)
