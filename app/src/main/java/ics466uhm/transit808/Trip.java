package ics466uhm.transit808;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A trip between two locations.
 * Created by eduardgamiao on 4/9/15.
 */
public class Trip implements Parcelable {
    private String origin;
    private String destination;
    private String title;

    public Trip() {

    }

    public Trip(String origin, String destination, String title) {
        this.origin = origin;
        this.destination = destination;
        this.title = title;
    }

    public Trip(Parcel in) {
        origin = in.readString();
        destination = in.readString();
        title = in.readString();
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginShort() {
        String [] originSplit = origin.split(", ");
        if (originSplit.length > 0) {
            return originSplit[0];
        }
        return origin;
    }

    public String getDestinationShort() {
        String [] destinationSplit = destination.split(", ");
        if (destinationSplit.length > 0) {
            return destinationSplit[0];
        }
        return destination;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(origin);
        dest.writeString(destination);
        dest.writeString(title);
    }

    public static final Creator<Trip> CREATOR = new Parcelable.Creator<Trip>() {

        @Override
        public  Trip createFromParcel(Parcel source) {
            return new Trip(source);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };
}
