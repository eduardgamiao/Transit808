package ics466uhm.transit808;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a single direction step in a trip.
 * Created by eduardgamiao on 3/18/15.
 */
public class DirectionStep implements Parcelable {
    public String instruction;
    public String departureStop = "";
    public String arrivalStop = "";

    /**
     * Constructor.
     * @param instruction Direction instruction.
     */
    public DirectionStep(String instruction) {
        this.instruction = instruction;
    }

    /**
     * Constructor.
     * @param instruction Direction instruction.
     * @param departureStop Departure stop.
     * @param arrivalStop Arrival stop.
     */
    public DirectionStep(String instruction, String departureStop, String arrivalStop) {
        this.instruction = instruction;
        this.departureStop = departureStop;
        this.arrivalStop = arrivalStop;
    }

    /**
     * Parcel constructor.
     * @return
     */
    public DirectionStep(Parcel in) {
        instruction = in.readString();
        departureStop = in.readString();
        arrivalStop = in.readString();
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getDepartureStop() {
        return departureStop;
    }

    public void setDepartureStop(String departureStop) {
        this.departureStop = departureStop;
    }

    public String getArrivalStop() {
        return arrivalStop;
    }

    public void setArrivalStop(String arrivalStop) {
        this.arrivalStop = arrivalStop;
    }

    public String toString() {
        if (this.arrivalStop.isEmpty() || this.departureStop.isEmpty()) {
            return this.instruction;
        }
        return this.instruction + " from " + this.departureStop + " to " + this.arrivalStop;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method sub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(instruction);
        dest.writeString(departureStop);
        dest.writeString(arrivalStop);
    }

    public static final Creator<DirectionStep> CREATOR = new Parcelable.Creator<DirectionStep>() {

        @Override
        public DirectionStep createFromParcel(Parcel source) {
            return new DirectionStep(source);
        }

        @Override
        public DirectionStep[] newArray(int size) {
            return new DirectionStep[size];
        }
    };
}
