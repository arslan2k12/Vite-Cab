package com.parse.starter;

import android.app.Application;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCrashReporting;
import com.parse.ParseUser;

public class ParseApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    
    
    // Initialize Crash Reporting.
    ParseCrashReporting.enable(this);

    // Enable Local Datastore.
    Parse.enableLocalDatastore(this);

    // Add your initialization code here
    Parse.initialize(this, "3b37nZAArmJsRHFDSdfd7MPxvds4XOundEDR15go", "IJasI9SLtyGsD5TZWF5mmE7X6FCQbbr8RtKOa2hq");

    ParseUser.enableAutomaticUser();
    //ParseUser.getCurrentUser().saveInBackground();
    ParseACL defaultACL = new ParseACL();
    // Optionally enable public read access.
     defaultACL.setPublicReadAccess(true);
     defaultACL.setPublicWriteAccess(true);
     
    ParseACL.setDefaultACL(defaultACL, true);

    
  }
}
