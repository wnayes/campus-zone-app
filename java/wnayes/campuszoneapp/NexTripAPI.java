package wnayes.campuszoneapp;

import android.util.Log;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;

import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class NexTripAPI {
    public static ArrayList<Departure> getDepartures(LRTStation station, Departure.Direction direction) throws SocketTimeoutException, ConnectTimeoutException {
        String url = String.format("http://svc.metrotransit.org/NexTrip/902/%d/%s?format=json",
                     direction.getValue(), station.getAbbreviation());
        return getDepartures(url);
    }

    public static ArrayList<Departure> getDepartures(int stopId) throws SocketTimeoutException, ConnectTimeoutException {
        String url = String.format("http://svc.metrotransit.org/NexTrip/%d?format=json", stopId);
        return getDepartures(url);
    }

    private static ArrayList<Departure> getDepartures(String url) throws SocketTimeoutException, ConnectTimeoutException {
        RestClient client = new RestClient(url);

        // Prevent the search from stalling indefinitely.
        client.SetTimeout(10000, 10000);

        try {
            client.Execute(RestClient.RequestMethod.GET);
        } catch (SocketTimeoutException sce) {
            Log.e("NexTrip getDepartures", "Socket connection timeout.");
            throw sce;
        } catch (ConnectTimeoutException cte) {
            Log.e("NexTrip getDepartures", "HTTP Connection timeout.");
            throw cte;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Log.d("NexTrip getDepartures", url + ": " + ((client.getResponse() == null) ? "" : client.getResponse()));

        // Parse the stop times and grab the latest one.
        try {
            JSONArray stopsArray = new JSONArray(client.getResponse());
            return Departure.parseList(stopsArray);
        } catch (JSONException e) {
            Log.e(NexTripAPI.class.toString(), "Error parsing stop array");
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.e(NexTripAPI.class.toString(), "Other parsing error - likely networking issue.");
            e.printStackTrace();
        }

        return null;
    }
}
