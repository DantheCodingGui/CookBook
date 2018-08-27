package com.danthecodinggui.recipes.model;

import android.graphics.Bitmap;

import android.net.Uri;

/**
 * MainActivity RecyclerView model
 */
public class RecipeViewModel {

    private String title;
    private int ingredientsNo = -1;
    private int stepsNo = -1;

    //private Bitmap preview;

    public Uri getImagePreviewUri() {
        return imagePreviewUri;
    }

    public void setImagePreviewUri(Uri imagePreviewUri) {
        this.imagePreviewUri = imagePreviewUri;
    }

    private Uri imagePreviewUri;
    //change to URI for glide to load w/ data binding

    private int calories;
    private int timeInMins;

    public RecipeViewModel(String title, Integer calories, int timeInMins, Uri imagePreviewUri) {
        this.title = title;
        this.calories = calories;
        this.timeInMins = timeInMins;
        this.imagePreviewUri = imagePreviewUri;
    }
    public RecipeViewModel(String title, Integer calories, int timeInMins) {
        this(title,  calories, timeInMins, null);
    }

    //With Image
    public RecipeViewModel(String title, Integer calories, Uri imagePreviewUri) {
        this(title,  calories, -1, imagePreviewUri);
    }
    public RecipeViewModel(String title, int timeInMins, Uri imagePreviewUri) {
        this(title, -1, timeInMins, imagePreviewUri);
    }
    public RecipeViewModel(String title, Uri imagePreviewUri) {
        this(title, -1, -1, imagePreviewUri);
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
        return imagePreviewUri != null;
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

    /*
    public Bitmap getPreview() {
        return preview;
    }
    public void setPreview(Bitmap preview) {
        this.preview = preview;
    }
    */

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
