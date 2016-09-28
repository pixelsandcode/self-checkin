/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.f2prateek.rx.preferences.Preference;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.data.api.models.Guest;
import me.tipi.self_check_in.ui.FindUserActivity;
import me.tipi.self_check_in.ui.SignUpActivity;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.SettingShouldShowEvent;
import timber.log.Timber;

public class SuccessSignUpFragment extends Fragment {

    public static final String TAG = SuccessSignUpFragment.class.getSimpleName();
    @Inject
    Guest guest;
    @Inject
    Bus bus;
    @Inject
    Tracker tracker;
    @Inject
    @Named(ApiConstants.HOSTEL_NAME)
    Preference<String> hostelName;

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            startOver();
        }
    };

    /**
     * Instantiates a new Success sign up fragment.
     */
    public SuccessSignUpFragment() {
        // Required empty public constructor
    }

    /**
     * New instance success sign up fragment.
     *
     * @param context the context
     * @return the success sign up fragment
     */
    public static SuccessSignUpFragment newInstance(Context context) {
        SuccessSignUpFragment fragment = new SuccessSignUpFragment();
        SelfCheckInApp.get(context).inject(fragment);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_success_sign_up, container, false);
        ButterKnife.bind(this, rootView);
        removePhoto();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
        if (getActivity() != null) {
            bus.post(new BackShouldShowEvent(false));
            bus.post(new SettingShouldShowEvent(false));

            handler.postDelayed(runnable, ApiConstants.START_OVER_TIME);
        }
        tracker.setScreenName("Success");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
    }

    @OnClick(R.id.continue_btn)
    public void finishTapped() {
        startOver();
    }

    private void startOver() {
        if (getActivity() != null && getActivity() instanceof SignUpActivity) {
            ((SignUpActivity) getActivity()).reset();
        } else if (getActivity() != null) {
            ((FindUserActivity) getActivity()).reset();
        }
    }

    private void removePhoto() {
        File passportPhoto = new File(guest.passportPath);
        File avatarPhoto = new File(guest.avatarPath);

        if (passportPhoto.exists()) {
            if (passportPhoto.delete()) {
                Timber.w("passport photo deleted");
            }
        }
        if (avatarPhoto.exists()) {
            if (avatarPhoto.delete()) {
                Timber.w("avatar photo deleted");
            }
        }
    }
}
