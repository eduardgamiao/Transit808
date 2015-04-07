package ics466uhm.transit808;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Database handler class.
 * Based on http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
 * Created by eduardgamiao on 4/5/15.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    // Database version.
    private static final int DATABASE_VERSION = 1;

    //Database name.
    private static final String DATABASE_NAME = "transit808";

    // Table names.
    private static final String STOP_TABLE = "stops";
    private static final String TRIPS_TABLE = "trips";

    // Column names.
    private static final String STOPS_ID = "id";
    private static final String STOPS_STREET = "street_name";
    private static final String STOPS_COORDINATES = "coordinates";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creates tables for database.
     * @param db The application's database to be created.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_STOPS_TABLE = "CREATE TABLE " + STOP_TABLE + "("
                + STOPS_ID + " INTEGER PRIMARY KEY," + STOPS_STREET + " TEXT,"
                + STOPS_COORDINATES + " TEXT" + ")";
        db.execSQL(CREATE_STOPS_TABLE);
    }

    /**
     * Upgrade database.
     * @param db The application's database to be upgraded.
     * @param oldVersion The old version number.
     * @param newVersion The new version number.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop old tables.
        db.execSQL("DROP TABLE IF EXISTS " + STOP_TABLE);

        // Recreate tables.
        onCreate(db);
    }

    /**
     * Add stop to database.
     * @param stop The stop to add.
     */
    public void addStop(BusStop stop) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(STOPS_ID, stop.getStopID());
        values.put(STOPS_STREET, stop.getStreetName());
        values.put(STOPS_COORDINATES, stop.getCoordinates());

        db.insert(STOP_TABLE, null, values);
        db.close();
    }

    /**
     * Get a bus stop.
     * @param id The ID of the stop.
     * @return The bus stop matching the ID.
     */
    public BusStop getStop(int id) {
        BusStop stop = null;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(STOP_TABLE, new String [] {STOPS_ID, STOPS_STREET,
                STOPS_COORDINATES}, STOPS_ID + "=?", new String[] {String.valueOf(id)},
                null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            stop = new BusStop(cursor.getString(2), cursor.getString(1), cursor.getString(0));
        }

        return stop;
    }

    public void deleteStop(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(STOP_TABLE, STOPS_ID + " = ? ", new String[] {String.valueOf(id)});
        db.close();
    }

    /**
     * Retrieve all stops in database.
     * @return A list of all stops.
     */
    public List<BusStop> getBusStops() {
        List<BusStop> stops = new ArrayList<BusStop>();

        String selectQuery = "SELECT * FROM " + STOP_TABLE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                BusStop stop = new BusStop();

                stop.setCoordinates(cursor.getString(2));
                stop.setStreetName(cursor.getString(1));
                stop.setStopID(cursor.getString(0));

                stops.add(stop);
            } while (cursor.moveToNext());
        }

        return stops;
    }
}
