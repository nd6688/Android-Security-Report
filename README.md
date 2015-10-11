# Android Security Report
Android app to evaluate the permissions of installed application

#Built Using:
1. Android Studio
2. Node.js
3. Restful Web Services

#Description
We evaluate security in two different phases: 1) Evaluate System’s Settings 2) Evaluate Installed Application’s Details on Google Play Store

In the first phase we check different system settings and calculate final Settings Score based on user’s system settings. We go through different settings like: • If Bluetooth is On or Off • If USB debugging is On or OFF • If the device is rooted or not • If the device is connected to WPA / WEP / WEP2 • If the lock screen setting is set to auto lock or not • If the phone has a security feature for the lock screen

Based on the above conditions we calculate the score and give a final Settings Score for the user’s Android device.

In the second phase we evaluate different details of an application on the Google Play store. We divide the data into different categories and using that data we calculate the Application score for every application. For every application installed on user’s device we check the following properties: • Application’s current rating on play store • Application’s developer’s current reputation (Top developer or not) • Number of downloads from the Google Play store • Number of current reviews for the application • Category the application belongs to

Using the above details we calculate a score for every application and produce a Final score based on individual application score.
