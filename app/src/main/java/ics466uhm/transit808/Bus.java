package ics466uhm.transit808;

/**
 * Created by eduardgamiao on 5/9/15.
 */
public class Bus {
    private String route;
    private String headsign;
    private String stopTime;
    private String textTime;

    public Bus(String route, String headsign, String stopTime, String textTime) {
        this.route = route;
        this.headsign = headsign;
        this.stopTime = stopTime;
        this.textTime = textTime;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getHeadsign() {
        return headsign;
    }

    public void setHeadsign(String headsign) {
        this.headsign = headsign;
    }

    public String getStopTime() {
        return stopTime;
    }

    public void setStopTime(String stopTime) {
        this.stopTime = stopTime;
    }

    public String getTextTime() {
        return textTime;
    }

    public void setTextTime(String textTime) {
        this.textTime = textTime;
    }
}
