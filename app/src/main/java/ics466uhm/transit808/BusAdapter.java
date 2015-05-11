package ics466uhm.transit808;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eduardgamiao on 5/9/15.
 */
public class BusAdapter extends ArrayAdapter<Bus> {
    private List<Bus> buses;
    private int resource;

    public BusAdapter(Context context, int resource, ArrayList<Bus> buses) {
        super(context, resource, buses);
        this.buses = buses;
        this.resource = resource;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Bus bus = buses.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(resource, null);
        }

        TextView route = (TextView) convertView.findViewById(R.id.route);
        TextView headsign = (TextView) convertView.findViewById(R.id.headsign);
        TextView arrivalTime = (TextView) convertView.findViewById(R.id.arrivalTime);
        TextView arrivalText = (TextView) convertView.findViewById(R.id.arrivalText);

        if (bus != null) {
            route.setText(bus.getRoute());
            headsign.setText(bus.getHeadsign());
            arrivalTime.setText(bus.getStopTime());
            arrivalText.setText(bus.getTextTime());
        }
        return convertView;
    }
}
