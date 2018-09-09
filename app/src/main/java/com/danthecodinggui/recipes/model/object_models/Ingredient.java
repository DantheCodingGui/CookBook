package com.danthecodinggui.recipes.model.object_models;

/**
 * ViewRecipeActivity RecyclerView model for Ingredients tab
 */
public class Ingredient {

    private String ingredientText;
    private int viewOrder;

    public Ingredient(String ingredientText) {
        this.ingredientText = ingredientText;
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
}
