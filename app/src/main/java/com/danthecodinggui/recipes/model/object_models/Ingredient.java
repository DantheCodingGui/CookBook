package com.danthecodinggui.recipes.model.object_models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * ViewRecipeActivity RecyclerView model for Ingredients tab
 */
public class Ingredient implements Parcelable {

    private String ingredientText;
    private int viewOrder;

    public Ingredient(String ingredientText) {
        this.ingredientText = ingredientText;
    }

    public Ingredient(Parcel parcel) {
        this.ingredientText = parcel.readString();
    }

    public String getIngredientText() {
        return ingredientText;
    }

    public void setIngredientText(String ingredientText) {
        this.ingredientText = ingredientText;
    }

    public int getViewOrder() {
        return viewOrder;
    }

    public void setViewOrder(int viewOrder) {
        this.viewOrder = viewOrder;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Ingredient))
            return false;

        Ingredient ob = (Ingredient) obj;
        return ob.getViewOrder() == viewOrder &&
                ob.ingredientText.equals(ingredientText);
    }

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(ingredientText);
    }
    public static final Creator<Ingredient> CREATOR = new Creator<Ingredient>() {
        @Override
        public Ingredient createFromParcel(Parcel in) {
            return new Ingredient(in);
        }

        @Override
        public Ingredient[] newArray(int size) {
            return new Ingredient[size];
        }
    };
}
