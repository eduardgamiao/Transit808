package ics466uhm.transit808;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;

import java.util.ArrayList;

/**
 * Created by eduardgamiao on 3/18/15.
 */
public class DirectionStepAdapter extends ArrayAdapter<DirectionStep> {
    private ArrayList<DirectionStep> directions;

    public DirectionStepAdapter(Context context, ArrayList<DirectionStep> directions) {
        super(context, 0, directions);
        this.directions = directions;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final DirectionStep step = directions.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.direction_step, parent, false);
        }

        TextView instruction = (TextView) convertView.findViewById(R.id.instruction);
        TextView stops = (TextView) convertView.findViewById(R.id.stops);
        TextView viewOnMap = (TextView) convertView.findViewById(R.id.viewStop);
        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);

        instruction.setText(step.getInstruction());
        if (step.getTravelMode().equals("TRANSIT")) {
            stops.setText("Take Bus [" + step.getRoute() + " - " +
                    step.getHeadsign() + "] from " + step.getDepartureStop() + " to " + step.getArrivalStop());
            stops.setVisibility(TextView.VISIBLE);
            icon.setImageDrawable(convertView.getResources().getDrawable(R.drawable.ic_directions_bus_black_48dp));
            viewOnMap.setVisibility(TextView.VISIBLE);
        }
        else {
            stops.setVisibility(TextView.GONE);
            viewOnMap.setVisibility(TextView.GONE);
            icon.setImageDrawable(convertView.getResources().getDrawable(R.drawable.ic_directions_walk_black_48dp));
        }

        viewOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return convertView;
    }
}
