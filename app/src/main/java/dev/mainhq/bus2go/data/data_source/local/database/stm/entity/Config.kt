package dev.mainhq.bus2go.data.data_source.local.database.stm.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

//import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["version", "min_app_version_required", "max_app_version_required"], unique = true)])
data class Config(
	//@PrimaryKey //val id : Int,
	@ColumnInfo(name="version") val version : Int,
	@ColumnInfo(name="min_app_version_required") val minAppVersionRequired : Int,
	@ColumnInfo(name="max_app_version_required") val maxAppVersionRequired : Int,
)