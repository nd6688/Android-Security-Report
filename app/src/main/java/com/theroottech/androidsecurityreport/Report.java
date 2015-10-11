package com.theroottech.androidsecurityreport;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Neel on 6/21/2015.
 */
public class Report extends Activity{
    public JSONObject applicationReport;
    public ParseApplicationReportJSON parsedAppReportData = null;
    public double appScore = 0;
    public double settingScore = 0;
    public JSONObject settingReport;

    public Report(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report);
        Intent intent = this.getIntent();
        try {
            applicationReport = new JSONObject(intent.getStringExtra("applicationReport"));
            settingReport = new JSONObject(intent.getStringExtra("settingReport"));
            appScore = Double.parseDouble(intent.getStringExtra("appScore"));
            settingScore = Double.parseDouble(intent.getStringExtra("settingScore"));
            Log.i("Report class", "applicationReport: " + applicationReport);
            parsedAppReportData = new ParseApplicationReportJSON(applicationReport);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void displaySettingReport(View view) throws JSONException {
        LinearLayout ll = (LinearLayout)findViewById(R.id.settingsReportLinearLayout);
        if(ll.getVisibility()==View.VISIBLE){
            ll.setVisibility(View.INVISIBLE);
            ll.removeAllViews();
        }
        else {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.removeAllViews();
            ll.setVisibility(View.VISIBLE);
            if (settingReport != null) {
                Iterator<String> iter = settingReport.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    String value = (String) settingReport.get(key);
                    TextView tv = new TextView(this);
                    tv.setTextColor(Color.parseColor("#FFFFFF"));
                    tv.setText(" " + key + "\n " + value);
                    tv.setBackgroundColor(Color.parseColor("#2C3539"));
                    tv.setLayoutParams(lp);
                    ll.addView(tv);
                }
            }
            TextView tv = new TextView(this);
            tv.setTextColor(Color.parseColor("#FFFFFF"));
            tv.setText(" Total Score: " + settingScore);
            tv.setTextSize(20);
            tv.setBackgroundColor(Color.parseColor("#2C3539"));
            tv.setLayoutParams(lp);
            ll.addView(tv);
        }
    }


    public void displayApplicationReport(View view) throws JSONException {
        LinearLayout ll = (LinearLayout)findViewById(R.id.applicationsReportLinearLayout);
        if(ll.getVisibility()==View.VISIBLE){
            ll.setVisibility(View.INVISIBLE);
            ll.removeAllViews();
        }
        else {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.removeAllViews();
            ll.setVisibility(View.VISIBLE);
            String possiblyMalicious = "";
            HashMap<String, String> appNamesAndScore = parsedAppReportData.getAppNamesAndScore();
            Iterator it = appNamesAndScore.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                String name = (String) pair.getKey();
                String score = (String) pair.getValue();
                if (name.equals("Possible Malicious Apps: ")) {
                    possiblyMalicious = name + score;
                } else {
                    TextView tv = new TextView(this);
                    if(score.charAt(0)=='-'){
                        tv.setTextColor(Color.parseColor("#B24747"));
                        score = score.substring(1);
                        tv.setText(" Application Name: " + name + "\n --> Score: " + score);
                    }else if(score.equals("0")) {
                        tv.setTextColor(Color.parseColor("#B24747"));
                        tv.setText(" Application Name: " + name + "\n --> Score: " + score);
                    }else
                    {
                        tv.setTextColor(Color.parseColor("#FFFFFF"));
                        tv.setText(" Application Name: " + name + "\n --> Score: " + score);
                    }
                    tv.setBackgroundColor(Color.parseColor("#2C3539"));
                    tv.setLayoutParams(lp);
                    ll.addView(tv);
                }
            }
            TextView tv1 = new TextView(this);
            tv1.setTextColor(Color.parseColor("#FFFFFF"));
            tv1.setText(possiblyMalicious);
            tv1.setTextSize(18);
            tv1.setBackgroundColor(Color.parseColor("#2C3539"));
            tv1.setLayoutParams(lp);
            ll.addView(tv1);
            TextView tv = new TextView(this);
            tv.setTextColor(Color.parseColor("#FFFFFF"));
            tv.setText(" Total Score: " + appScore);
            tv.setTextSize(20);
            tv.setBackgroundColor(Color.parseColor("#2C3539"));
            tv.setLayoutParams(lp);
            ll.addView(tv);
        }
    }
}
