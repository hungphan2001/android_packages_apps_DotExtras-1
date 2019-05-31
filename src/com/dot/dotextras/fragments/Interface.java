/*
 * Copyright (C) 2014-2016 The Dirty Unicorns Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dot.dotextras.fragments;

import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.SystemProperties;
import android.graphics.Color;
import android.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.dot.dotextras.preference.CustomSeekBarPreference;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.wrapper.OverlayManagerWrapper;
import com.android.settings.wrapper.OverlayManagerWrapper.OverlayInfo;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.Utils;
import com.android.internal.util.dotos.DOTUtils;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
public class Interface extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

        private static final String TAG = "Interface";

        private static final String OMNI_QS_PANEL_BG_ALPHA = "qs_panel_bg_alpha";

        private static final String ACCENT_COLOR = "accent_color";

        private static final String ACCENT_COLOR_PROP = "persist.sys.theme.accentcolor";

        private CustomSeekBarPreference mQsPanelAlpha;
        private ColorPickerPreference mThemeColor;
        private OverlayManagerWrapper mOverlayService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.interface_settings);

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefSet = getPreferenceScreen();

        mQsPanelAlpha = (CustomSeekBarPreference) findPreference(OMNI_QS_PANEL_BG_ALPHA);
        int qsPanelAlpha = Settings.System.getIntForUser(resolver,
                Settings.System.OMNI_QS_PANEL_BG_ALPHA, 255, UserHandle.USER_CURRENT);
        mQsPanelAlpha.setValue(qsPanelAlpha);
        mQsPanelAlpha.setOnPreferenceChangeListener(this);

        mThemeColor = (ColorPickerPreference) findPreference(ACCENT_COLOR);
        String colorVal = SystemProperties.get(ACCENT_COLOR_PROP, "-1");
        int color = "-1".equals(colorVal)
                ? Color.WHITE
                : Color.parseColor("#" + colorVal);
        mThemeColor.setNewPreviewColor(color);
        mThemeColor.setOnPreferenceChangeListener(this);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.DOTEXTRAS;
    }

	
    @Override
    public void onResume() {
        super.onResume();
    }
	
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mQsPanelAlpha) {
            int bgAlpha = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.OMNI_QS_PANEL_BG_ALPHA, bgAlpha,
                    UserHandle.USER_CURRENT);
		 return true;
		
        } else if (preference == mThemeColor) {
            int color = (Integer) newValue;
            String hexColor = String.format("%08X", (0xFFFFFFFF & color));
            SystemProperties.set(ACCENT_COLOR_PROP, hexColor);
            mOverlayService.reloadAndroidAssets(UserHandle.USER_CURRENT);
            mOverlayService.reloadAssets("com.android.settings", UserHandle.USER_CURRENT);
            mOverlayService.reloadAssets("com.android.systemui", UserHandle.USER_CURRENT);
            DOTUtils.showSystemUiRestartDialog(getContext());
        return true;
        }
        return false;
    }
}
