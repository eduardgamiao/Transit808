package ics466uhm.transit808;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
        DirectionStep step = directions.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.direction_step, parent, false);
        }

        TextView instruction = (TextView) convertView.findViewById(R.id.instruction);
        TextView stops = (TextView) convertView.findViewById(R.id.stops);

        instruction.setText("\u2192 " + step.getInstruction());
        if (!((step.getDepartureStop().equals("") && step.getArrivalStop().equals("")))) {
            stops.setText("\t" + step.getDepartureStop() + " to " + step.getArrivalStop());
            stops.setVisibility(TextView.VISIBLE);
        }
        else {
            stops.setVisibility(TextView.GONE);
        }

        return convertView;
    }
}
