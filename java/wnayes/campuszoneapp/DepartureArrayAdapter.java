package wnayes.campuszoneapp;


import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DepartureArrayAdapter extends ArrayAdapter<Departure> {
    private ArrayList<Departure> departures;

    public DepartureArrayAdapter(Context context, int textViewResourceId, ArrayList<Departure> objects) {
        super(context, textViewResourceId, objects);
        this.departures = objects;
    }

    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.departure_list_item, null);
        }

        Departure departure = departures.get(position);
        if (departure == null)
            return view;

        ((TextView)view.findViewById(R.id.departure_time)).setText(departure.getFormattedDepartureText());
        ((TextView)view.findViewById(R.id.departure_time_ampm))
                       .setText(new SimpleDateFormat("a").format(departure.DepartureTime));

        return view;
    }
}
