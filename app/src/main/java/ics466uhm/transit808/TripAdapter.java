package ics466uhm.transit808;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
        final Trip trip = trips.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.trips, parent, false);
        }

        TextView origin = (TextView) convertView.findViewById(R.id.origin_title);
        TextView destination = (TextView) convertView.findViewById(R.id.destination_title);
        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
        ImageView delete = (ImageView) convertView.findViewById(R.id.deleteIcon);

        if (trip != null) {
            origin.setText(trip.getOriginShort());
            destination.setText(trip.getDestinationShort());
            icon.setImageDrawable(convertView.getResources().getDrawable(R.drawable.ic_directions_black_48dp));
            delete.setImageDrawable(convertView.getResources().getDrawable(R.drawable.ic_delete_black_48dp));
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseHandler db = new DatabaseHandler(getContext());
                    Trip savedTrip = db.getTrip(trip.getOrigin(), trip.getDestination());
                    if (savedTrip != null) {
                        db.deleteTrip(trip.getOrigin(), trip.getDestination());
                    }
                    Toast.makeText(getContext(), "Trip from " + trip.getOriginShort() + " to " + trip.getDestinationShort()
                            + " removed.", Toast.LENGTH_SHORT).show();
                    trips.remove(trip);
                    notifyDataSetChanged();
                }
            });
        }

        return convertView;
    }
}
