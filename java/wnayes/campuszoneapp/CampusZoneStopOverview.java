package wnayes.campuszoneapp;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 */
public class CampusZoneStopOverview extends Fragment {

    private StopOverviewFragmentListener mListener;

    private HashMap<Integer, Departure> mStopTimes;

    public CampusZoneStopOverview() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mStopTimes = new HashMap<Integer, Departure>(6);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View infLayout = inflater.inflate(R.layout.fragment_campus_zone_stop_overview, container, false);

        if (savedInstanceState != null) {
            // Load departure information from the bundle, if saved.
            for (Integer stopId : CampusZoneActivity.campusZoneStops) {
                Departure departure = savedInstanceState.getParcelable(Integer.toString(stopId));
                if (departure != null) {
                    this.mStopTimes.put(stopId, departure);
                    this.updateStopTime(stopId, departure, infLayout);
                }
            }
        }

        // Click handlers on stop circles
//        infLayout.findViewById(R.id.westBankStopView).setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                if (mListener != null)
//                    mListener.onStopSelected(56043);
//            }
//        });

        return infLayout;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the label states of the stops.
        Iterator it = mStopTimes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            outState.putParcelable(pairs.getKey().toString(), (Departure)pairs.getValue());
            it.remove();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (StopOverviewFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement StopOverviewFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void updateStopTime(int stopId, Departure departure, View view) {
        if (view == null)
            view = this.getView();

        String departureTime = new SimpleDateFormat("h:mm").format(departure.DepartureTime);
        this.mStopTimes.put(stopId, departure);
        int resID = getResources().getIdentifier("stopTime" + Integer.toString(stopId), "id", this.getActivity().getPackageName());
        TextView stopLabel = (TextView)(view.findViewById(resID));
        stopLabel.setText(departureTime);

        // Use either "Arriving at" or "Arriving in" depending on the Departure format.
        resID = getResources().getIdentifier("stopDesc" + Integer.toString(stopId), "id", this.getActivity().getPackageName());
        ((TextView)(view.findViewById(resID))).setText(
            departure.Actual ? R.string.predicted : R.string.scheduled);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface StopOverviewFragmentListener {
        public void onStopSelected(int stopId);
    }

    public static class StopCircleView extends View {
        String[] stopName;

        public StopCircleView(Context context) {
            super(context);
        }

        public StopCircleView(Context context, AttributeSet attrs) {
            super(context, attrs);
            int[] set = {
                android.R.attr.text
            };
            TypedArray a = context.obtainStyledAttributes(attrs, set);
            stopName = a.getText(0).toString().split(" ");
            a.recycle();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int x = getWidth();
            int y = getHeight();
            int radius = Math.min(x, y) / 2;
            Paint paint = new Paint();

            // Draw the circles
            paint.setColor(getResources().getColor(R.color.lineColorGreen));
            canvas.drawCircle(x / 2, y / 2, radius, paint);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(x / 2, y / 2, radius * 0.8f, paint);

            // Draw the stop name TODO: more than 2 lines of stop name?
            paint.setColor(Color.BLACK);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setAntiAlias(true);
            int scaledSize = getResources().getDimensionPixelSize(R.dimen.stop_circle_fontsize);
            paint.setTextSize(scaledSize);
            canvas.drawText(stopName[0], x / 2, y / 2 - paint.descent(), paint);
            canvas.drawText(stopName[1], x / 2, y / 2 - paint.ascent(), paint);
        }
    }
}
