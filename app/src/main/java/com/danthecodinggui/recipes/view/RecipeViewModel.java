package com.danthecodinggui.recipes.view;

import android.graphics.Bitmap;

/**
 * MainActivity RecyclerView model
 */
class RecipeViewModel {

    private boolean hasPhoto;
    private boolean hasExtendedInfo;

    private String title;
    private int ingredientsNo = -1;
    private int stepsNo = -1;

    private Bitmap preview;

    private int calories;
    private int timeInMins;

    RecipeViewModel(String title, Integer calories, int timeInMins, boolean hasPhoto) {
        this.title = title;
        this.calories = calories;
        this.timeInMins = timeInMins;
        this.hasPhoto = hasPhoto;

        hasExtendedInfo = true;
    }

    RecipeViewModel(String title, Integer calories, boolean hasPhoto) {
        this(title,  calories, -1, hasPhoto);
    }

    RecipeViewModel(String title, int timeInMins, boolean hasPhoto) {
        this(title, -1, timeInMins, hasPhoto);
    }

    RecipeViewModel(String title, boolean hasPhoto) {
        this(title, -1, -1, hasPhoto);

        hasExtendedInfo = false;
    }

    /**
     * States whether or not recipe record includes time and/or Calorie information
     * @return
     */
    boolean hasExtendedInfo() {
        return hasExtendedInfo;
    }

    /**
     * States whether or not recipe record includes an attached photo or the completed dish
     * @return
     */
    boolean hasPhoto() {
        return hasPhoto;
    }

    String getTitle() {
        return title;
    }
    void setTitle(String title) {
        this.title = title;
    }

    int getIngredientsNo() {
        return ingredientsNo;
    }
    void setIngredientsNo(int ingredientsNo) {
        this.ingredientsNo = ingredientsNo;
    }

    int getStepsNo() {
        return stepsNo;
    }
    void setStepsNo(int stepsNo) {
        this.stepsNo = stepsNo;
    }

    Bitmap getPreview() {
        return preview;
    }
    void setPreview(Bitmap preview) {
        this.preview = preview;
    }

    int getCalories() {
        return calories;
    }
    void setCalories(int calories) {
        this.calories = calories;
    }

    int getTimeInMins() {
        return timeInMins;
    }
    void setTimeInMins(int timeInMins) {
        this.timeInMins = timeInMins;
    }
}
