package dev.mainhq.schedules.database.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

//how to implement not null? and unique?
@Entity
public class Routes {
    @PrimaryKey
    @ColumnInfo(name="id")
    public int id;
    @ColumnInfo(name="routeid")
    public int routeId;
    @ColumnInfo(name="route_long_name")
    @NonNull
    public String routeLongName = "";
    @ColumnInfo(name="route_type")
    public int routeType;
    @ColumnInfo(name="route_color")
    @NonNull
    public String routeColor = "";
}
