package dev.mainhq.bus2go.data.data_source.local.database.stm.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface ConfigDAO {
	@Query("SELECT version FROM Config;")
	suspend fun getDbVersion(): Int

}