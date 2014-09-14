package wnayes.campuszoneapp;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * NexTrip Departure Information
 * http://www.datafinder.org/metadata/NexTripAPI.html
 */
public class Departure implements Parcelable {
    /** Transit vehicle departure prediction is using Actual(real-time) or scheduled information */
    public boolean Actual;

    /** Relates to "block_id" in the Google transit feed data. */
    public int BlockNumber;

    /** Departure time if "Actual" field is false, Countdown in minutes if Actual is true */
    public String DepartureText;

    /** Predicted departure time	http://www.iso.org/iso/home/standards/iso8601.htm */
    public Date DepartureTime;

    /** Text describing transit vehicle destination as well as vehicle scroll (above front windshield) */
    public String Description;

    /** Gate of predicted departure if applicable */
    public String Gate;

    /** Route number for current departure */
    public String Route;

    public enum Direction {
        SOUTHBOUND(1), EASTBOUND(2), WESTBOUND(3), NORTHBOUND(4), UNKNOWN(5);

        private int value;
        private Direction(int value) {
            this.value = value;
        }

        public static Direction fromString(String value) {
            if (value.toUpperCase().equals("SOUTHBOUND"))
                return SOUTHBOUND;
            else if (value.toUpperCase().equals("EASTBOUND"))
                return EASTBOUND;
            else if (value.toUpperCase().equals("WESTBOUND"))
                return WESTBOUND;
            else if (value.toUpperCase().equals("NORTHBOUND"))
                return NORTHBOUND;

            Log.e("Departure.Direction.fromString", "Unknown Direction string value");
            return UNKNOWN;
        }

        public static Direction fromValue(int value) {
            if (value == 1)
                return SOUTHBOUND;
            else if (value == 2)
                return EASTBOUND;
            else if (value == 3)
                return WESTBOUND;
            else if (value == 4)
                return NORTHBOUND;

            Log.e("Departure.Direction.fromString", "Unknown Direction string value");
            return UNKNOWN;
        }
    }
    public Direction RouteDirection;

    /** Terminal letter of route - indicates variant of route */
    public String Terminal;

    /** Not currently in use. */
    public int VehicleHeading;

    /** Location coordinate field - decimal degrees latitude */
    public double VehicleLatitude;

    /** Location coordinate field - decimal degrees longitude */
    public double VehicleLongitude;

    public Departure(JSONObject departureInfo) throws JSONException {
        // Parse the JSON properties into their corresponding fields.
        this.Actual = departureInfo.getBoolean("Actual");
        this.BlockNumber = departureInfo.getInt("BlockNumber");
        this.DepartureText = departureInfo.getString("DepartureText");
        this.Description = departureInfo.getString("Description");
        this.Gate = departureInfo.getString("Gate");
        this.Route = departureInfo.getString("Route");
        this.RouteDirection = Direction.fromString(departureInfo.getString("RouteDirection"));
        this.Terminal = departureInfo.getString("Terminal");
        this.VehicleHeading = departureInfo.getInt("VehicleHeading");
        this.VehicleLatitude = departureInfo.getDouble("VehicleLatitude");
        this.VehicleLongitude = departureInfo.getDouble("VehicleLongitude");

        // Parse DepartureTime
        String departureTimeStr = departureInfo.getString("DepartureTime");
        String timeString = departureTimeStr.substring(departureTimeStr.indexOf("(") + 1,
                                                       departureTimeStr.indexOf(")"));
        long millis;
        int timeZoneOffSet = 0;
        if (timeString.contains("+") || timeString.contains("-")) {
            String[] timeSegments = timeString.split("[-+]");
            millis = Long.valueOf(timeSegments[0]);
//            timeZoneOffSet = Integer.valueOf(timeSegments[1]) * 36000; // (("0100" / 100) * 3600 * 1000)
//            if (timeString.contains("-"))
//                timeZoneOffSet = -timeZoneOffSet;
        }
        else {
            millis = Long.valueOf(timeString);
        }

        this.DepartureTime = new Date(millis + timeZoneOffSet);
    }

    // Parcelable implementation
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeByte((byte) (this.Actual ? 1 : 0));
        out.writeInt(this.BlockNumber);
        out.writeString(this.DepartureText);
        out.writeString(this.Description);
        out.writeString(this.Gate);
        out.writeString(this.Route);
        out.writeInt(this.RouteDirection.value);
        out.writeString(this.Terminal);
        out.writeInt(this.VehicleHeading);
        out.writeDouble(this.VehicleLatitude);
        out.writeDouble(this.VehicleLongitude);
    }

    private Departure(Parcel in) {
        this.Actual = in.readByte() != 0;
        this.BlockNumber = in.readInt();
        this.DepartureText = in.readString();
        this.Description = in.readString();
        this.Gate = in.readString();
        this.Route = in.readString();
        this.RouteDirection = Direction.fromValue(in.readInt());
        this.Terminal = in.readString();
        this.VehicleHeading = in.readInt();
        this.VehicleLatitude = in.readDouble();
        this.VehicleLongitude = in.readDouble();
    }

    public static final Parcelable.Creator<Departure> CREATOR
            = new Parcelable.Creator<Departure>() {
        public Departure createFromParcel(Parcel in) {
            return new Departure(in);
        }

        public Departure[] newArray(int size) {
            return new Departure[size];
        }
    };
}
