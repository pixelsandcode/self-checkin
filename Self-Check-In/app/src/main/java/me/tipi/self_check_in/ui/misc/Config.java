package me.tipi.self_check_in.ui.misc;

import com.microblink.recognizers.blinkbarcode.usdl.USDLRecognizerSettings;
import com.microblink.recognizers.blinkid.mrtd.MRTDRecognizerSettings;
import com.microblink.recognizers.settings.RecognizerSettings;

public class Config {

    /** Returns recognizer settings */
    public static RecognizerSettings[] getRecognizerSettings() {
        MRTDRecognizerSettings sett = new MRTDRecognizerSettings();
        USDLRecognizerSettings usdlSett = new USDLRecognizerSettings();

        return new RecognizerSettings[] {
                sett, usdlSett
        };
    }
}

