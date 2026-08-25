package dev.mainhq.bus2go.data.data_source.local.database.stm.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Query
import java.time.LocalDate

@Dao
interface FeedInfoDAO {
	@Query("SELECT feed_end_date FROM FeedInfo")
	suspend fun getExpirationDate() : LocalDate
}