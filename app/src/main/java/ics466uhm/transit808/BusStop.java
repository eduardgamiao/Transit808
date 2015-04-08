package ics466uhm.transit808;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a Bus Stop object.
 */
public class BusStop implements Parcelable {
    private String coordinates;
    private String streetName;
    private String stopID;
    private String _ID;
    private String _streetName;
    private String _coordinates;

    /**
     * Empty constructor.
     */
    public BusStop() {

    }

    public BusStop(String coordinates, String streetName, String stopID) {
        this.coordinates = coordinates;
        this.streetName = streetName;
        this.stopID = stopID;
    }

    /**
     * Parcel constructor.
     * @return
     */
    public BusStop(Parcel in) {
        stopID = in.readString();
        streetName = in.readString();
        coordinates = in.readString();
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getStopID() {
        return stopID;
    }

    public void setStopID(String stopID) {
        this.stopID = stopID;
    }

    public String toString() {
        return this.streetName + "(#" + this.stopID + ")";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(stopID);
        dest.writeString(streetName);
        dest.writeString(coordinates);
    }

    public static final Creator<BusStop> CREATOR = new Parcelable.Creator<BusStop>() {

        @Override
        public  BusStop createFromParcel(Parcel source) {
            return new BusStop(source);
        }

        @Override
        public BusStop[] newArray(int size) {
            return new BusStop[size];
        }
    };

}
