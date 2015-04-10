package ics466uhm.transit808;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by eduardgamiao on 4/10/15.
 */
public class TripAdapter extends ArrayAdapter<Trip> {
    private ArrayList<Trip> trips = new ArrayList<Trip>();

    public TripAdapter(Context context, int resource, ArrayList<Trip> trips) {
        super(context, resource, trips);
        this.trips = trips;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Trip trip = trips.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.trips, parent, false);
        }

        TextView origin = (TextView) convertView.findViewById(R.id.origin_title);
        TextView destination = (TextView) convertView.findViewById(R.id.destination_title);

        if (trip != null) {
            origin.setText(trip.getOrigin());
            destination.setText(trip.getDestination());
        }

        return convertView;
    }
}
