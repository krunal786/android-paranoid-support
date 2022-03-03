package com.codebase.paranoidsupport.service.api;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ScreenDataModel implements Parcelable {
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("title")
    @Expose
    public String title;
    @SerializedName("button_title")
    @Expose
    public String buttonTitle;

    protected ScreenDataModel(Parcel in) {
        message = in.readString();
        title = in.readString();
        buttonTitle = in.readString();
    }

    public static final Creator<ScreenDataModel> CREATOR = new Creator<ScreenDataModel>() {
        @Override
        public ScreenDataModel createFromParcel(Parcel in) {
            return new ScreenDataModel(in);
        }

        @Override
        public ScreenDataModel[] newArray(int size) {
            return new ScreenDataModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(message);
        parcel.writeString(title);
        parcel.writeString(buttonTitle);
    }
}
