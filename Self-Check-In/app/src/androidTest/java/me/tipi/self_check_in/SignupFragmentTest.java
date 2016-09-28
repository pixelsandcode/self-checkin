package me.tipi.self_check_in;

import android.support.test.rule.ActivityTestRule;

import com.squareup.spoon.Spoon;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import me.tipi.self_check_in.ui.SignUpActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by NP on 9/28/2016.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)

public class SignupFragmentTest {
    @Rule
    public ActivityTestRule<SignUpActivity> activityTestRule =
            new ActivityTestRule<>(SignUpActivity.class);

    @Test
    public void aMainFragment() {
        Spoon.screenshot(activityTestRule.getActivity(), "mainFragment_first");
        onView(withId(R.id.main_btn)).perform(click());
        onView(withText(R.string.checked_in_text)).check(matches(isDisplayed()));
        Spoon.screenshot(activityTestRule.getActivity(), "mainFragment_second");
    }

//    @Test
//    public void bLandingFragment() {
//        activityTestRule.getActivity().showLandingFragment();
//        onView(withText(R.string.checked_in_text)).check(matches(isDisplayed()));
//        Spoon.screenshot(activityTestRule.getActivity(), "landing_first_yes");
//        onView(withId(R.id.yes_btn)).perform(click());
//        onView(withText(R.string.enter_your_tipi_email)).check(matches(isDisplayed()));
//        onView(withId(R.id.no_btn)).perform(click());
//        onView(withText(R.string.enter_your_email)).check(matches(isDisplayed()));

}
