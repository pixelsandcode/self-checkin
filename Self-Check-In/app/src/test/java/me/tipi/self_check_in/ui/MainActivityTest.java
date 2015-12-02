/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.ui;


import android.os.Build;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import me.tipi.self_check_in.BuildConfig;
import me.tipi.self_check_in.R;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
@RunWith(RobolectricGradleTestRunner.class)
public class MainActivityTest {

  private ActivityController<MainActivity> controller;
  private MainActivity mainActivity;

  // @Before => JUnit 4 annotation that specifies this method should run before each test is run
  // Useful to do setup for objects that are needed in the test
  @Before
  public void setup() {
    // Convenience method to run MainActivity through the Activity Lifecycle methods:
    // onCreate(...) => onStart() => onPostCreate(...) => onResume()
    mainActivity = Robolectric.setupActivity(MainActivity.class);
    controller = Robolectric.buildActivity(MainActivity.class);
  }

  // @Test => JUnit 4 annotation specifying this is a test to be run
  // The test simply checks that our TextView exists and has the text "Hello world!"
  @Test
  public void validateTextViewContent() {
    TextView logoTextView = (TextView) mainActivity.findViewById(R.id.main_logo);
    assertNotNull("TextView could not be found", logoTextView);
    assertTrue("TextView contains incorrect text",
        "Mobile Check-in for Travellers".equals(logoTextView.getText().toString()));
  }
}
