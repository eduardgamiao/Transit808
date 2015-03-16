package ics466uhm.transit808;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class BusStopSearchActivity extends ActionBarActivity {

    private ListView listView;
    BusStopAdapter adapter;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_result);

        ArrayList stops = (ArrayList) getStops();

        listView = (ListView) findViewById(R.id.results);
        editText = (EditText) findViewById(R.id.inputSearch);

        // Adding items to list view.
        //adapter = new ArrayAdapter<BusStop>(this, R.layout.list_item, R.id.busStop, stops);
        adapter = new BusStopAdapter(this, R.layout.list_item, stops);
        listView.setAdapter(adapter);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                BusStopSearchActivity.this.adapter.getFilter().filter(s);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 Toast.makeText(getApplicationContext(),
                         parent.getAdapter().getItem(position).toString(),
                         Toast.LENGTH_SHORT).show();
            }
        });
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

    private List<BusStop> getStops() {
        List<BusStop> stops = new ArrayList<BusStop>();
        BufferedReader br = null;

        try {
            br = new BufferedReader(
                    new InputStreamReader(getAssets().open("stops.txt")));
            String currentLine;
            String []lineArray;
            while ((currentLine = br.readLine()) != null) {
                lineArray = currentLine.split(",");
                stops.add(new BusStop(lineArray[0] + "," + lineArray[2], lineArray[7], lineArray[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stops;
    }
}
