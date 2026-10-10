package app.bus2go.android.data.data_source.local.database.stm.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface ConfigDAO {
	@Query("SELECT version FROM Config;")
	suspend fun getDbVersion(): Int

}