package ics466uhm.transit808;

/**
 * Represents a single direction step in a trip.
 * Created by eduardgamiao on 3/18/15.
 */
public class DirectionStep {
    private String instruction;
    private String departureStop = "";
    private String arrivalStop = "";

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
}
