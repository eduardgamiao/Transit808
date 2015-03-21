package ics466uhm.transit808;

/**
 * Represents a Bus Stop object.
 */
public class BusStop {
    private String coordinates;
    private String streetName;
    private String stopID;

    public BusStop(String coordinates, String streetName, String stopID) {
        this.coordinates = coordinates;
        this.streetName = streetName;
        this.stopID = stopID;
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
}
