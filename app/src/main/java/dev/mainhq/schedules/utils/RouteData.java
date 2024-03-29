package dev.mainhq.schedules.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class RouteData {
    private final short id;
    private final String serviceId;
    private final String tripId;
    private final String headSign;
    @Nullable
    private final Direction dir;
    private final boolean wheelChairAccess;

    public RouteData(short id, String serviceId, String tripId, String headSign, /*String dir,*/ boolean wheelChairAccess) {
        this.id = id;
        this.serviceId = serviceId;
        this.tripId = tripId;
        this.headSign = headSign;
        //todo check how to parse direction
        //first check if bus 1-5 (exceptions)
        if (this.id >= 10) {
            char tmp = this.headSign.split("-")[1].charAt(0);
            switch(tmp){
                //careful because in french, Ouest =/= West
                case 'O':
                    this.dir = Direction.WEST;
                    break;
                case 'E':
                    this.dir = Direction.EAST;
                    break;
                case 'N':
                    this.dir = Direction.NORTH;
                    break;
                case 'S':
                    this.dir = Direction.SOUTH;
                    break;
                default:
                    //todo for testing
                    throw new IllegalStateException();
            }
        }
        else{
            //todo these are metro lines
            this.dir = null;
        }
        this.wheelChairAccess = wheelChairAccess;
    }

    public Direction getDir(){return this.dir;}

    @NonNull
    @Override
    public String toString(){
        return "\nBusNum: " + this.id + "\n" +
                "HeadSign: " + this.headSign + "\n" +
                "Dir" + this.dir + "\n" +
                "WheelChairAccess: " + this.wheelChairAccess;
    }
}
