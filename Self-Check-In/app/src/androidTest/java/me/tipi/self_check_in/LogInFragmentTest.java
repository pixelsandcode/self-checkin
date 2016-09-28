
package me.tipi.self_check_in;

import android.support.test.rule.ActivityTestRule;

import com.squareup.spoon.Spoon;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import me.tipi.self_check_in.ui.MainActivity;

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

@FixMethodOrder(MethodSorters.NAME_ASCENDING)

public class LogInFragmentTest {
    @Rule
    public ActivityTestRule<MainActivity> activityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void validPassEmptyError() {
        Spoon.screenshot(activityTestRule.getActivity(), "spoon_pass_empty_fisrt");
        onView(withId(R.id.password))
                .perform(typeText(""), closeSoftKeyboard());
        onView(withId(R.id.email))
                .perform(typeText("a@j.lo"), closeSoftKeyboard());
        onView(withId(R.id.submit_btn)).perform(click());
        onView(withText(R.string.error_field_required)).check(matches(isDisplayed()));
        Spoon.screenshot(activityTestRule.getActivity(), "spoon_pass_empty_second");
    }

    @Test
    public void validEmailEmptyError() {
        Spoon.screenshot(activityTestRule.getActivity(), "spoon_email_emty_fisrt");

        onView(withId(R.id.password))
                .perform(typeText("1234567895"), closeSoftKeyboard());
        onView(withId(R.id.email))
                .perform(typeText(""), closeSoftKeyboard());
        onView(withId(R.id.submit_btn)).perform(click());
        onView(withText(R.string.error_field_required)).check(matches(isDisplayed()));
        Spoon.screenshot(activityTestRule.getActivity(), "spoon_email_empty_second");

    }

    @Test
    public void validEmailPatternError() {
        Spoon.screenshot(activityTestRule.getActivity(), "spoon_email_pattern_fisrt");
        onView(withId(R.id.password))
                .perform(typeText("1234567895"), closeSoftKeyboard());
        onView(withId(R.id.email))
                .perform(typeText("asas"), closeSoftKeyboard());
        onView(withId(R.id.submit_btn)).perform(click());
        onView(withText(R.string.error_invalid_email)).check(matches(isDisplayed()));
        Spoon.screenshot(activityTestRule.getActivity(), "spoon_email_pattern_second");
    }

    @Test
    public void validPassPatternError() {
        Spoon.screenshot(activityTestRule.getActivity(), "spoon_pass_pattern_fisrt");

        onView(withId(R.id.password))
                .perform(typeText("12"), closeSoftKeyboard());
        onView(withId(R.id.email))
                .perform(typeText("asas@f.k"), closeSoftKeyboard());
        onView(withId(R.id.submit_btn)).perform(click());
        onView(withText(R.string.error_incorrect_password)).check(matches(isDisplayed()));
        Spoon.screenshot(activityTestRule.getActivity(), "spoon_pass_pattern_second");


    }

    @Test
    public void zValidIntent() {
        onView(withId(R.id.password))
                .perform(typeText("adminadmin"), closeSoftKeyboard());
        onView(withId(R.id.email))
                .perform(typeText("admin@matchbox.com"), closeSoftKeyboard());
        onView(withId(R.id.submit_btn)).perform(click());
        onView(withId(R.id.main_icon_btn)).check(matches(isDisplayed()));
    }


}