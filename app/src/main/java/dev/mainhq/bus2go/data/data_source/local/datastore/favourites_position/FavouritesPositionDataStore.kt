package dev.mainhq.bus2go.data.data_source.local.datastore.favourites_position

import android.content.Context
import androidx.datastore.dataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.favourites_position.entity.FavouritesPositionDataSerializer


val Context.favouritesPositionDataStore by dataStore(
	fileName = "favourites_position_v1.json",
	serializer = FavouritesPositionDataSerializer,
//	produceMigrations = {}
)
