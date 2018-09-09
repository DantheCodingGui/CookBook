package com.danthecodinggui.recipes.msc;

import android.graphics.Color;

import java.util.Random;

/**
 * Returns randomly generated colours from Google's Material Design Guidelines
 */
public class MaterialColours {

    private static final String[] colours = {
            "#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3", "#03A9F4",
            "#00BCD4", "#009688", "#4CAF50", "#8BC34A", "#C0CA33", "#FFC107", "#FF9800", "#D84315"};

    //Seeded Random-Number-Generator to select random colour
    private static Random rand = new Random(System.currentTimeMillis());

    //Private constructor to avoid instantiation
    private MaterialColours() {}

    /**
     * Generates Random material design colour
     * @return Colour
     */
    public static int nextColour() {
        return Color.parseColor(colours[rand.nextInt(colours.length)]);
    }
}
