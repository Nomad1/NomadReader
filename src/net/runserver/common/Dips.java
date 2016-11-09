/**
 * 
 */
package net.runserver.common;

import android.content.res.Resources;

public class Dips {

    public static int spToPx(final int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().scaledDensity);
    }

    public static int dpToPx(final int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(final int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int screenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int screenWidthDP() {
        return pxToDp(screenWidth());
    }

    public static int screenHeightDP() {
        return pxToDp(screenHeight());
    }

    public static int screenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public static int screenMinWH() {
        return Math.min(screenHeight(), screenWidth());
    }

    public static boolean isSmallScreen() {
        // large screens are at least 640dp x 480dp
        return Dips.screenMinWH() < Dips.dpToPx(450);
    }

}
