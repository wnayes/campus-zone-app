package wnayes.campuszoneapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/** Represents a train station */
public class LRTStation implements Parcelable {
    private final String name;
    private final String abbr;

    public LRTStation(String name, String abbreviation) {
        this.name = name;
        this.abbr = abbreviation;
    }

    public String getStationName() {
        return this.name;
    }

    public String getAbbreviation() {
        return this.abbr;
    }

    public static LRTStation getWestBankStation() {
        return new LRTStation("West Bank Station", "WEBK");
    }

    public static LRTStation getEastBankStation() {
        return new LRTStation("East Bank Station", "EABK");
    }

    public static LRTStation getStadiumVillageStation() {
        return new LRTStation("Stadium Village Station", "STVI");
    }

    public static List<LRTStation> greenLineStations = new ArrayList<>() {
        {
            add(new LRTStation("Target Field Station Platform 2", "TF2"));
            add(new LRTStation("Target Field Station Platform 2", "TF1"));
            add(new LRTStation("Warehouse District / Hennepin Ave Station", "WARE"));
            add(new LRTStation("Nicollet Mall Station", "5SNI"));
            add(new LRTStation("Government Plaza Station", "GOVT"));
            add(new LRTStation("U.S. Bank Stadium Station", "USBA"));
            add(getWestBankStation());
            add(getEastBankStation());
            add(getStadiumVillageStation());
            add(new LRTStation("Prospect Park Station", "PSPK"));
            add(new LRTStation("Westgate Station", "WGAT"));
            add(new LRTStation("Raymond Ave Station", "RAST"));
        }
    };

    // Parcelable implementation
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.name);
        out.writeString(this.abbr);
    }

    private LRTStation(Parcel in) {
        this.name = in.readString();
        this.abbr = in.readString();
    }

    public static final Parcelable.Creator<LRTStation> CREATOR
            = new Parcelable.Creator<>() {
        public LRTStation createFromParcel(Parcel in) {
            return new LRTStation(in);
        }
        public LRTStation[] newArray(int size) {
            return new LRTStation[size];
        }
    };
}
