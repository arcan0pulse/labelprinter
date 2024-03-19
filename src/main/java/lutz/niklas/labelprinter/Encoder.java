package lutz.niklas.labelprinter;

public class Encoder {

    /**
     * Encodes a number to the specifications given by the customer.
     * @param number the number to be encoded, represented as a String
     * @return the same number, but with its pre- and suffix
     */
    public static String encode(String number) {

        /* Removed to hide proprietary encoding algorithm */
        return number;
    }

    /**
     * Helper method that converts from centimeters to pixels, assuming 72 PPI resolution
     * @param cm real distance that has to be converted to pixels
     * @return amount of pixels equivalent to the argument
     */
    static double fromCMtoPPI(double cm) {
        return toPPI(cm / 2.54);
    }

    /**
     * Helper method that converts from inches to pixels, assuming 72 PPI resolution
     * @param inch real distance that has to be converted to pixels
     * @return amount of pixels equivalent to the argument
     */
    static double toPPI(double inch) {
        return inch * 72;
    }

}
