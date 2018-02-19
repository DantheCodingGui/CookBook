package com.danthecodinggui.recipes.view;

import android.graphics.Bitmap;

/**
 * MainActivity RecylerView model
 */
class RecipeModel {

    private boolean hasFullRecipe;
    private boolean hasPhoto;

    private String title;
    private int ingredientsNo;
    private int stepsNo;

    private Bitmap preview;

    private int calories;
    private int timeInMins;

    RecipeModel(String title, int ingredientsNo, int stepsNo) {
        hasPhoto = hasFullRecipe = false;
        this.title = title;
        this.ingredientsNo = ingredientsNo;
        this.stepsNo = stepsNo;
    }

    RecipeModel(String title, int ingredientsNo, int stepsNo, int calories, int timeInMins) {
        hasPhoto = false;
        hasFullRecipe = true;
        this.title = title;
        this.ingredientsNo = ingredientsNo;
        this.stepsNo = stepsNo;
        this.calories = calories;
        this.timeInMins = timeInMins;
    }

    RecipeModel(String title, int ingredientsNo, int stepsNo, Bitmap preview) {
        hasFullRecipe = false;
        hasPhoto = true;
        this.title = title;
        this.ingredientsNo = ingredientsNo;
        this.stepsNo = stepsNo;
        this.preview = preview;
    }

    RecipeModel(String title, int ingredientsNo, int stepsNo, int calories, int timeInMins, Bitmap preview) {
        hasPhoto = true;
        hasFullRecipe = true;
        this.title = title;
        this.ingredientsNo = ingredientsNo;
        this.stepsNo = stepsNo;
        this.calories = calories;
        this.timeInMins = timeInMins;
        this.preview = preview;
    }

    /**
     * States whether or not recipe record includes time and/or Calorie information
     * @return
     */
    boolean hasFullRecipe() {
        return hasFullRecipe;
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
