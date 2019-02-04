package com.parse.starter;


import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
 
public class SettingsActivity extends PreferenceActivity
{
	 @Override
     public void onCreate(final Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.user_settings);
     }
}