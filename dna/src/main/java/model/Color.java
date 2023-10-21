package model;

import dna.Dna;
import logger.LogEvent;
import logger.Logger;

/**
 * A class representing RGB colors to avoid awt for headless mode.
 */
public class Color {
    int red, green, blue;

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

    public Color(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    /**
     * Constructor for hex colors.
     *
     * @param hexColor A hex color string, for example {@code "#FF11AB"}
     */
    public Color(String hexColor) {
        if (hexColor.charAt(0) == '#') {
            hexColor = hexColor.substring(1);
        }

        // check if the hex color has a length of 6
        if (hexColor.length() != 6) {
            LogEvent l = new LogEvent(Logger.ERROR,
                    "Invalid hex color length. Expected length of 6.",
                    "Invalid hex color length. Expected length of 6 when parsing the following hex color string: " + hexColor + ".");
            Dna.logger.log(l);
        }

        this.red = Integer.parseInt(hexColor.substring(0, 2), 16);
        this.green = Integer.parseInt(hexColor.substring(2, 4), 16);
        this.blue = Integer.parseInt(hexColor.substring(4, 6), 16);
    }

    /**
     * Converts this Color instance to an instance of java.awt.Color.
     *
     * @return a java.awt.Color instance with the same RGB values.
     */
    public java.awt.Color toAWTColor() {
        return new java.awt.Color(this.red, this.green, this.blue);
    }
}
