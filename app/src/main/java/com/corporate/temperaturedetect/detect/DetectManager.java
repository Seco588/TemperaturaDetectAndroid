package com.corporate.temperaturedetect.detect;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import java.util.Arrays;

public class DetectManager {
    private static final String TAG = "Logic";

    public DetectManager() {
    }

    /**
     * Get color from temperature detect
     *
     * @param temperatureDetect
     * @param Temperature_Min
     * @param Temperature_Max
     * @return
     */
    public static Scalar getColoFromTemperature(float temperatureDetect, double Temperature_Min, double Temperature_Max) {
        if (temperatureDetect >= Temperature_Min && temperatureDetect <= Temperature_Max) {
            return new Scalar(0, 255, 0, 1);//Green
        }
        if (temperatureDetect > Temperature_Max) {
            return new Scalar(255, 0, 0, 1);//Red
        }
        if (temperatureDetect < Temperature_Min) {
            return new Scalar(0, 0, 255, 1);//Blue
        }
        return new Scalar(129, 129, 129, 0.4);//Gray
    }

    /**
     * Create double array , in the first array in position [0] get min , [1]get medium ,[2]get max , in second array get all temperature
     *
     * @param temperature list of temperature
     * @return Float [][]
     */
    public static Float[][] temperatureThresholds(Float[] temperature) {
        Float[] save = temperature;
        int n = temperature.length;
        float temp = 0f;

        //bubbelsort
        for (int index = 0; index < n; index++) {
            for (int j = 1; j < (n - index); j++) {
                if (temperature[j - 1] > temperature[j]) {
                    //scambia elementi
                    temp = temperature[j - 1];
                    temperature[j - 1] = temperature[j];
                    temperature[j] = temp;
                }
            }
        }


        float aFloat = 0f;
        for (float x : temperature) {
            aFloat += x;
        }

        //stat temperature
        float min = temperature[0];
        float mid = aFloat / n;//temperature[(temperature.length - 1) / 2];
        float max = temperature[temperature.length - 1];


        //double array
        //Float[][] response = new Float[3][n];
        Float[][] response = new Float[n + 3][1];

        //up
        response[0][0] = min;
        response[1][0] = mid;
        response[2][0] = max;
        Log.d(TAG, "Float[][] : " + response.length + "\n" + Arrays.deepToString(response) + "\n temp lenght : " + temperature.length);
        //up
        for (int i = 0; i < n ; i++) {
            Log.i(TAG, "temperature[" + i + "] = " + save[i]);
            response[i+3][0] = save[i];
        }
        Log.d(TAG, "Float[][] LAST ! : " + response.length + "\n" + Arrays.deepToString(response));
        return response;

    }

    /**
     * Convert String temperature to float number
     *
     * @param temperature String temperature
     * @return
     */
    public static float convertTemperature(String temperature) {
        String[] strArray = temperature.split("\\s");
        return Float.parseFloat(strArray[strArray.length - 1].substring(0, 4));
    }

    /**
     * Response temperature set in configurator file
     *
     * @param temperatureDetect
     * @param Temperature_Min
     * @param Temperature_Max
     * @param Temperature_inside_range
     * @param Temperature_Max_exceeded
     * @param Temperature_below_limit
     * @param Error_temperature
     * @return
     */
    public static String temperatureResponse(float temperatureDetect,
                                             double Temperature_Min,
                                             double Temperature_threshold,
                                             double Temperature_Max,
                                             String Temperature_inside_range,
                                             String Temperature_inside_threshold,
                                             String Temperature_Max_exceeded,
                                             String Temperature_below_limit,
                                             String Error_temperature) {
        if (temperatureDetect >= Temperature_Min && temperatureDetect <= Temperature_Max) {
            double v = Temperature_Max - Temperature_threshold;
            if (temperatureDetect >= v) {
                return Temperature_inside_threshold;
            }
            return Temperature_inside_range;
        }
        if (temperatureDetect > Temperature_Max) {
            return Temperature_Max_exceeded;
        }
        if (temperatureDetect < Temperature_Min) {
            return Temperature_below_limit;
        }
        return Error_temperature;
    }

    /**
     *
     * @param temperatureDetect
     * @param Temperature_Min
     * @param Temperature_threshold
     * @param Temperature_Max
     * @return int 0 if temperature detect is inside range max inside threshold . 1 if inside range max ,2 if is > temp max ,3 if < tmep min , 4 error temp
     */
    public static int levelTemp(float temperatureDetect,
                                             double Temperature_Min,
                                             double Temperature_threshold,
                                             double Temperature_Max) {
        if (temperatureDetect >= Temperature_Min && temperatureDetect <= Temperature_Max) {
            double v = Temperature_Max - Temperature_threshold;
            if (temperatureDetect >= v) {
                return 0;
            }
            return 1;
        }
        if (temperatureDetect > Temperature_Max) {
            return 2;
        }
        if (temperatureDetect < Temperature_Min) {
            return 3;
        }
        return 4;
    }

    /**
     * Create roi in center Mat with specific height and width
     *
     * @param rgba   Mat
     * @param width  float
     * @param height float
     * @return Rect
     */
    public static Rect createRoi(Mat rgba, float width, float height) {
        return new Rect(new Point(rgba.width() / 2f - (width / 2f), rgba.height() / 2f - (height / 2f)), new Point(rgba.width() / 2f + (width / 2f), rgba.height() / 2f + (height / 2f)));
    }

    /**
     * Create subroi from roi
     *
     * @param roi      Rect
     * @param offset_x Floar
     * @param offset_y Float
     * @return Rect
     */
    public static Rect createSubroiFromRoi(Rect roi, float offset_x, float offset_y) {
        return new Rect(new Point(roi.tl().x + offset_x, roi.tl().y + offset_y), new Point(roi.br().x - offset_x, roi.br().y - offset_y));
    }

    /**
     * Check if ract face is between rect ext and rect int
     *
     * @param r_ext ROI external
     * @param r_int ROI inside
     * @param face  Rect of face detect
     * @return true if rect face is between range of bottom right and up left
     */
    public static boolean checkSquares(Rect r_ext, Rect r_int, Rect face) {
        if (face == null) {
            Log.e(TAG, "Face == null");
            return false;
        }

        int xEst = r_ext.x;
        int yEst = r_ext.y;
        int oxExt = r_ext.x + r_ext.width;
        int oyExt = r_ext.y + r_ext.height;

        int xInt = r_int.x;
        int yInt = r_int.y;
        int oxInt = r_int.x + r_int.width;
        int oyInt = r_int.y + r_int.height;

        int xf = face.x;
        int yf = face.y;
        int oxf = face.x + face.width;
        int oyf = face.y + face.height;
        //  ((xf > xEst && yf > yEst) && (xf < xInt && yf < yInt)) ? ((oxf < oxExt && oyf < oyExt) && (oxf > oxInt && oyf > oyInt)) : false;
        return ((xf > xEst && yf > yEst) && (xf < xInt && yf < yInt)) && ((oxf < oxExt && oyf < oyExt) && (oxf > oxInt && oyf > oyInt));
    }

    /**
     * Calculate position face on screen and return String with indications
     *
     * @param r_ext externale rect
     * @param r_int internal rect
     * @param face  rect dynamic face
     * @return String arrows
     */
    public static String arrowsDirections(Rect r_ext, Rect r_int, Rect face) {

        //directions
        String up = "⬆";
        String down = "⬇";
        String right = "➡";
        String left = "⬅";
        String center = "";//"□";
        String rDown = "↙";
        String rUp = "↖";
        String lDown = "↘";
        String lUp = "↗";

        //Coordinates face
        double xf = face.x;
        double yf = face.y;

        //rect alto sx
        Rect r_asx = new Rect(r_ext.tl(), r_int.tl());

        //midpoint x e y rect
        double due = (r_asx.x + r_asx.width) / 2f;
        double cinque = (r_asx.y + r_asx.height) / 2f;

        double uno = due / 2;
        double tre = due + uno;

        double quattro = cinque / 2;
        double sei = cinque + quattro;

        //Diagonal directions
        if (xf < uno && yf < quattro) {
            return rDown;
        }
        if (xf > tre && yf < quattro) {
            return lDown;
        }
        if (xf < uno && yf > sei) {
            return rUp;
        }
        if (xf > tre && yf > sei) {
            return lUp;
        }

        //Normal directions
        if ((xf > uno && xf < tre) && yf < quattro) {
            return down;
        }
        if ((xf > uno && xf < tre) && yf > sei) {
            return up;
        }
        if ((yf > quattro && yf < sei) && xf < uno) {
            return left;
        }
        if ((yf > quattro && yf < sei) && xf > tre) {
            return right;
        }

        //Center
        if ((xf > uno && xf < tre) && (yf > quattro && yf < sei)) {
            return center;
        }

        return "";
    }

}
