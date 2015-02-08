package kherb64.android.shottimer;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingsFragment()).commit();
	}

	public static class SettingsFragment extends PreferenceFragment implements
			OnSharedPreferenceChangeListener {

		private SharedPreferences mSharedPrefs;

		// private OnSharedPreferenceChangeListener mPrefsListener;

		@Override
		public void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);

			mSharedPrefs = getPreferenceManager().getSharedPreferences();
			// mSharedPref
			// .registerOnSharedPreferenceChangeListener(mPrefsListener);

			validatePrefs(mSharedPrefs);
			setSummaries(mSharedPrefs);
		}

		private void validatePrefs(SharedPreferences prefs) {
			// validates all necessary preferences
			validatePref(prefs, getString(R.string.pref_key_autostart_minsecs));
			validatePref(prefs, getString(R.string.pref_key_autostart_maxsecs));
			validatePref(prefs, getString(R.string.pref_key_startsound_volume));

		}

		private void validatePref(SharedPreferences prefs, String key) {
			// validates a preference value
			Integer i;
			if (key.equals(getString(R.string.pref_key_autostart_minsecs))
					|| key.equals(getString(R.string.pref_key_autostart_maxsecs))) {
				i = getIntPref(prefs, key, 3);
				if (i < 1) {
					SharedPreferences.Editor editor1 = prefs.edit();
					editor1.putString(key, "1");
					editor1.commit();
				}
				if (i > 7) {
					SharedPreferences.Editor editor1 = prefs.edit();
					editor1.putString(key, "7");
					editor1.commit();
				}
			}
			if (key.equals(getString(R.string.pref_key_startsound_volume))) {
				i = getIntPref(prefs, key, 75);
				if (i < 0) {
					SharedPreferences.Editor editor1 = prefs.edit();
					editor1.putString(key, "0");
					editor1.commit();
				}
				if (i > 100) {
					SharedPreferences.Editor editor1 = prefs.edit();
					editor1.putString(key, "100");
					editor1.commit();
				}
			}

		}

		private void setSummaries(SharedPreferences prefs) {
			// updates all necessary summaries
			updatePrefSummary(prefs,
					getString(R.string.pref_key_autostart_minsecs));
			updatePrefSummary(prefs,
					getString(R.string.pref_key_autostart_maxsecs));
			updatePrefSummary(prefs,
					getString(R.string.pref_key_startsound_volume));


            updatePrefSummary(prefs,"pref_key_FFT_MeanValMin");
            updatePrefSummary(prefs,"pref_key_FFT_SumValMin");
            updatePrefSummary(prefs,"pref_key_FFT_MeanMinValMin");
            updatePrefSummary(prefs,"pref_key_FFT_SumMinValMin");
		}

		@Override
		public void onResume() {
			super.onResume();
			mSharedPrefs.registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause() {
			super.onPause();
			mSharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
			validatePrefs(mSharedPrefs);
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences prefs,
				String key) {
			// displayToast("pref change detected");
			updatePrefSummary(prefs, key);

		}

		private void updatePrefSummary(SharedPreferences prefs, String key) {
			// updates a preference summary according to android suggestions

			// displayToast(key + " has changed");
			Preference pref = findPreference(key);
			if (pref == null) {
				displayToast("Unknown Preference " + key + " ignored");
				return;
			}
			CharSequence summary = pref.getSummary();

			// TODO validate on the fly

			int i;
			if (key.equals(getString(R.string.pref_key_autostart_minsecs))
					|| key.equals(getString(R.string.pref_key_autostart_maxsecs))) {
				i = getIntPref(prefs, key, 3);
				summary = Integer.toString(i) + " seconds";
			}
			if (key.equals(getString(R.string.pref_key_startsound_volume))) {
				i = getIntPref(prefs, key, 75);
				summary = Integer.toString(i) + " percent";
			}
			if (key.equals("pref_key_FFT_MeanValMin")) {
				i = getIntPref(prefs, key, 3);
				summary = Integer.toString(i);
			}
			if (key.equals("pref_key_FFT_SumValMin")) {
				i = getIntPref(prefs, key, 2000);
				summary = Integer.toString(i);
			}
			if (key.equals("pref_key_FFT_MeanMinValMin")) {
				i = getIntPref(prefs, key, 3);
				summary = Integer.toString(i);
			}
			if (key.equals("pref_key_FFT_SumMinValMin")) {
				i = getIntPref(prefs, key, 1000);
				summary = Integer.toString(i);
			}

			// Set summary to be the user-description for the selected value
			pref.setSummary(summary);
		}

		private int getIntPref(SharedPreferences prefs, String key, int value) {
			// get an integer value from a string preference
			String s = Integer.toString(value);
			s = prefs.getString(key, s);
			// TODO catch integer violation
			try {
				value = Integer.parseInt(s);
			} catch (NumberFormatException e) {
				value = 0;
			}
			return value;
		}

		private void displayToast(String string) {
			Toast toast = Toast.makeText(getActivity().getApplicationContext(),
					string, Toast.LENGTH_SHORT);
			toast.show();
		}
	} // SettingsFragment

}
