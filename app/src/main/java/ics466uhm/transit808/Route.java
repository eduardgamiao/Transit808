package ics466uhm.transit808;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class Route extends ActionBarActivity {
    private ArrayList<DirectionStep> directions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        directions = bundle.getParcelableArrayList("directions");
        for (DirectionStep step : directions) {
            System.out.println(step);
        }
        TextView title = (TextView) findViewById(R.id.route_title);
        String origin = intent.getStringExtra(Trips.ORIGIN);
        String destination = intent.getStringExtra(Trips.DESTINATION);
        title.setText("Start: " + origin + "\nEnd: " + destination);
        DirectionStepAdapter directionAdapter = new DirectionStepAdapter(this, directions);
        ListView listView = (ListView) findViewById(R.id.directions);
        listView.setAdapter(directionAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_route, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
