package ics466uhm.transit808;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class BusStopSearchActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String message = intent.getStringExtra(ArrivalActivity.SEARCH_TERM);
        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText(message);
        getStops(message);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bus_stop_search, menu);
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

    private String prepareURL(String busStopID) {
        String result = getResources().getString(R.string.hea_url).replace("API_key",
                getResources().getString(R.string.hea_api)).replace("stop_ID", busStopID);
        return result;
    }

    private void getStops(String searchTerm) {
        BufferedReader br = null;

        try {
            br = new BufferedReader(
                    new InputStreamReader(getAssets().open("stops.txt")));
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                if (currentLine.contains(searchTerm.toUpperCase())) {
                    Log.i("BUS STOP", currentLine);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
