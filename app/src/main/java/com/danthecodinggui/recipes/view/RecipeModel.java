package com.danthecodinggui.recipes.view;

import android.graphics.Bitmap;

/**
 * Recyclerview model
 */
public class RecipeModel {

    private boolean hasFullRecipe;
    private boolean hasPhoto;

    private String title;
    private int ingredientsNo;
    private int stepsNo;

    private Bitmap preview;

    private int calories;
    private int timeInMins;

    public RecipeModel(String title, int ingredientsNo, int stepsNo) {
        hasPhoto = hasFullRecipe = false;
        this.title = title;
        this.ingredientsNo = ingredientsNo;
        this.stepsNo = stepsNo;
    }

    public RecipeModel(String title, int ingredientsNo, int stepsNo, int calories, int timeInMins) {
        hasPhoto = false;
        hasFullRecipe = true;
        this.title = title;
        this.ingredientsNo = ingredientsNo;
        this.stepsNo = stepsNo;
        this.calories = calories;
        this.timeInMins = timeInMins;
    }

    public RecipeModel(String title, int ingredientsNo, int stepsNo, Bitmap preview) {
        hasFullRecipe = false;
        hasPhoto = true;
        this.title = title;
        this.ingredientsNo = ingredientsNo;
        this.stepsNo = stepsNo;
        this.preview = preview;
    }

    public RecipeModel(String title, int ingredientsNo, int stepsNo, int calories, int timeInMins, Bitmap preview) {
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
    public boolean hasFullRecipe() {
        return hasFullRecipe;
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
