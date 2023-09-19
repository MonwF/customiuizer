package name.monwf.customiuizer.utils;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

public class SoundData implements Parcelable {
	public final String caller;
	public final String uid;
	public final String type;
	public final long time;

	public static SoundData fromPref(String pref) {
		String[] dataArr = pref.split("\\|");
		return new SoundData(dataArr[0], dataArr[1], dataArr[2]);
	}

	public SoundData(String caller, String type, String uid) {
		this.caller = caller;
		this.uid = uid;
		this.type = type;
		this.time = Calendar.getInstance().getTime().getTime();
	}

	public SoundData(Parcel in) {
		caller = in.readString();
		uid = in.readString();
		type = in.readString();
		time = in.readLong();
	}

	public static final Creator<SoundData> CREATOR = new Creator<SoundData>() {
		@Override
		public SoundData createFromParcel(Parcel in) {
			return new SoundData(in);
		}

		@Override
		public SoundData[] newArray(int size) {
			return new SoundData[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(caller);
		dest.writeString(uid);
		dest.writeString(type);
		dest.writeLong(time);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SoundData)) return false;
		SoundData data = (SoundData)obj;
		return this.caller.equals(data.caller) && this.uid.equals(data.uid) && this.type.equals(data.type);
	}

	public String toPref() {
		return caller + "|" + type + "|" + uid;
	}

	@Override
	public String toString() {
		return "SoundData{" +
			"caller='" + caller + '\'' +
			", uid='" + uid + '\'' +
			", type='" + type + '\'' +
			", time=" + time +
		'}';
	}
}
