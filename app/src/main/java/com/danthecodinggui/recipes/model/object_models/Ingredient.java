package com.danthecodinggui.recipes.model.object_models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * ViewRecipeActivity RecyclerView model for Ingredients tab
 */
public class Ingredient implements Parcelable {

    private String ingredientText;
    private int quantity;
    private String measurement;
    private int viewOrder;

    public Ingredient(String ingredientText, int quantity, String measurement) {
        this.ingredientText = ingredientText;
        this.quantity = quantity;
        this.measurement = measurement;
    }

    public Ingredient(String ingredientText, String measurement) {
        this.ingredientText = ingredientText;
        this.measurement = measurement;
    }

    protected Ingredient(Parcel parcel) {
        this.ingredientText = parcel.readString();
        this.quantity = parcel.readInt();
        this.measurement = parcel.readString();
    }

    public Ingredient(Ingredient ingredient) {
        ingredientText = ingredient.ingredientText;
        quantity = ingredient.quantity;
        measurement = ingredient.measurement;
        viewOrder = ingredient.viewOrder;
    }

    public String getIngredientText() {
        return ingredientText;
    }
    public void setIngredientText(String ingredientText) {
        this.ingredientText = ingredientText;
    }

    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getMeasurement() {
        return measurement;
    }
    public void setMeasurement(String measurement) {
        this.measurement = measurement;
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
                ob.ingredientText.equals(ingredientText) &&
                ob.quantity == quantity &&
                ob.measurement.equals(measurement);
    }

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(ingredientText);
        parcel.writeInt(quantity);
        parcel.writeString(measurement);
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
