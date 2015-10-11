package com.theroottech.androidsecurityreport;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class MainActivity extends Activity {
    public double appScore = 0;
    public int settingScore = 100;
    public double appScoreForNextIntent = 0;
    public int settingScoreForNextIntent = 100;
    public int settingBonus = 0;
    String applicationReport = "";
    String settingsReport = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

    public void testDevice(View view) throws JSONException {
        TextView settingScore = (TextView) findViewById(R.id.settingScoreResult);
        TextView appScore = (TextView) findViewById(R.id.appScoreResult);
        TextView finalScore = (TextView) findViewById(R.id.finalScoreResult);

        settingScore.setText("0 / 100");
        appScore.setText("0 / 100");
        finalScore.setText("0 / 100");

        Log.i("Inside testDevice: ","calling thread to get appScore");
        new LongRunningGetIO().execute();
        Log.i("Inside testDevice: ", "calling getSettingScore");
        getSettingScore();
    }

    public void getSettingScore() throws JSONException {
        JSONObject settingReport = new JSONObject();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enable :)
                Log.i("---","Bluetooth is turned off");
            }
            else{
                Log.i("---","Bluetooth is turned on");
                settingReport.put("Bluetooth: ON", "--> Lost 5 points");
                settingScore = settingScore - 5;
                Log.i("Setting score reduced","Bluetooth enabled");
            }
        }

        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> networkList = wifi.getScanResults();

        //get current connected SSID for comparison to ScanResult
        WifiInfo wi = wifi.getConnectionInfo();
        String currentSSID = wi.getSSID();

        if (networkList != null) {
            for (ScanResult network : networkList)
            {
                //check if current connected SSID
                if (currentSSID.equals(network.SSID)){
                    //get capabilities of current connection
                    String Capabilities =  network.capabilities;
                    Log.d ("Checking WiFi", network.SSID + " capabilities : " + Capabilities);

                    if (Capabilities.contains("WPA2")) {
                        Log.i("Capability","WPA2");
                    }
                    else if (Capabilities.contains("WPA")) {
                        Log.i("Capability","WPA");
                        settingReport.put("Wi-Fi connection: WPA", "--> Lost 5 points");
                        settingScore = settingScore - 5;
                        Log.i("Setting score reduced","Wi-Fi -> WPA");
                    }
                    else if (Capabilities.contains("WEP")) {
                        Log.i("Capability","WEP");
                        settingReport.put("Wi-Fi connection: WEP", "--> Lost 10 points");
                        settingScore = settingScore - 10;
                        Log.i("Setting score reduced","Wi-Fi -> WEP");
                    }
                }
            }
        }

        //-----------------------------------Pattern Lock---------------------------------------------

        String LOCKSCREEN_UTILS = "com.android.internal.widget.LockPatternUtils";
        Log.d("LockPreference", "Checking");
        int lockProtectionLevel = 0;
        try
        {
            Class<?> lockUtilsClass = Class.forName(LOCKSCREEN_UTILS);
            // "this" is a Context, in my case an Activity
            Object lockUtils = lockUtilsClass.getConstructor(Context.class).newInstance(this);
            Method method = lockUtilsClass.getMethod("getActivePasswordQuality");
            lockProtectionLevel = (Integer)method.invoke(lockUtils);
            if(lockProtectionLevel > DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED)
            {
                Log.d("LockPreference", "Locked");
            }
            else{
                Log.d("LockPreference", "Unlocked");
                settingReport.put("Lockscreen security: Not available", "--> Lost 5 points");
                settingScore = settingScore - 5;
                Log.i("Setting score reduced","Password lock quality low");
            }
        }
        catch (Exception e)
        {
            Log.e("reflectInternalUtils", "ex:"+e);
        }
        //--------------------------- Is autolock enabled?---------------------------------------------

        boolean b = android.provider.Settings.System.getInt(
                getContentResolver(), Settings.System.LOCK_PATTERN_ENABLED, 0)==1;

        Log.i("Autolock", " -> " + b);
        if(!b) {
            settingReport.put("Auto screen lock: Not available","--> Lost 5 points");
            settingScore = settingScore - 5;
            Log.i("Setting score reduced","Autolock off");
        }
        if(lockProtectionLevel < DevicePolicyManager.PASSWORD_QUALITY_NUMERIC) {
            if (!b) {
                settingReport.put("Bonus negative: For above two concerns","--> Lost 5 points");
                settingScore = settingScore - 5;
                Log.i("Setting score reduced","Password lock quality low && Autolock off");
            }
        }

        //------------------- Is rooted? -------------------------------
        boolean b1 = isDeviceRooted();

        Log.i("Rooted?", " -> "+ b1);
        if(b1) {
            settingReport.put("Device Rooted","--> Lost 15 points");
            settingScore = settingScore - 15;
            Log.i("Setting score reduced","Device rooted");
        }

        //-------------- VPN Enable? ----------------------------

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_VPN);

        try {
            if (mWifi.isConnected()) {
                // Do whatever
                Log.i("------------VPN--------","Is conncted ");
                settingReport.put("Bonus positive: Device connected to VPN", "Gained 5 points");
                settingScore = settingScore + 5;
                Log.i("Setting score reduced","VPN Connected");
            }
            else {
                Log.i("------------VPN--------", "Naathi ");
            }
        }

        catch (NullPointerException e){
            Log.i("------------VPN--------", " No VPN ");
        }

        //--------------------------- USB Debugging -----------------------

        if(Settings.Secure.getInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 0) == 1) {
            // debugging enabled
            Log.i("-------Debug-----","Is enabled ");
            settingReport.put("USB Debugging: Turned ON", "--> Lost 5 points");
            settingScore = settingScore - 5;
            Log.i("Setting score reduced","USB Debugging on");
        } else {
            //;debugging does not enabled
            Log.i("----Debug-----","Is not enabled ");
        }
        settingsReport = settingReport.toString();
        TextView settingScoreView = (TextView) findViewById(R.id.settingScoreResult);
        TextView finalScoreView = (TextView) findViewById(R.id.finalScoreResult);
        TextView appScoreView = (TextView) findViewById(R.id.appScoreResult);
        settingScoreView.setText(settingScore + " / 100");
        if(!appScoreView.getText().equals(new String("0 / 100"))) {
            double finalScore = ((appScore + settingScore) / 2) * 100;
            int temp = (int) finalScore;
            finalScore = temp / 100.00;
            finalScoreView.setText( finalScore + " / 100");
            appScoreForNextIntent = appScore;
            settingScoreForNextIntent = settingScore;
            appScore = 0;
            settingScore = 100;
            settingBonus = 0;
            Button viewReport = (Button) findViewById(R.id.viewReport);
            viewReport.setVisibility(View.VISIBLE);
        }
    }
    //Thread to make REST call on the server
    private class LongRunningGetIO extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            HttpURLConnection httpcon = null;
            try {
                Log.i("Inside Thread","going to get list");
                JSONObject appList = getApplist();
                URL url = new URL("http://104.236.54.42:2613/getresult/");
                httpcon = (HttpURLConnection) url.openConnection();
                httpcon.setRequestProperty("Content-Type", "application/json");
                httpcon.setDoOutput(true);
                httpcon.setRequestMethod("POST");
                httpcon.setDoInput(true);
                Log.i("Inside Thread", "stuck at connect");
                httpcon.connect();
                Log.i("Inside Thread", "connected");
                OutputStream os = httpcon.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                osw.write(appList.toString());
                osw.flush();
                osw.close();
                Log.i("Inside Thread", "os closed");
                InputStream in = new BufferedInputStream(
                        httpcon.getInputStream());
                String inputStream = new String(getResponseText(in));
                in.close();
                Log.i("Inside Thread", "in closed");
                return inputStream;
            }
            catch(Exception e){
                return "";
            }
        }
        //Function to scan through the string received from buffer
        private String getResponseText(InputStream inStream) {
            String response = new Scanner(inStream).useDelimiter("\\A").next();
            Log.i("Response: ", response);
            return response;
        }
        //Function to process the response received from server
        protected void onPostExecute(String results){
            applicationReport = results;
            JSONObject returnedJSON = null;
            try {
                returnedJSON = new JSONObject(results);
                JSONObject finalScoreJSONObject = new JSONObject();
                finalScoreJSONObject = returnedJSON.getJSONObject("finalReport");
                appScore = (double) finalScoreJSONObject.get("score");
                Log.i("appScore before decimal",appScore + "");
                appScore = appScore * 100;
                int temp = (int) appScore;
                Log.i("temp",temp + "");
                appScore = temp / 100.00;
                Log.i("onPostExecut appScore", appScore + " / 100.00");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            TextView settingScoreView = (TextView) findViewById(R.id.settingScoreResult);
            TextView finalScoreView = (TextView) findViewById(R.id.finalScoreResult);
            TextView appScoreView = (TextView) findViewById(R.id.appScoreResult);
            appScoreView.setText(appScore + " / 100");
            if(!settingScoreView.getText().equals(new String("0 / 100"))) {
                double finalScore = ((appScore + settingScore) / 2) * 100;
                Log.i("finalScore before temp",finalScore + "");
                int temp = (int) finalScore;
                Log.i("finalScore after temp",temp + "");
                finalScore = temp / 100.00;
                finalScoreView.setText(finalScore + " / 100");
                appScoreForNextIntent = appScore;
                settingScoreForNextIntent = settingScore;
                appScore = 0;
                settingScore = 100;
                settingBonus = 0;
                Button viewReport = (Button) findViewById(R.id.viewReport);
                viewReport.setVisibility(View.VISIBLE);
            }
        }
    }

    //Functions to check if device is rooted or not
    public static boolean isDeviceRooted() {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3();
    }
    //Functions to check if device is rooted or not
    public static boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }
    //Functions to check if device is rooted or not
    public static boolean checkRootMethod2() {
        String[] paths = { "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                "/system/bin/failsafe/su", "/data/local/su" };
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }
    //Functions to check if device is rooted or not
    public static boolean checkRootMethod3() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[] { "/system/xbin/which", "su" });
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (in.readLine() != null) return true;
            return false;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }

    //Function to get list of applications installed on phone
    public JSONObject getApplist() throws JSONException {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);

        List<ApplicationInfo> installedApps = new ArrayList<ApplicationInfo>();
        HashMap<String, String[]> appPermissions = new HashMap<String, String[]>();

        PackageInfo packageInfo = null;
        for (ApplicationInfo app : apps) {
            //checks for flags; if flagged, check if updated system app
            if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1) {
                installedApps.add(app);
                try {
                    packageInfo = getPackageManager().getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS);
                    appPermissions.put(app.packageName, packageInfo.requestedPermissions);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                //it's a system app, not interested
            } else if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                //Discard this one
                //in this case, it should be a user-installed app
            } else {
                installedApps.add(app);
                try {
                    packageInfo = getPackageManager().getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS);
                    appPermissions.put(app.packageName, packageInfo.requestedPermissions);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        JSONObject mainList = new JSONObject();
        JSONArray mainArray = new JSONArray();
        //To iterate through the Hashmap
        Iterator it = appPermissions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String name = (String) pair.getKey();
            String[] permissions = (String[]) pair.getValue();
            Log.i("App Name", name);
            JSONObject appList = new JSONObject();
            appList.put("id", name);
            if (permissions != null) {
                JSONArray appPermJsonArray = new JSONArray();
                for (int i = 0; i < permissions.length; i++) {
                    //Log.i("\t\tApp Permission " + (i + 1), permissions[i]);
                    appPermJsonArray.put(permissions[i]);
                }
                JSONObject permissionObject = new JSONObject();
                permissionObject.put("list", appPermJsonArray);
                appList.put("permissions", permissionObject);
                mainArray.put(appList);
            } else {
                Log.i("\t\tApp Permission ", "No Permissions");
            }
        }
        mainList.put("list", mainArray);
        //Log.i("Main JSON List",mainList.toString());
        return mainList;
    }


    // Onclick function for the Show Report button
    public void showReport(View v){
        Intent intent = new Intent(getApplicationContext(),Report.class);
        Log.i("applicationReport",applicationReport);
        intent.putExtra("applicationReport", applicationReport);
        intent.putExtra("appScore",appScoreForNextIntent + "");
        intent.putExtra("settingScore", settingScoreForNextIntent + "");
        intent.putExtra("settingReport",settingsReport);
        startActivity(intent);
    }
}