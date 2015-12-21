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
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.api.AuthenticationService;
import me.tipi.self_check_in.data.api.models.FindResponse;
import me.tipi.self_check_in.data.api.models.Guest;
import me.tipi.self_check_in.data.api.models.User;
import me.tipi.self_check_in.ui.events.PagerChangeEvent;
import me.tipi.self_check_in.ui.transform.CircleStrokeTransformation;
import me.tipi.self_check_in.util.Strings;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class FindUserFragment extends Fragment {

  @Inject Picasso picasso;
  @Inject Bus bus;
  @Inject AuthenticationService authenticationService;
  @Inject Guest guest;

  @Bind(R.id.email) EditText emailEditText;
  @Bind(R.id.match_text) TextView matchTextView;
  @Bind(R.id.or_text) TextView orTextView;
  @Bind(R.id.sign_up_btn) Button signUpButton;
  @Bind(R.id.match_user_container) RelativeLayout matchedUserContainer;
  @Bind(R.id.avatar) ImageView avatarView;
  @Bind(R.id.user_name) TextView userNameView;
  @BindColor(R.color.colorAccent) int accentColor;

  private String enteredEmail;
  private MaterialDialog loading;

  /**
   * Instantiates a new Find user fragment.
   */
  public FindUserFragment() {
    // Required empty public constructor
  }

  /**
   * New instance find user fragment.
   *
   * @param context the context
   * @return the find user fragment
   */
  public static FindUserFragment newInstance(Context context) {
    FindUserFragment fragment = new FindUserFragment();
    SelfCheckInApp.get(context).inject(fragment);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment// Inflate the layout for this fragment
    View rootView = inflater.inflate(R.layout.fragment_find_user, container, false);
    ButterKnife.bind(this, rootView);
    Timber.d("ViewCreated");

    loading = new MaterialDialog.Builder(getActivity())
        .content("Please wait")
        .cancelable(false)
        .progress(true, 0)
        .build();

    matchedUserContainer.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        bus.post(new PagerChangeEvent(1));
      }
    });

    return  rootView;
  }

  @Override public void onResume() {
    super.onResume();
    bus.register(this);
    Timber.d("Resumed");
  }

  @Override public void onPause() {
    super.onPause();
    bus.unregister(this);
    Timber.d("Paused");
  }

  /**
   * Find.
   */
  @OnClick(R.id.find_btn)
  public void find() {
    if (!isError()) {
      guest.user_key = "";
      guest.name = "";
      orTextView.setVisibility(View.GONE);
      signUpButton.setVisibility(View.GONE);
      matchedUserContainer.setVisibility(View.GONE);
      loading.show();
      authenticationService.findUser(enteredEmail, new Callback<FindResponse>() {
        @Override public void success(FindResponse findResponse, Response response) {
          loading.dismiss();
          User matchedUser = findResponse.data;
          Timber.d("Found user: %s", matchedUser.toString());

          // Handling show/hide views
          matchTextView.setText(R.string.match_found_hint);
          matchTextView.setTextColor(accentColor);
          orTextView.setVisibility(View.GONE);
          signUpButton.setVisibility(View.GONE);
          matchedUserContainer.setVisibility(View.VISIBLE);

          // Filling user info
          picasso.load(Strings.makeAvatarUrl(matchedUser.doc_key))
              .resize(200, 200).centerCrop()
              .transform(new CircleStrokeTransformation(getActivity(), 0, 0))
              .placeholder(R.drawable.avatar_placeholder).into(avatarView);
          userNameView.setText(matchedUser.name);
          guest.name = matchedUser.name;
          guest.user_key = matchedUser.doc_key;
        }

        @Override public void failure(RetrofitError error) {
          loading.dismiss();

          // Handling show/hide views
          matchTextView.setText(R.string.no_match_hint);
          matchTextView.setTextColor(accentColor);
          orTextView.setVisibility(View.VISIBLE);
          signUpButton.setVisibility(View.VISIBLE);
          matchedUserContainer.setVisibility(View.GONE);
          Timber.d("Error finding: %s", error.toString());
        }
      });
    }
  }

  /**
   * Is error boolean.
   *
   * @return the boolean
   */
  private boolean isError() {

    emailEditText.setError(null);

    boolean cancel = false;
    View focusView = null;

    enteredEmail = emailEditText.getText().toString();

    // Check for validation
    if (TextUtils.isEmpty(enteredEmail)) {
      emailEditText.setError(getString(R.string.error_field_required));
      focusView = emailEditText;
      cancel = true;
    } else if (!Strings.isValidEmail(enteredEmail)) {
      emailEditText.setError(getString(R.string.error_invalid_email));
      focusView = emailEditText;
      cancel = true;
    }

    if (cancel) {
      if (focusView != null) {
        focusView.requestFocus();
      }
    }

    return cancel;
  }

}
