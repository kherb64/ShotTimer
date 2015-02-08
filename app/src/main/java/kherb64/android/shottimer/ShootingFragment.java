package kherb64.android.shottimer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import kherb64.android.shottimer.R.id;
import kherb64.android.shottimer.ShotRecorder2.OnShotDetectedListener;

public class ShootingFragment extends Fragment implements
        OnShotDetectedListener {

    private static final int STATE_INACTIVE = 0;
    private static final int STATE_READY = 1;
    private static final int STATE_STANDBY = 2;
    private static final int STATE_ACTIVE = 3;
    private static final int STATE_FINISHED = 4;
    private static final int STATE_SCORING = 5;

    private static final int SOUND_START = 1;
    private static final String LOG_TAG = "Shooting";
    // handler & runnable for updating UI
    private final Handler mHandler = new Handler();
    private final Runnable updateRunnable = new Runnable() {
        public void run() {
            // call the activity method that updates the UI
            updateShotScreen();
        }
    };
    // Settings
    private boolean mStartSoundEnabled = true;
    private int mStartSoundVolumePercent = 75;
    private boolean mAutostartEnabled = true;
    private int mAutostartMinSecs = 3;
    private int mAutostartMaxSecs = 7;
    private boolean mSaveShotTimes = false;
    // Defined Array values to show in ListView
    // String[] mShotArray = new String[] {};
    private ArrayList<String> mShotArray = new ArrayList<String>();
    // Define a new Adapter
    // First parameter - Context
    // Second parameter - Layout for the row
    // Forth - the Array of data
    private ArrayAdapter<String> mAdapter;
    private Button mBtnStandBy;
    private Button mBtnStart;
    private Button mBtnShot;
    private Button mBtnReset;
    private Button mBtnStop;
    // TODO make save button: saves cached files to permanent ones
    private Chronometer mChronometer;
    private TextView mViewNumShots;
    private TextView mViewState;
    private CountDownTimer mAutostartTimer;
    private int mStartSoundID;
    private long mStartTimeMillis;
    private float mLastShotTime;
    private int mNumShots;
    // TODO find best audio recorder
    // ShotRecorder1 mShotRecorder;
    private ShotRecorder2 mShotRecorder;
    private MainActivity mMainActivity;
    private SoundPool mSoundPool;
    private Integer mState = 0;


    public ShootingFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        showDebug(Log.VERBOSE, "onAttach");
        mMainActivity = (MainActivity) activity;
        mMainActivity.setFragmentHandle(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showDebug(Log.VERBOSE, "onCreate");
        // retain this fragment
        // setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        showDebug(Log.VERBOSE, "onCreateView");
        return inflater.inflate(R.layout.fragment_shooting, container,
                false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showDebug(Log.VERBOSE, "onActivityCreated");

        // layout initialization
        mAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, mShotArray);
        ListView listView = (ListView) getView().findViewById(
                R.id.listViewShots);
        listView.setAdapter(mAdapter);
        listView.setTranscriptMode(2);

        mBtnStandBy = (Button) getView().findViewById(R.id.buttonStandBy);
        mBtnStart = (Button) getView().findViewById(R.id.buttonStart);
        mBtnShot = (Button) getView().findViewById(R.id.buttonShot);
        mBtnStop = (Button) getView().findViewById(R.id.ButtonStop);
        mBtnReset = (Button) getView().findViewById(R.id.ButtonReset);
        mChronometer = (Chronometer) getView().findViewById(id.chronometer1);
        mViewNumShots = (TextView) getView()
                .findViewById(R.id.textViewNumShots);
        mViewState = (TextView) getView().findViewById(R.id.textViewState);

        // runtime initialization
        loadPrefs();

        mStartSoundID = mStartSoundEnabled ? loadSound(SOUND_START) : 0;

        // resetState(); keine Business-Logik bei Fragment-Lifecycle Ereignissen
    }

    @Override
    public void onStart() {
        super.onStart();
        showDebug(Log.VERBOSE, "onStart");
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null)
            restoreMyMembers(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        showDebug(Log.VERBOSE, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        showDebug(Log.VERBOSE, "onPause");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveMyMembers(outState);
    }


    @Override
    public void onStop() {
        super.onStop();
        showDebug(Log.VERBOSE, "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        showDebug(Log.VERBOSE, "onDestroy");
    }

    private void restoreMyMembers(Bundle savedInstanceState) {
        // showDebug("restoring class members");
        Integer State = savedInstanceState.getInt("mState");

        showDebug(Log.DEBUG, "class members " + State + " restored");

        setState(State);

        // showDebug("class members " + mGunner + " " + mState + " set");
    }

    private void saveMyMembers(Bundle outState) {
        outState.putInt("mState", mState);
        showDebug(Log.DEBUG, "class members " + mState + " saved");
    }

    public void resetShooting() {
        showDebug(Log.INFO, "resetShooting");
        resetTimer();
        resetState();
    }

    public void makeReady() {
        setState(STATE_READY);
    }

    public void standBy() {
        if (mShotRecorder == null)
            startShotRecorder();
        mShotRecorder.prepareRecording();
        setState(STATE_STANDBY);
        if (mAutostartEnabled)
            startAutostartTimer();
    }

    void startShotRecorder() {
        mShotRecorder = new ShotRecorder2();
        mShotRecorder.setShootingFrag(this);
    }

    private void startAutostartTimer() {
        Random r = new Random();
        long randomMillis;
        if (mAutostartMaxSecs > mAutostartMinSecs)
            randomMillis = r.nextInt(mAutostartMaxSecs * 1000
                    - mAutostartMinSecs * 1000)
                    + mAutostartMinSecs * 1000;
        else
            randomMillis = mAutostartMinSecs * 1000;

        mAutostartTimer = new CountDownTimer((long) randomMillis, 100) {

            public void onTick(long millisUntilFinished) {
                displayChronometer(millisUntilFinished);
            }

            public void onFinish() {
                startShooting();
            }
        }.start();

    }

    public void startShooting() {
        setState(STATE_ACTIVE);
        if (mStartSoundEnabled)
            playSound(mStartSoundID, mStartSoundVolumePercent);
        startTimer(System.currentTimeMillis());
    }

    public void ShotWasFired() {
        addShot(System.currentTimeMillis());
    }

    public void stopShooting() {
        stopTimer();
        setState(STATE_FINISHED);
    }

    void startTimer(long currentTimeMillis) {
        mStartTimeMillis = currentTimeMillis;
        // mLastShotTime = 0;
        displayChronometer(0);
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
        if (mShotRecorder != null)
            mShotRecorder.startRecording(currentTimeMillis);
    }

    void stopTimer() {
        if (mShotRecorder != null)
            mShotRecorder.stopRecording();
        mChronometer.stop();
        if (mSaveShotTimes)
            saveShotTimes();
    }

    private void saveShotTimes() {
        // TODO repair broken app when saving files

        showDebug(Log.DEBUG, "saving shot times");

        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
        File file = new File("ShotList-" + dateFormat.format(date) + ".txt");
        String fileName = getActivity().getExternalFilesDir(null) + "/"
                + file.getName();
        if (!mShotArray.isEmpty()) {
            FileWriter fw;
            try {
                showDebug(Log.DEBUG, "opening file " + fileName);
                fw = new FileWriter(fileName);
                BufferedWriter bw = new BufferedWriter(fw);
                for (String aMShotArray : mShotArray) {
                    bw.append(aMShotArray);
                    bw.write(10);
                }
                showDebug(Log.INFO, mShotArray.size() + " shot times saved");
                bw.flush();
                bw.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    void resetTimer() {
        showDebug(Log.INFO, "resetTimer");
        mStartTimeMillis = 0;
        mLastShotTime = 0;
        stopTimer();
        mNumShots = 0;
        mViewNumShots.setText(Integer.toString(mNumShots));
        mShotArray.clear();
        mAdapter.notifyDataSetChanged();
        setButtons();
    }

    void addShot(long currentTimeMillis) {
        if (getState() == STATE_ACTIVE) {
            float ShotTime = (float) (currentTimeMillis - mStartTimeMillis) / 1000f;
            float SplitTime = ShotTime - mLastShotTime;
            long mMinSplitMillis = 80;
            if (SplitTime * 1000f > mMinSplitMillis) {

                ++mNumShots;
                mLastShotTime = ShotTime;
                mShotArray.add(String.format("%1d.  %.3f  +%.3f", mNumShots,
                        ShotTime, SplitTime));
                // do not draw by yourself, because might be called from different thread
                // updateShotScreen();
            } else
                showDebug(Log.DEBUG, "shot skipped after " + SplitTime + " s");
        }
    }

    private void updateShotScreen() {
        // bug: mViewNumShots is not updated on screen
        mViewNumShots.setText(Integer.toString(mNumShots));
        mAdapter.notifyDataSetChanged();
    }

    private String getGunner() {
        return mMainActivity.getGunner();
    }

    public void onGunnerChanged(String newGunner) {
        showDebug(Log.DEBUG, "onGunnerChanged");

        // besser nicht, weil beim Drehen die Buttons falsch sind
        // besser schon, weil sonst die buttons falsch sind nach dem Auswählen eines (neuen) Gunners
        //resetShooting();
        setButtons();
    }

    public int getState() {
        if (mState == null) mState = 0;
        return mState;
    }

    public int setState(int newState) {
        if (mState == null) mState = 0;
        if (newState != mState) changeState(newState);

        return mState;
    }

    private void changeState(int newState) {
        // TODO state-engine
        showDebug(Log.INFO, "changing State from " + mState + " to " + newState);

        mState = newState;

        mViewState.setText(Integer.toString(newState));
        setButtons();

        if (mAutostartTimer != null) mAutostartTimer.cancel();
    }

    void resetState() {
        showDebug(Log.INFO, "resetState");
        setState(STATE_INACTIVE);
        displayChronometer(mAutostartEnabled ? mAutostartMaxSecs : 0);
    }

    private void setButtons() {
        showDebug(Log.INFO, "setButtons " + mMainActivity.getGunner() + " " + getState());

        if (mMainActivity.getGunner().isEmpty()) {
            mBtnStandBy.setEnabled(false);
            mBtnStart.setEnabled(false);
            mBtnShot.setEnabled(false);
            mBtnStop.setEnabled(false);
            mBtnReset.setEnabled(false);
            return;
        }
        switch (getState()) {
            case STATE_READY:
            case STATE_STANDBY:
                mBtnStandBy.setEnabled(false);
                mBtnStart.setEnabled(!mAutostartEnabled);
                mBtnShot.setEnabled(false);
                mBtnStop.setEnabled(false);
                mBtnReset.setEnabled(true);
                break;
            case STATE_ACTIVE:
                mBtnStandBy.setEnabled(false);
                mBtnStart.setEnabled(false);
                mBtnShot.setEnabled(true);
                mBtnStop.setEnabled(true);
                mBtnReset.setEnabled(true);
                break;
            case STATE_FINISHED:
            case STATE_SCORING:
                mBtnStandBy.setEnabled(false);
                mBtnStart.setEnabled(false);
                mBtnShot.setEnabled(false);
                mBtnStop.setEnabled(false);
                mBtnReset.setEnabled(true);
                break;
            default:
                mBtnStandBy.setEnabled(true);
                mBtnStart.setEnabled(false);
                mBtnShot.setEnabled(false);
                mBtnStop.setEnabled(false);
                mBtnReset.setEnabled(false);
                break;
        }

    }

    private void displayChronometer(int seconds) {
        displayChronometer((long) seconds * 1000);

    }

    private void displayChronometer(long millis) {
        Time time = new Time();
        time.set(millis);
        mChronometer.setText(time.format("%M:%S"));
    }

    private int loadSound(int sound) {
        // Load the sound
        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        // mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
        // @Override
        // public void onLoadComplete(SoundPool soundPool, int sampleId,
        // int status) {
        // // loaded = true;
        // }
        // });
        return mSoundPool.load(getActivity().getApplicationContext(),
                R.raw.startsound, 1);
    }

    private void playSound(int soundID, int volumePercent) {
        if (soundID > 0 /* and loaded */) {
            float volume = 0.75f;
            volume = (float) volumePercent / 100;
            volume = Math.max(volume, 0f);
            volume = Math.min(volume, 1f);
            mSoundPool.play(soundID, volume, volume, 1, 0, 1f);

        }
    }

    private void loadPrefs() {
        loadPrefs(PreferenceManager.getDefaultSharedPreferences(getActivity()));
    }

    public void loadPrefs(SharedPreferences prefs) {
        if (prefs == null) {
            showDebug(Log.WARN, "Preferences unavailable, continue with defaults");
            return;
        }

        // Autostart
        mAutostartEnabled = prefs.getBoolean(
                getString(R.string.pref_key_autostart_enabled),
                mAutostartEnabled);
        mAutostartMinSecs = getIntPref(prefs,
                getString(R.string.pref_key_autostart_minsecs),
                mAutostartMinSecs);
        mAutostartMaxSecs = getIntPref(prefs,
                getString(R.string.pref_key_autostart_maxsecs),
                mAutostartMaxSecs);

        // Startsound
        mStartSoundEnabled = prefs.getBoolean("pref_key_startsound_enabled",
                mStartSoundEnabled);
        mStartSoundVolumePercent = getIntPref(prefs,
                getString(R.string.pref_key_startsound_volume),
                mStartSoundVolumePercent);

        // Save ShotList
        mSaveShotTimes = prefs.getBoolean("pref_key_save_shotlist",
                mSaveShotTimes);

        if (mShotRecorder == null)
            startShotRecorder();
        mShotRecorder.loadPrefs(prefs);
    }

    private int getIntPref(SharedPreferences prefs, String key, int value) {
        String s = Integer.toString(value);
        s = prefs.getString(key, s);
        try {
            value = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            value = 0;
        }
        return value;
    }

    public void onShotDetected(long millis) {
        addShot(millis);
        // update the UI using the handler and the runnable
        mHandler.post(updateRunnable);
    }

    private void showDebug(int level, String string) {
        if (mMainActivity != null)
            mMainActivity.showDebug(level, LOG_TAG, string);
    }

    public MainActivity getMainActivity() {
        return mMainActivity;
    }
}
