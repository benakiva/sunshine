/**
 * FILE: WeatherDataParser.java
 * AUTHOR: Dr. Isaac Ben-Akiva <isaac.ben-akiva@ubimobitech.com>
 * <p/>
 * CREATED ON: 05/06/15
 */

package com.ubimobitech.sunshine.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by benakiva on 05/06/15.
 */
public class WeatherDataParser {

    /**
     * Given a string of the form returned by the api call:
     * http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7
     * retrieve the maximum temperature for the day indicated by dayIndex
     * (Note: 0-indexed, so 0 would refer to the first day).
     */
    public static double getMaxTemperatureForDay(String weatherJsonStr, int dayIndex)
            throws JSONException {

        JSONObject reader = new JSONObject(weatherJsonStr);
        JSONArray forecastArray = reader.getJSONArray("list");
        JSONObject mElem = forecastArray.getJSONObject(dayIndex);
        JSONObject maxTemperatureObj = mElem.getJSONObject("temp");

        double maxTemp = maxTemperatureObj.getDouble("max");

        return maxTemp;
    }
}
