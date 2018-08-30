package com.danthecodinggui.recipes.msc;

import android.graphics.Color;

import java.util.Random;

/**
 * Returns randomly generated colours from Google's Material Design Guidelines
 */
public class MaterialColours {

//    //List of all Material Design colours
//    private static final String[] colours = {
//            "#F44336", "#FFEBEE", "#FFCDD2", "#EF9A9A", "#E57373", "#EF5350", "#F44336", "#E53935", //Reds
//            "#D32F2F", "#C62828", "#B71C1C", "#FF8A80", "#FF5252", "#FF1744", "#D50000",
//            "#E91E63", "#FCE4EC", "#F8BBD0", "#F48FB1", "#F06292", "#EC407A", "#E91E63", "#D81B60", //Pinks
//            "#C2185B", "#AD1457", "#880E4F", "#FF80AB", "#FF4081", "#F50057", "#C51162",
//            "#9C27B0", "#F3E5F5", "#E1BEE7", "#CE93D8", "#BA68C8", "#AB47BC", "#9C27B0", "#8E24AA", //Purples
//            "#7B1FA2", "#6A1B9A", "#4A148C", "#EA80FC", "#E040FB", "#D500F9", "#AA00FF",
//            "#673AB7", "#EDE7F6", "#D1C4E9", "#B39DDB", "#9575CD", "#7E57C2", "#673AB7", "#5E35B1", //Deep Purples
//            "#512DA8", "#4527A0", "#311B92", "#B388FF", "#7C4DFF", "#651FFF", "#6200EA",
//            "", "", "", "", "", "", "", "", //Indigos
//            "", };

        private static final String[] colours = {
                "#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3", "#03A9F4",
                "#00BCD4", "#009688", "#4CAF50", "#8BC34A", "#C0CA33", "#FFC107", "#FF9800", "#D84315"};

    //Seeded Random-Number-Generator to select random colour
    private static Random rand = new Random(System.currentTimeMillis());

    private MaterialColours() {}

    /**
     * Generates Random material design colour
     * @return Colour
     */
    public static int nextColour() {
        return Color.parseColor(colours[rand.nextInt(colours.length)]);
    }
}
