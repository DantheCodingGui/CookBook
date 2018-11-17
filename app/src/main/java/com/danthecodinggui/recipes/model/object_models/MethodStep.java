package com.danthecodinggui.recipes.model.object_models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * ViewRecipeActivity RecyclerView model for Method tab
 */
public class MethodStep implements Parcelable {

    private String stepText;
    private int stepNumber;

    public MethodStep(String stepText, int stepNumber) {
        this.stepText = stepText;
        this.stepNumber = stepNumber;
    }

    protected MethodStep(Parcel in) {
        stepText = in.readString();
        stepNumber = in.readInt();
    }

    public String getStepText() {
        return stepText;
    }

    public void setStepText(String stepText) {
        this.stepText = stepText;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MethodStep))
            return false;

        MethodStep ob = (MethodStep) obj;
        return ob.stepNumber == stepNumber &&
                ob.stepText.equals(stepText);
    }

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(stepText);
        parcel.writeInt(stepNumber);
    }
    public static final Creator<MethodStep> CREATOR = new Creator<MethodStep>() {
        @Override
        public MethodStep createFromParcel(Parcel in) {
            return new MethodStep(in);
        }

        @Override
        public MethodStep[] newArray(int size) {
            return new MethodStep[size];
        }
    };
}
