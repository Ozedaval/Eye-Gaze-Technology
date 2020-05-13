package com.pwc.explore.eyegaze.opencvblob;


import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.PointerIcon;

public class EyeGazeCursor extends Object implements Parcelable {

    protected EyeGazeCursor(Parcel in) {
    }

    public static final Creator<EyeGazeCursor> CREATOR = new Creator<EyeGazeCursor>() {
        @Override
        public EyeGazeCursor createFromParcel(Parcel in) {
            return new EyeGazeCursor(in);
        }

        @Override
        public EyeGazeCursor[] newArray(int size) {
            return new EyeGazeCursor[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {

    }
    /*TODO (Will contain data on which View object the user has selected in a lifecycle conscious way  ) */

    // intent.putExtra("cursor", new EyeGazeCursor(x,y));

    // main activity -> Bundle data = getIntent().getExtras();
    // EyeGazeCursor cursor = (EyeGazeCursor) data.getParcelable("cursor");

}
