package com.danthecodinggui.recipes.msc;

/**
 * Group of helper methods related to Strings
 */
public class StringUtils {

    /**
     * Converts duration in minutes to HH:mm format
     * @param mins Number of minutes
     * @return Formatted string
     */
    public static String minsToHourMins(int mins) {
        if (mins == 0)
            return "";
        if (mins < 60) {
            if (mins > 1)
                return mins + " mins";
            else
                return mins + " min";
        }
        else {
            int remainder = mins % 60;
            int hours = mins / 60;
            return (hours) + ((hours == 1) ? " hr " : " hrs ") + ((remainder != 0) ? (mins % 60) + " mins" : "");
        }
    }
}
