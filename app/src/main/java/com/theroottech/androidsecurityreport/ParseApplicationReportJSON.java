package com.theroottech.androidsecurityreport;

import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Neel on 6/21/2015.
 */
public class ParseApplicationReportJSON {
    public JSONObject report;
    JSONArray listArray = new JSONArray();

    public ParseApplicationReportJSON(){

    }
    public ParseApplicationReportJSON(JSONObject report) throws JSONException {
        this.report =  report;
        listArray = report.getJSONArray("list");
    }
    public HashMap<String, String> getAppNamesAndScore() throws JSONException {
        HashMap<String, String> appNamesAndScore = new HashMap<String, String>();
        int possiblyMalicious = 0;
        for(int i=0; i<listArray.length(); i++){
            JSONObject temp = (JSONObject) listArray.get(i);
            JSONObject tempForScore = temp.getJSONObject("result");
            JSONObject tempForPermissions = temp.getJSONObject("permissions");
            String appScore = tempForScore.getString("total");
            if(tempForPermissions.getString("clean").equals("true")){
                String appName = temp.getString("title");
                appNamesAndScore.put(appName, appScore);
            }
            else{
                if(!(appScore.equals("0"))) {
                    appScore = "-" + appScore;
                    String appName = temp.getString("title");
                    appNamesAndScore.put(appName, appScore);
                    Log.i(appName, appScore);
                }
                else{
                    String appName = temp.getString("id");
                    appNamesAndScore.put(appName, appScore);
                    Log.i(appName, appScore);
                }
                possiblyMalicious++;
            }
        }
        appNamesAndScore.put("Possible Malicious Apps: ", possiblyMalicious + "");
        return appNamesAndScore;
    }
}
