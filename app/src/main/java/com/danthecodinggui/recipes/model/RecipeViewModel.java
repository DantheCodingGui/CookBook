package com.danthecodinggui.recipes.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * MainActivity RecyclerView model
 */
public class RecipeViewModel implements Parcelable {

    //Primary key in db
    private long recipePk;

    private String title;
    private int ingredientsNo = -1;
    private int stepsNo = -1;

    private String imageFilePath;

    private int calories;
    private int timeInMins;

    public RecipeViewModel(long recipePk, String title, Integer calories, int timeInMins, String imagefilePath) {
        this.recipePk = recipePk;
        this.title = title;
        this.calories = calories;
        this.timeInMins = timeInMins;
        this.imageFilePath = imagefilePath;
    }
    public RecipeViewModel(long recipePk, String title, Integer calories, int timeInMins) {
        this(recipePk, title,  calories, timeInMins, null);
    }

    //With Image
    public RecipeViewModel(long recipePk, String title, Integer calories, String imagefilePath) {
        this(recipePk, title,  calories, -1, imagefilePath);
    }
    public  RecipeViewModel(long recipePk, String title, int timeInMins, String imagefilePath) {
        this(recipePk, title, -1, timeInMins, imagefilePath);
    }
    public RecipeViewModel(long recipePk, String title, String imagefilePath) {
        this(recipePk, title, -1, -1, imagefilePath);
    }

    //Without Image
    public RecipeViewModel(long recipePk, String title, Integer calories) {
        this(recipePk, title,  calories, -1, null);
    }
    public RecipeViewModel(long recipePk, String title, int timeInMins) {
        this(recipePk, title, -1, timeInMins, null);
    }
    public RecipeViewModel(long recipePk, String title) {
        this(recipePk, title, -1, -1, null);
    }

    /**
     * States whether or not recipe record includes time and/or Calorie information
     * @return
     */
    public boolean hasExtendedInfo() {
        return calories != -1 || timeInMins != -1;
    }

    /**
     * States whether or not recipe record includes an attached photo or the completed dish
     * @return
     */
    public boolean hasPhoto() {
        return imageFilePath != null;
    }

    public long getRecipeId() {
        return recipePk;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public int getIngredientsNo() {
        return ingredientsNo;
    }
    public void setIngredientsNo(int ingredientsNo) {
        this.ingredientsNo = ingredientsNo;
    }

    public int getStepsNo() {
        return stepsNo;
    }
    public void setStepsNo(int stepsNo) {
        this.stepsNo = stepsNo;
    }

    public String getImageFilePath() {
        return imageFilePath;
    }
    public void setImageFilePath(String imageFilePath) {
        this.imageFilePath = imageFilePath;
    }

    public int getCalories() {
        return calories;
    }
    public void setCalories(int calories) {
        this.calories = calories;
    }

    public int getTimeInMins() {
        return timeInMins;
    }
    public void setTimeInMins(int timeInMins) {
        this.timeInMins = timeInMins;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(recipePk);
        parcel.writeString(title);
        parcel.writeInt(ingredientsNo);
        parcel.writeInt(stepsNo);
        parcel.writeString(imageFilePath);
        parcel.writeInt(calories);
        parcel.writeInt(timeInMins);
    }

    public RecipeViewModel(Parcel parcel) {
        this.recipePk = parcel.readLong();
        this.title = parcel.readString();
        this.ingredientsNo = parcel.readInt();
        this.stepsNo = parcel.readInt();
        this.imageFilePath = parcel.readString();
        this.calories = parcel.readInt();
        this.timeInMins = parcel.readInt();
    }

    //Generates instances of Parcelable class from a Parcel
    public static final Parcelable.Creator<RecipeViewModel> CREATOR = new Parcelable.Creator<RecipeViewModel>() {

        @Override
        public RecipeViewModel createFromParcel(Parcel parcel) {
            return new RecipeViewModel(parcel);
        }

        @Override
        public RecipeViewModel[] newArray(int size) {
            //Used when parcelable gets a list of ParcelableObjects
            return new RecipeViewModel[size];
        }
    };
}
