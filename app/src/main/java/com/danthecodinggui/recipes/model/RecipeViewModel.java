package com.danthecodinggui.recipes.model;

import android.net.Uri;

/**
 * MainActivity RecyclerView model
 */
public class RecipeViewModel {

    private String title;
    private int ingredientsNo = -1;
    private int stepsNo = -1;

    private String imageFilePath;

    private int calories;
    private int timeInMins;

    public RecipeViewModel(String title, Integer calories, int timeInMins, String imagefilePath) {
        this.title = title;
        this.calories = calories;
        this.timeInMins = timeInMins;
        this.imageFilePath = imagefilePath;
    }
    public RecipeViewModel(String title, Integer calories, int timeInMins) {
        this(title,  calories, timeInMins, null);
    }

    //With Image
    public RecipeViewModel(String title, Integer calories, String imagefilePath) {
        this(title,  calories, -1, imagefilePath);
    }
    public RecipeViewModel(String title, int timeInMins, String imagefilePath) {
        this(title, -1, timeInMins, imagefilePath);
    }
    public RecipeViewModel(String title, String imagefilePath) {
        this(title, -1, -1, imagefilePath);
    }

    //Without Image
    public RecipeViewModel(String title, Integer calories) {
        this(title,  calories, -1, null);
    }
    public RecipeViewModel(String title, int timeInMins) {
        this(title, -1, timeInMins, null);
    }
    public RecipeViewModel(String title) {
        this(title, -1, -1, null);
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
}
