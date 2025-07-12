package wnayes.campuszoneapp;

import android.os.Build;
import android.os.Bundle;

import java.util.ArrayList;

import androidx.fragment.app.ListFragment;

/**
 * A fragment representing a list of Departure.
 */
public class DepartureListFragment extends ListFragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_STATION = "station";
    private static final String ARG_DIRECTION = "direction";
    private static final String ARG_DEPARTURES = "departures";

    private LRTStation mStation;
    private Departure.Direction mDirection;
    private ArrayList<Departure> mDepartures;

    private DepartureArrayAdapter mAdapter;

    public static DepartureListFragment newInstance(LRTStation station, Departure.Direction direction, ArrayList<Departure> departures) {
        DepartureListFragment fragment = new DepartureListFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_STATION, station);
        args.putInt(ARG_DIRECTION, direction.getValue());
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
            mStation = getArguments().getParcelable(ARG_STATION);
            mDirection = Departure.Direction.fromValue(getArguments().getInt(ARG_DIRECTION));
            mDepartures = getArguments().getParcelableArrayList(ARG_DEPARTURES);
        }

        if (mDepartures != null) {
            this.mAdapter = new DepartureArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, mDepartures);
            setListAdapter(mAdapter);
        } else {
            this.mAdapter = new DepartureArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, new ArrayList<>());
            setListAdapter(mAdapter);
        }
    }

    public void updateDepartureList(ArrayList<Departure> departures) {
        if (departures == null)
            return;
        getArguments().putParcelableArrayList(ARG_DEPARTURES, departures);

        mAdapter.clear();
        mAdapter.addAll(departures);
    }
}
