package com.danthecodinggui.recipes.model;

import android.graphics.Bitmap;

/**
 * MainActivity RecyclerView model
 */
public class RecipeViewModel {

    private boolean hasPhoto;
    private boolean hasExtendedInfo;

    private String title;
    private int ingredientsNo = -1;
    private int stepsNo = -1;

    private Bitmap preview;

    private int calories;
    private int timeInMins;

    public RecipeViewModel(String title, Integer calories, int timeInMins, boolean hasPhoto) {
        this.title = title;
        this.calories = calories;
        this.timeInMins = timeInMins;
        this.hasPhoto = hasPhoto;

        hasExtendedInfo = true;
    }

    public RecipeViewModel(String title, Integer calories, boolean hasPhoto) {
        this(title,  calories, -1, hasPhoto);
    }

    public RecipeViewModel(String title, int timeInMins, boolean hasPhoto) {
        this(title, -1, timeInMins, hasPhoto);
    }

    public RecipeViewModel(String title, boolean hasPhoto) {
        this(title, -1, -1, hasPhoto);

        hasExtendedInfo = false;
    }

    /**
     * States whether or not recipe record includes time and/or Calorie information
     * @return
     */
    public boolean hasExtendedInfo() {
        return hasExtendedInfo;
    }

    /**
     * States whether or not recipe record includes an attached photo or the completed dish
     * @return
     */
    public boolean hasPhoto() {
        return hasPhoto;
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

    public Bitmap getPreview() {
        return preview;
    }
    public void setPreview(Bitmap preview) {
        this.preview = preview;
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
}
