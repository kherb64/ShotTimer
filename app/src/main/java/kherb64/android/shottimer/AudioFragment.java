package kherb64.android.shottimer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AudioFragment extends Fragment {

    private static final int MAX_SAMPLES = 10000;
    private static final String LOG_TAG = "AudioAnalysis";
    private static final int mTargetDir = 2;
    private List<Short> mSData = new ArrayList<Short>();

    public AudioFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_audio, container,
                false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String[] mAudioKeyValuesArray = new String[]{"K1", "K2", "K3", "K4",
                "K5"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, mAudioKeyValuesArray);
        ListView listView = (ListView) getActivity().findViewById(
                R.id.listAudioKeyValues);
        listView.setAdapter(adapter);

    }

    public void loadSamples() {
        mSData.clear();
        loadFile();
        if (mSData.size() > 0) {
            analyzeSamples();
            displayKeyValues();
        }
    }

    private void analyzeSamples() {
        // TODO calculate KeyValues
        displayToast("analyzing " + Integer.toString(mSData.size())
                + " samples");
        float amplitudeM = 0;
        float energyM = 0;
        final int M = 50; // Mächtigkeit der Mittelwerte

        for (int i = 0; i < mSData.size(); i++) {
            if (i > MAX_SAMPLES)
                break;
            if (i > M) {
                amplitudeM = 0f;
                energyM = 0f;
                for (int m = 0; m < M; m++) {
                    float f = mSData.get(i - m) / 32768;
                    amplitudeM += Math.abs(f);
                    energyM += f * f;
                }
                amplitudeM /= 5;
                energyM /= 5;
                Log.d(LOG_TAG,
                        "amplitude" + Integer.toString(M) + " "
                                + Float.toString(amplitudeM) + " energy"
                                + Integer.toString(M) + " "
                                + Float.toString(energyM)
                );
            }
        }
    }

    private void displayKeyValues() {
        // TODO display KeyValues

    }

    void loadFile() {
        // get automatic filename
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        File file = new File("Samples-" + dateFormat.format(date) + ".pcm");
        String fileName = getMyDirName() + file.getName();

        loadFile(fileName);
    }

    void loadFile(String fileName) {
        mSData.clear();

        displayToast("loading file");

        FileInputStream fos = null;
        DataInputStream os = null;
        try {
            // Log.d(LOG_TAG, "opening file " + fileName);
            fos = new FileInputStream(fileName);
            os = new DataInputStream(new BufferedInputStream(fos));
            // Log.d(LOG_TAG, "file " + fileName + " opened");
            while (os.available() > 0) {
                mSData.add(os.readShort());
                if (mSData.size() > MAX_SAMPLES)
                    break;
            }
            displayToast(Integer.toString(mSData.size()) + " samples loaded");
            os.close();
            // Log.d(LOG_TAG, "file " + fileName + " closed");
        } catch (FileNotFoundException e2) {
            displayToast("file " + fileName + " not found", Toast.LENGTH_LONG);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadPrefs(SharedPreferences prefs) {
        if (prefs == null)
            displayToast("Preferences unavailable, continue with defaults");
        else {
            // Autostart
            // mAutostartEnabled = prefs.getBoolean(
            // getString(R.string.pref_key_autostart_enabled),
            // mAutostartEnabled);
            // mAutostartMinSecs = getIntPref(prefs,
            // getString(R.string.pref_key_autostart_minsecs),
            // mAutostartMinSecs);
        }
    }

    private String getMyDirName() {
        String dirName = null;
        switch (mTargetDir) {
            case 1:
                // private directory in data/data ...
                dirName = ((Context) MainActivity.Context).getFilesDir() + "/";
                break;
            case 2:
                // public directory in Android/data ...
                dirName = ((Context) MainActivity.Context)
                        .getExternalFilesDir(null) + "/";
                break;
            case 3:
                // public directory
                dirName = Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + "/media/";
                break;
        }
        return dirName;

    }

    private void displayToast(String string) {
        displayToast(string, Toast.LENGTH_SHORT);
    }

    private void displayToast(String string, int Length) {
        Log.d(LOG_TAG, string);
        Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                string, Length);
        toast.show();
    }

}
