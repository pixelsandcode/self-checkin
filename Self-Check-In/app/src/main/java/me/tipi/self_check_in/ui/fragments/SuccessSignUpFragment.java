/*
 * *
 *  * Copyright (c) 2015-2016 www.Tipi.me.
 *  * Created by Ashkan Hesaraki.
 *  * Ashkan.Hesaraki@gmail.com
 *
 */

package me.tipi.self_check_in.ui.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.RT_Printer.BluetoothPrinter.BLUETOOTH.BluetoothPrintDriver;
import com.afollestad.materialdialogs.MaterialDialog;
import com.f2prateek.rx.preferences.Preference;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tipi.self_check_in.R;
import me.tipi.self_check_in.SelfCheckInApp;
import me.tipi.self_check_in.data.PrinterPreference;
import me.tipi.self_check_in.data.api.ApiConstants;
import me.tipi.self_check_in.data.api.models.Guest;
import me.tipi.self_check_in.ui.AppContainer;
import me.tipi.self_check_in.ui.FindUserActivity;
import me.tipi.self_check_in.ui.SignUpActivity;
import me.tipi.self_check_in.ui.events.BackShouldShowEvent;
import me.tipi.self_check_in.ui.events.SettingShouldShowEvent;
import timber.log.Timber;

import static com.RT_Printer.BluetoothPrinter.BLUETOOTH.BluetoothPrintDriver.BT_Write;

public class SuccessSignUpFragment extends Fragment {

  public static final String TAG = SuccessSignUpFragment.class.getSimpleName();
  @Inject
  PrinterPreference printerPreference;
  @Inject
  Guest guest;
  @Inject
  AppContainer appContainer;
  @Inject
  Bus bus;
  @Inject
  Tracker tracker;
  @Inject
  @Named(ApiConstants.HOSTEL_NAME)
  Preference<String> hostelName;
  @Bind(R.id.printer_btn) ImageButton printerBtn;
  private BluetoothAdapter mBluetoothAdapter;
  private BluetoothDevice mBluetoothDevice;
  private MaterialDialog loading;
  private BluetoothSocket mBluetoothSocket;
  private UUID applicationUUID;
  private BluetoothPrintDriver mChatService = null;
  private boolean isVisible = false;
  private boolean isConnecting = false;

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
    loading = new MaterialDialog.Builder(getActivity())
        .content("Please wait...")
        .cancelable(false)
        .progress(true, 0)
        .build();
    applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    if (printerPreference.get()) {
      connectToPrinter();
    }

    return rootView;
  }

  @Override
  public void onStart() {
    super.onStart();
    if (printerPreference.get()) {
      if (mBluetoothAdapter.isEnabled()) {
        if (mChatService == null) setupChat();
      }
    }
  }

  @Override public void onDestroy() {
    super.onDestroy();
    if (printerPreference.get()) {
      if (mChatService != null) mChatService.stop();
    }
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

  private void connectToPrinter() {
    if (mChatService == null) {
      setupChat();
    }
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if (mChatService.getState() == BluetoothPrintDriver.STATE_NONE && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
      mChatService.start();
    }
    if (mBluetoothAdapter != null) {
      if (!mBluetoothAdapter.isEnabled()) {
        loading.dismiss();
        printerBtn.setVisibility(View.VISIBLE);
        showSnackbar("Bluetooth is OFF");
      } else {
        ListPairedDevices();
      }
    }
  }

  private void ListPairedDevices() {
    Set<BluetoothDevice> mPairedDevices = mBluetoothAdapter.getBondedDevices();
    if (mPairedDevices.size() > 0) {
      for (BluetoothDevice mDevice : mPairedDevices) {
        if (mDevice != null && mDevice.getAddress().equals("00:02:0A:03:3C:E0")) {
          isVisible = true;
          handleBlutooth(mDevice);
          break;
        }
      }
      if (!isVisible) {
        loading.dismiss();
        printerBtn.setVisibility(View.VISIBLE);
        showSnackbar("Device is not pair");
      }
    }
  }

  private void handleBlutooth(BluetoothDevice mDevice) {
    String mDeviceAddress = mDevice.getAddress();
    mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
    new Thread(new Runnable() {
      @Override public void run() {
        try {
          mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(applicationUUID);
          mBluetoothAdapter.cancelDiscovery();
          mChatService.connect(mBluetoothDevice);
          Timber.w("ConnectToSocket");
        } catch (IOException eConnectException) {
          Timber.w("CouldNotConnectToSocket");
          closeSocket(mBluetoothSocket);
        }
      }
    }).start();
  }

  private void setupChat() {
    mChatService = new BluetoothPrintDriver(getActivity(), mHandler);
  }

  private final Handler mHandler;

  {
    mHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
        switch (msg.what) {
          case 1:
            Timber.w("MESSAGE_STATE_CHANGE: " + msg.arg1);
            switch (msg.arg1) {
              case BluetoothPrintDriver.STATE_CONNECTED:
                Timber.w("Print connected");
                if (BluetoothPrintDriver.IsNoConnection()) {
                  printerBtn.setVisibility(View.VISIBLE);
                  showSnackbar("Printer not connected");
                }
                isConnecting = false;
                printQR(guest.email);
                loading.dismiss();
                printFeed(3);
                break;
              case BluetoothPrintDriver.STATE_CONNECTING:
                loading.show();
                isConnecting = true;
                Timber.w("Print connecting");
                break;
              case BluetoothPrintDriver.STATE_LISTEN:

              case BluetoothPrintDriver.STATE_NONE:
                Timber.w("Printer not connected");
                loading.dismiss();
                if (isConnecting) {
                  printerBtn.setVisibility(View.VISIBLE);
                  showSnackbar("Printer not connected");
                  isConnecting = false;
                }
                break;
            }
            break;
          case 3:
            break;
          case 2:
            byte[] readBuf = (byte[]) msg.obj;
            Timber.w("readBuf[0]:" + readBuf[0] + "  readBuf[1]:" + readBuf[1] + "readBuf[2]:" + readBuf[2]);
            if (readBuf[2] == 0)
              Timber.w("NO ERROR!");
            else {
              if ((readBuf[2] & 0x02) != 0) {
                Timber.w("ERROR: No printer connected!");
                loading.dismiss();
                printerBtn.setVisibility(View.VISIBLE);
                showSnackbar("No printer connected!");
              }
              if ((readBuf[2] & 0x04) != 0) {
                Timber.w("ERROR: m! ");
                loading.dismiss();
                printerBtn.setVisibility(View.VISIBLE);
                showSnackbar("No paper!");
              }
              if ((readBuf[2] & 0x08) != 0) {
                Timber.w("ERROR: Voltage is too low!!");
                loading.dismiss();
                printerBtn.setVisibility(View.VISIBLE);
                showSnackbar("Voltage is too low!");
              }
              if ((readBuf[2] & 0x40) != 0) {
                Timber.w("ERROR: Printer Over Heat! ");
                loading.dismiss();
                printerBtn.setVisibility(View.VISIBLE);
                showSnackbar("Printer Over Heat!");
              }
            }
            break;
        }
      }
    };
  }

  private void closeSocket(BluetoothSocket nOpenSocket) {
    try {
      nOpenSocket.close();
      Timber.w("SocketClosed");
    } catch (IOException ex) {
      Timber.w("CouldNotCloseSocket");
    }
  }

  private void printQR(String email) {
    int emailLength = email.length();
    printFeed(3);
    byte[] cmd = new byte[]{(byte) 29, (byte) 40, (byte) 107, (byte) 3, (byte) 0, (byte) 49, (byte) 67, (byte) 10, (byte) 29, (byte) 40, (byte) 107, (byte) 3, (byte) 0, (byte) 49, (byte) 69, (byte) 51, (byte) 29, (byte) 40, (byte) 107, (byte) 10, (byte) 0, (byte) 49, (byte) 80, (byte) 48, (byte) 29, (byte) 40, (byte) 107, (byte) 3, (byte) 0, (byte) 49, (byte) 81, (byte) 48, (byte) 27, (byte) 97, (byte) 49, (byte) 29, (byte) 40, (byte) 107, (byte) 4, (byte) 0, (byte) 49, (byte) 65, (byte) 49, (byte) 0, (byte) 27, (byte) 100, (byte) 5};
    byte[] res = new byte[41 + emailLength];
    System.arraycopy(cmd, 0, res, 0, 24);
    res[19] = (byte) (emailLength + 3);
    for (int i = 24; i < 24 + emailLength; i++) {
      res[i] = (byte) email.charAt(i - 24);
    }
    System.arraycopy(cmd, 24, res, 24 + emailLength, 16);
    BT_Write(res);
    BluetoothPrintDriver.printString(" ");

  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    ButterKnife.unbind(this);
  }

  @OnClick(R.id.printer_btn) public void onClick() {
    connectToPrinter();
  }

  private void showSnackbar(String error) {
    String finalError = String.format(Locale.US, ". \n ( %s )", error);
    final Snackbar snackbar = Snackbar.make(appContainer.bind(getActivity()), getString(R.string.printer_error_snackbar) + finalError, Snackbar.LENGTH_INDEFINITE);
    snackbar.setAction("Ok", new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        snackbar.dismiss();
      }
    });
    View snackbarView = snackbar.getView();
    TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
    textView.setMaxLines(2);
    snackbar.show();
  }

  private void printFeed(int feedSize) {
    byte[] feed = new byte[]{(byte) 27, (byte) 100, (byte) feedSize};
    BT_Write(feed);
  }
}
