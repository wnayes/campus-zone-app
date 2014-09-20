package wnayes.campuszoneapp;

import android.os.Bundle;
import android.support.v4.app.ListFragment;

import java.util.ArrayList;

/**
 * A fragment representing a list of Departure.
 */
public class DepartureListFragment extends ListFragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_STOPID = "stopId";
    private static final String ARG_DEPARTURES = "departures";

    private int mStopId;
    private ArrayList<Departure> mDepartures;

    public static DepartureListFragment newInstance(int stopId, ArrayList<Departure> departures) {
        DepartureListFragment fragment = new DepartureListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_STOPID, stopId);
        args.putParcelableArrayList(ARG_DEPARTURES, departures);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DepartureListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mStopId = getArguments().getInt(ARG_STOPID);
            mDepartures = getArguments().getParcelableArrayList(ARG_DEPARTURES);
        }

        setListAdapter(new DepartureArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, mDepartures));
    }
}
