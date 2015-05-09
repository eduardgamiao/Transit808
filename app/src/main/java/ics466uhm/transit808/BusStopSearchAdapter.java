package ics466uhm.transit808;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eduardgamiao on 3/15/15.
 */
public class BusStopSearchAdapter extends ArrayAdapter<BusStop> implements Filterable {
    // Array list of BusStops.
    private ArrayList<BusStop> busStops;
    private ArrayList<BusStop> originalStops;
    private int resource;

    public BusStopSearchAdapter(Context context, int resource, ArrayList<BusStop> objects) {
        super(context, resource, objects);
        this.resource = resource;
        this.busStops = objects;

    }

    public View getView(int position, View convertView, ViewGroup parent) {
        BusStop stop = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(resource, null);
        }

        TextView title = (TextView) convertView.findViewById(R.id.stop_street_name);
        TextView stopID = (TextView) convertView.findViewById(R.id.stop_id);
        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
        ImageView saved = (ImageView) convertView.findViewById(R.id.starIcon);

        title.setText(WordUtils.capitalizeFully(stop.getStreetName()));
        stopID.setText(stop.getStopID());
        icon.setImageDrawable(convertView.getResources().getDrawable(R.drawable.ic_directions_bus_black_48dp));
        DatabaseHandler db = new DatabaseHandler(getContext());
        if (db.getStop(Integer.parseInt(stop.getStopID())) != null) {
            saved.setImageDrawable(convertView.getResources().getDrawable(android.R.drawable.star_on));
        }
        else {
            saved.setImageDrawable(convertView.getResources().getDrawable(android.R.drawable.star_off));
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return busStops.size();
    }

    @Override
    public BusStop getItem(int position) {
        return busStops.get(position);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<BusStop> filteredStops = new ArrayList<BusStop>();

                if (originalStops == null) {
                    originalStops = new ArrayList<BusStop>(busStops);
                }

                if (constraint == null || constraint.length() == 0) {
                    results.count = originalStops.size();
                    results.values = originalStops;
                }
                else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < originalStops.size(); i++) {
                        BusStop data = originalStops.get(i);
                        if (data.toString().toLowerCase().contains(constraint.toString())) {
                            filteredStops.add(data);
                        }
                    }
                    results.count = filteredStops.size();
                    results.values = filteredStops;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                busStops = (ArrayList<BusStop>) results.values;
                notifyDataSetChanged();
            }
        };
        return filter;
    }

}
