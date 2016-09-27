
package me.tipi.self_check_in;

import android.support.test.rule.ActivityTestRule;

import com.squareup.spoon.Spoon;

import org.junit.Rule;
import org.junit.Test;

import me.tipi.self_check_in.ui.MainActivity;
import tools.fastlane.screengrab.Screengrab;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by NP on 9/24/2016.
 */
public class LogInFragmentTest {
    @Rule
    public ActivityTestRule<MainActivity> activityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void validPassEmptyError() {
        Screengrab.screenshot("grabber_pass_first");
        Spoon.screenshot(activityTestRule.getActivity(), "spoon_pass_fisrt");
        onView(withId(R.id.password))
                .perform(typeText(""), closeSoftKeyboard());
        onView(withId(R.id.email))
                .perform(typeText("a@j.lo"), closeSoftKeyboard());
        onView(withId(R.id.submit_btn)).perform(click());
        onView(withText(R.string.error_field_required)).check(matches(isDisplayed()));
        Screengrab.screenshot("grabber_pass_second");
        Spoon.screenshot(activityTestRule.getActivity(), "spoon_pass_second");
    }

    @Test
    public void validEmailEmptyError() {
        onView(withId(R.id.password))
                .perform(typeText("1234567895"), closeSoftKeyboard());
        onView(withId(R.id.email))
                .perform(typeText(""), closeSoftKeyboard());
        onView(withId(R.id.submit_btn)).perform(click());
        onView(withText(R.string.error_field_required)).check(matches(isDisplayed()));

    }

    @Test
    public void validEmailPatternError() {
        Screengrab.screenshot("graber_email_fisrt");
        Spoon.screenshot(activityTestRule.getActivity(), "spoon_email_fisrt");
        onView(withId(R.id.password))
                .perform(typeText("1234567895"), closeSoftKeyboard());
        onView(withId(R.id.email))
                .perform(typeText("asas"), closeSoftKeyboard());
        onView(withId(R.id.submit_btn)).perform(click());
        onView(withText(R.string.error_invalid_email)).check(matches(isDisplayed()));
        Screengrab.screenshot("graber_email_second");
        Spoon.screenshot(activityTestRule.getActivity(), "spoon_email_second");
    }

    @Test
    public void validPassPatternError() {
        onView(withId(R.id.password))
                .perform(typeText("12"), closeSoftKeyboard());
        onView(withId(R.id.email))
                .perform(typeText("asas@f.k"), closeSoftKeyboard());
        onView(withId(R.id.submit_btn)).perform(click());
        onView(withText(R.string.error_incorrect_password)).check(matches(isDisplayed()));

    }

//    @Test
//    public void validInput() {
//        onView(withId(R.id.password))
//                .perform(typeText("adminadmin"), closeSoftKeyboard());
//        onView(withId(R.id.email))
//                .perform(typeText("admin@matchbox.com"), closeSoftKeyboard());
//        onView(withId(R.id.submit_btn)).perform(click());
//        intended(toPackage("me.tipi.self_check_in.ui"));
//    }


}