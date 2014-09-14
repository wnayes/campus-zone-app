package wnayes.campuszoneapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class CampusZoneActivity extends ActionBarActivity
       implements CampusZoneStopOverview.StopOverviewFragmentListener {

    private CampusZoneStopOverview stopOverviewFragment;

    /** Storage for the latest sets of departure info, accessed via stop id. */
    private HashMap<Integer, ArrayList<Departure>> departureInfo;

    // 56043 WB westbound
    // 56042 EB westbound
    // 56041 SV westbound
    // 56001 WB eastbound
    // 56002 EB eastbound
    // 56003 SV eastbound
    public static final Integer campusZoneStops[] = { 56043, 56042, 56041, 56001, 56002, 56003 };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus_zone);

        this.departureInfo = new HashMap<Integer, ArrayList<Departure>>(6);

        if (savedInstanceState == null) {
            stopOverviewFragment = new CampusZoneStopOverview();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, stopOverviewFragment)
                    .commit();

            this.refreshStopTimes();
        } else {
            // Restore the fragment's instance
            stopOverviewFragment = (CampusZoneStopOverview)getSupportFragmentManager().getFragment(savedInstanceState, "stopOverviewFragment");

            // Restore departureInfo
            ArrayList<Integer> stopIds = savedInstanceState.getIntegerArrayList("stopIds");
            for (Integer stopId : stopIds) {
                this.departureInfo.put(stopId, savedInstanceState.<Departure>getParcelableArrayList(stopId.toString()));
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // Save the fragment's instance
        getSupportFragmentManager().putFragment(savedInstanceState, "stopOverviewFragment", stopOverviewFragment);

        // Save stop information.
        Iterator it = this.departureInfo.entrySet().iterator();
        ArrayList<Integer> stopIds = new ArrayList<Integer>();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            stopIds.add((Integer)pairs.getKey());
            savedInstanceState.putParcelableArrayList(pairs.getKey().toString(), (ArrayList<Departure>)pairs.getValue());
            it.remove();
        }
        savedInstanceState.putIntegerArrayList("stopIds", stopIds);
    }


    private Menu _menu;
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu == null)
            menu = new MenuBuilder(getApplicationContext());

        menu.clear();
        getMenuInflater().inflate(R.menu.campus_zone, menu);

        // Keep a reference to the menu for later uses (refresh indicator change).
        this._menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.menu_refresh_event) {
            this.refreshStopTimes();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshStopTimes() {
        new refreshStopTimeAPICaller().execute(campusZoneStops);
    }

    public void onStopSelected(int stopId) {
        // Launch activity showing detailed stop information
        Intent intent = new Intent(this, StopDetailsActivity.class);
        startActivity(intent);
    }

    private class refreshStopTimeAPICaller extends AsyncTask<Integer, Void, ArrayList<Integer>> {
        /** Quick access to the refresh button in the actionbar. */
        MenuItem refreshItem;
        protected void onPreExecute() {
            // Establish progress UI changes.
            if (_menu != null) {
                refreshItem = _menu.findItem(R.id.menu_refresh_event);
                if (refreshItem != null)
                    MenuItemCompat.setActionView(refreshItem, R.layout.actionbar_refresh_progress);
            }
        }

        protected ArrayList<Integer> doInBackground(Integer... ids) {
            ArrayList<Integer> stopIds = new ArrayList<Integer>(ids.length);
            for (Integer stopId : ids) {
                stopIds.add(stopId);
                try {
                    departureInfo.put(stopId, NexTripAPI.getDepartures(stopId));
                } catch (Exception e) {
                    Log.e("refreshStopTimeAPICaller", "NexTripAPI threw!");
                    e.printStackTrace();
                }
            }

            return stopIds;
        }

        protected void onPostExecute(ArrayList<Integer> stopIds) {
            // Remove progress UI.
            if (refreshItem != null)
                MenuItemCompat.setActionView(refreshItem, null);
            refreshItem = null;

            // If the event can't be found, no UI refresh should occur.
            if (stopIds.size() == 0) {
                Toast.makeText(getApplicationContext(),
                        "Could not download stop info. Check internet?",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Invalidate the options menu to account for any needed visibility changes.
            if (!ActivityCompat.invalidateOptionsMenu(CampusZoneActivity.this))
                CampusZoneActivity.this.onCreateOptionsMenu(_menu);

            if (stopOverviewFragment.isAdded()) {
                for (Integer stopId : stopIds) {
                    stopOverviewFragment.updateStopTime(stopId, departureInfo.get(stopId).get(0), null);
                }
            }
        }
    }
}
