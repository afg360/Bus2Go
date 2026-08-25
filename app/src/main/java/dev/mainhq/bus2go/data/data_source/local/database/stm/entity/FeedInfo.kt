package dev.mainhq.bus2go.data.data_source.local.database.stm.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity()
data class FeedInfo (
    @PrimaryKey val id : Int,
    @ColumnInfo(name="feed_publisher_name") val feedPublisherName : String,
    @ColumnInfo(name="feed_publisher_url") val feedPublisherUrl : String,
    @ColumnInfo(name="feed_lang") val feedPublisherLang : String,
    @ColumnInfo(name="feed_start_date") val feedStartDate : Int,
    @ColumnInfo(name="feed_end_date") val feedEndDate : Int,
    @ColumnInfo(name="feed_version") val feedVersion : String?,
)