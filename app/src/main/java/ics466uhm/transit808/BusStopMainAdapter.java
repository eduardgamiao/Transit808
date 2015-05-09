package ics466uhm.transit808;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eduardgamiao on 3/15/15.
 */
public class BusStopMainAdapter extends ArrayAdapter<BusStop> implements Filterable {
    // Array list of BusStops.
    private ArrayList<BusStop> busStops;
    private ArrayList<BusStop> originalStops;
    private int resource;

    public BusStopMainAdapter(Context context, int resource, ArrayList<BusStop> objects) {
        super(context, resource, objects);
        this.resource = resource;
        this.busStops = objects;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final BusStop stop = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(resource, null);
        }

        TextView title = (TextView) convertView.findViewById(R.id.stop_street_name);
        TextView stopID = (TextView) convertView.findViewById(R.id.stop_id);
        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
        ImageView saved = (ImageView) convertView.findViewById(R.id.starIcon);
        LinearLayout delete = (LinearLayout) convertView.findViewById(R.id.delete);

        title.setText(WordUtils.capitalizeFully(stop.getStreetName()));
        stopID.setText(stop.getStopID());
        icon.setImageDrawable(convertView.getResources().getDrawable(R.drawable.ic_directions_bus_black_48dp));
        saved.setImageDrawable(convertView.getResources().getDrawable(android.R.drawable.star_on));

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setMessage("Remove " + WordUtils.capitalizeFully(stop.getStreetName()) + " from saved bus stops?");
                        alertDialogBuilder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DatabaseHandler db = new DatabaseHandler(getContext());
                                if (db != null) {
                                    busStops.remove(stop);
                                    db.deleteStop(Integer.parseInt(stop.getStopID()));
                                    notifyDataSetChanged();
                                }
                            }
                        });

                alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

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
