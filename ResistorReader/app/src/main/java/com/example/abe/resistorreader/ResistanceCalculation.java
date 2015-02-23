package com.example.abe.resistorreader;

/**
 * Created by Jacob on 2/17/2015.
 */
public class ResistanceCalculation {

    public static void main(String args[]) {
        ColorAssign(white, yellow, gray, none);

        System.out.println("Resistance is: " + Float.toString(resistance));
        System.out.println("Tolerance is: " + "+/- " + Float.toString(tolerancePercent) + "%");
    }

    //Arrays for the different values of resistance and tolerance
    private static int black = 0, brown = 1, red = 2, orange = 3, yellow = 4, green = 5,
            blue = 6, violet = 7, gray = 8, white = 9, gold = 10, silver = 11, none = 12;

    private static int[] colors = { black, brown, red, orange, yellow, green,
            blue, violet, gray, white, gold, silver, none};

    private static float[][] colorValues = { {0, 1},
            {1, 10}, {2, 100}, {3, 1000},
            {4, 10000}, {5, 100000}, {6, 1000000},
            {7, 10000000}, {8, 100000000}, {9, 1000000000},
    };

    //Floats for the final values
    public static float resistance = 0, tolerance = 0, tolerancePercent = 0;
    public static float upperRange = 0, lowerRange = 0;

    private static float[] toleranceValue = {0, 1, 2, 0, 5, 0.5f, 0.25f, 0.1f, 10, 0, 5, 10, 20};

    public static void ColorAssign(int first, int second, int third, int fourth) {

        //If the first band is not gold or silver then it does it in the regular order
        if ( first != gold && first != silver && first != none) {
            //Each for loop checks for every color individually to determine it
            for (int i = 0; i < colors.length; i++) {
                if (first == colors[i])
                    resistance += colorValues[i][0]; }
            //The reason for multiple for loops is to ensure it outputs in the correct order
            for (int i = 0; i < colors.length; i++) {
                if (second == colors[i])
                    resistance += colorValues[i][0] / 10; }
            for (int i = 0; i < colors.length; i++) {
                if (third == colors[i])
                    resistance *= colorValues[i][1]; }
            for (int i = 0; i < colors.length; i++) {
                if (fourth == colors[i] && colors[i] != 0)
                    tolerance += toleranceValue[i]; }
        }
        else {
            //Remnants from an earlier attempt to change order that I forgot to remove
            int firstTemp = fourth, secondTemp = third, thirdTemp = second, fourthTemp = first;

            for (int i = 0; i < colors.length; i++) {
                if (firstTemp == colors[i])
                    resistance += colorValues[i][0]; }
            for (int i = 0; i < colors.length; i++) {
                if (secondTemp == colors[i])
                    resistance += colorValues[i][0] / 10; }
            for (int i = 0; i < colors.length; i++) {
                if (thirdTemp == colors[i])
                    resistance *= colorValues[i][1]; }
            for (int i = 0; i < colors.length; i++) {
                if (fourthTemp == colors[i] && colors[i] != 0)
                    tolerance += toleranceValue[i]; }
        }

        //Final calculations
        tolerancePercent += tolerance;
        tolerance = tolerance / 100;
        float toleranceRange = resistance * tolerance;
        upperRange = resistance + toleranceRange;
        lowerRange = resistance - toleranceRange;

    }

}
