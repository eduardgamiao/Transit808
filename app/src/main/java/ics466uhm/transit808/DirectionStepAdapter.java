package ics466uhm.transit808;

import android.content.Context;
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

    public DirectionStepAdapter(Context context, int resource, ArrayList<DirectionStep> directions) {
        super(context, resource, directions);
        this.directions = directions;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        DirectionStep step = directions.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.trip_planner, null);
        }

        return convertView;
    }
}
