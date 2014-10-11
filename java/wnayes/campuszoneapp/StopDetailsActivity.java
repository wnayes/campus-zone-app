package wnayes.campuszoneapp;

import java.util.ArrayList;
import java.util.Locale;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import wnayes.campuszoneapp.Departure.Direction;


public class StopDetailsActivity extends ActionBarActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections.
     */
    DepartureListPagerAdapter mSectionsPagerAdapter;

    /** The {@link ViewPager} that will host the section contents. */
    ViewPager mViewPager;

    LRTStation station;
    ArrayList<Departure> westboundDepartures;
    ArrayList<Departure> eastboundDepartures;
    Direction startingDirection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_details);

        // Read station information from intent.
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            this.station = intent.getParcelableExtra("LRTStation");
            this.startingDirection =
                 Direction.fromValue(intent.getIntExtra("StartingDirection", Direction.WESTBOUND.getValue()));
            if (intent.hasExtra("WestboundDepartures") && intent.hasExtra("EastboundDepartures")) {
                this.westboundDepartures = intent.getParcelableArrayListExtra("WestboundDepartures");
                this.eastboundDepartures = intent.getParcelableArrayListExtra("EastboundDepartures");
            } else {
                new refreshStationTimeAPICaller().execute(this.station);
            }

        } else {
            this.startingDirection = Direction.fromValue(savedInstanceState.getInt("StartingDirection"));
            this.station = savedInstanceState.getParcelable("LRTStation");
            westboundDepartures = savedInstanceState.getParcelableArrayList("WestboundDepartures");
            eastboundDepartures = savedInstanceState.getParcelableArrayList("EastboundDepartures");
        }

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setTitle(this.station.getStationName() + " Departures");

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new DepartureListPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager)findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(i))
                                               .setTabListener(this));
        }

        // Switch to eastbound tab based on the stop number.
        if (this.startingDirection == Direction.EASTBOUND)
            mViewPager.setCurrentItem(1);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt("StartingDirection", this.startingDirection.getValue());
        savedInstanceState.putParcelable("LRTStation", this.station);
        savedInstanceState.putParcelableArrayList("WestboundDepartures", westboundDepartures);
        savedInstanceState.putParcelableArrayList("EastboundDepartures", eastboundDepartures);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stop_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Respond to the action bar's Up/Home button
        //   if (id == android.R.id.home) {
        //       NavUtils.navigateUpFromSameTask(this);
        //       return true;
        //   }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}

    /** A {@link FragmentPagerAdapter} that returns a fragment corresponding to a direction. */
    public class DepartureListPagerAdapter extends FragmentPagerAdapter {

        public DepartureListPagerAdapter(FragmentManager fm) {
            super(fm);
            map = new SparseArray<DepartureListFragment>(2);
        }

        private SparseArray<DepartureListFragment> map;

        public DepartureListFragment getFragment(int position) {
            return map.get(position);
        }

        @Override
        public Fragment getItem(int position) {
            DepartureListFragment newFragment;
            if (position == 0)
                newFragment = DepartureListFragment.newInstance(station, Direction.WESTBOUND, westboundDepartures);
            else
                newFragment = DepartureListFragment.newInstance(station, Direction.EASTBOUND, eastboundDepartures);
            map.put(position, newFragment);
            return newFragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            map.remove(position);
            super.destroyItem(container, position, object);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.westbound).toUpperCase(l);
                case 1:
                    return getString(R.string.eastbound).toUpperCase(l);
            }
            return null;
        }
    }

    private class refreshStationTimeAPICaller extends AsyncTask<LRTStation, Void, ArrayList<ArrayList<Departure>>> {
        protected void onPreExecute() {

        }

        protected ArrayList<ArrayList<Departure>> doInBackground(LRTStation... stations) {
            ArrayList<ArrayList<Departure>> departures = new ArrayList<ArrayList<Departure>>(2);
            try {
                departures.add(NexTripAPI.getDepartures(stations[0], Departure.Direction.WESTBOUND));
                departures.add(NexTripAPI.getDepartures(stations[0], Departure.Direction.EASTBOUND));
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "NexTripAPI threw!");
                e.printStackTrace();
            }

            return departures;
        }

        protected void onPostExecute(ArrayList<ArrayList<Departure>> departures) {
            if (departures.size() != 2 || departures.get(0) == null || departures.get(1) == null) {
                Log.e(this.getClass().getSimpleName(), "Could not download W/E departures");
                Toast.makeText(getApplicationContext(), "Could not download station info. Check internet?",
                               Toast.LENGTH_SHORT).show();
                return;
            }

            westboundDepartures = departures.get(0);
            eastboundDepartures = departures.get(1);

            DepartureListPagerAdapter adapter = (DepartureListPagerAdapter)mViewPager.getAdapter();
            adapter.getFragment(0).updateDepartureList(westboundDepartures);
            adapter.getFragment(1).updateDepartureList(eastboundDepartures);
        }
    }
}
