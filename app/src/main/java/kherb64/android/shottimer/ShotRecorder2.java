package kherb64.android.shottimer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.uol.aig.fftpack.RealDoubleFFT;

class ShotRecorder2 {
    private static final String LOG_TAG = "ShotRecorder2";
    private final int mTargetDir = 2;
    // MediaPlayer mPlayer = null;
    private final int mFrequency = 11025;
    private final int mChannelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    private final boolean mMakeFFT = false;
    private AudioRecord mRecorder = null;
    private int mAudioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private int mBufferSize;
    private int mBlockSize = 1024;
    private boolean mIsRecording = false;
    private RealDoubleFFT mTransformer = null;
    private boolean mSaveSamples = false;
    private ShootingFragment ShootingFrag = null;
    private long mStartMillis;

    private List<Short> mSData = new ArrayList<Short>();
    // private List<Double> mFFTData = new ArrayList<Double>();

    private double mFFTMeanValMin = 3d;
    private double mFFTSumValMin = 2000d;
    private double mFFTMeanMinValMin = 3d;
    private double mFFTSumMinValMin = 1000d;

    private MainActivity mMainActivity;

    public ShotRecorder2() {
    }

    public void setShootingFrag(ShootingFragment shootingFrag) {
        ShootingFrag = shootingFrag;
        mMainActivity = ShootingFrag.getMainActivity();
    }

    public void prepareRecording(ShootingFragment shootingFrag) {
        ShootingFrag = shootingFrag;
        prepareRecording();
    }

    public void prepareRecording() {
        stopRecording();

        // runtime initialization
        loadPrefs();

        // showDebug(Log.DEBUG, "preparing a new AudioRecord");

        mBufferSize = AudioRecord.getMinBufferSize(mFrequency,
                mChannelConfiguration, mAudioEncoding);

        mRecorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                mFrequency, mChannelConfiguration, mAudioEncoding, mBufferSize);
        if (mRecorder.getState() == 0) {
            showDebug(Log.DEBUG, "stopping AudioRecord in wrong state " + Integer.toString(mRecorder.getState()));
            mRecorder.release();
            mRecorder = null;
        }

        mTransformer = new RealDoubleFFT(mBlockSize);

        mSData.clear();
        // mFFTData.clear();

/*
        showDebug(Log.DEBUG,
                String.format("FFT minimun values: %.3f", mFFTMeanValMin) + " "
                        + String.format("%.3f", mFFTSumValMin) + " "
                        + String.format("%.3f", mFFTMeanMinValMin) + " "
                        + String.format("%.3f", mFFTSumMinValMin)
        );
*/

        showDebug(Log.DEBUG,
                "AudioRecord successfully prepared: "
                        + Integer.toString(mFrequency) + " Hz sampling rate, "
                        + Integer.toString(mBufferSize) + " Bytes buffer size"
        );
    }

    public void startRecording(long millis) {
        mStartMillis = millis;
        showDebug(Log.DEBUG, "starting AudioRecord");

        // final String filePath = getFilename();
        // showDebug(Log.DEBUG, "Filepath " + filePath + " set");
        mIsRecording = true;
        if (mRecorder != null) {
            mRecorder.startRecording();

            // separate thread for analyzing audio data
            Thread mRecordingThread = new Thread(new Runnable() {

                public void run() {
                    // if (mSaveSamples)
                    // writeAudioDataToFile(filePath);
                    // else
                    if (mMakeFFT) analyzeAudioData();
                }
            }, LOG_TAG + " Thread");
            mRecordingThread.start();
        }
    }

    public void stopRecording() {
        mIsRecording = false;
        if (mRecorder != null) {
            showDebug(Log.DEBUG, "stopping AudioRecord");
            mRecorder.stop();
            showDebug(Log.DEBUG, "AudioRecord stopped");
            if (mSaveSamples)
                saveSamples();
            mSData.clear(); // vorher warten wg buffered stream?
            // saveFFTDatalist(); unneccesary to collect ALL FFT data
            // mFFTData.clear();
        }
    }

    private void loadPrefs() {
        loadPrefs(PreferenceManager
                .getDefaultSharedPreferences(((Context) MainActivity.Context)));
    }

    public void loadPrefs(SharedPreferences prefs) {
        if (prefs == null) {
            showDebug(Log.WARN, "Preferences unavailable, continue with defaults");
            return;
        }

        mSaveSamples = prefs.getBoolean("pref_key_save_samples", mSaveSamples);

        mFFTMeanValMin = (double) getIntPref(prefs, "pref_key_FFT_MeanValMin",
                (int) mFFTMeanValMin);
        mFFTSumValMin = (double) getIntPref(prefs, "pref_key_FFT_SumValMin",
                (int) mFFTSumValMin);
        mFFTMeanMinValMin = (double) getIntPref(prefs,
                "pref_key_FFT_MeanMinValMin", (int) mFFTMeanMinValMin);
        mFFTSumMinValMin = (double) getIntPref(prefs,
                "pref_key_FFT_SumMinValMin", (int) mFFTSumMinValMin);

    }

    private void writeAudioDataToFile(String filePath) {
        // Write the output audio in byte
        short sData[] = new short[mBlockSize];
        int totalSamples = 0;

        FileOutputStream fos = null;
        DataOutputStream os = null;
        try {
            showDebug(Log.DEBUG, "opening file " + filePath);
            fos = new FileOutputStream(filePath);
            os = new DataOutputStream(new BufferedOutputStream(fos));
            assert os != null;
            showDebug(Log.DEBUG, "file " + filePath + " opened");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (mIsRecording) {
            // gets the voice output from microphone to byte format

            // mRecorder.read(sData, 0, mBufferSize/2);
            int bufferReadResult = mRecorder.read(sData, 0, mBlockSize);
            if (bufferReadResult < 0) {
                break;
            }
            showDebug(Log.DEBUG, bufferReadResult + " shorts sampled");
            try {
                for (int i = 0; i < bufferReadResult; i++) {
                    os.writeShort(sData[i]); // leads to bigendian
                }
                totalSamples += bufferReadResult;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            showDebug(Log.DEBUG, "total " + totalSamples + " samples written to file");
            assert fos != null;
            fos.close();
            showDebug(Log.DEBUG, "file " + filePath + " closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void analyzeAudioData() {
        // analyzes audio data
        short sData[] = new short[mBlockSize];
        double[] toTransform = new double[mBlockSize];
        int totalSamples = 0;
        boolean shotDetected;

        while (mIsRecording & mRecorder != null) {
            int bufferReadResult = mRecorder.read(sData, 0, mBlockSize);
            if (bufferReadResult < 0) // error has happened
                break;
            // showDebug(Log.DEBUG, Integer.toString(bufferReadResult) +
            // " samples read");
            try {
                for (int i = 0; i < bufferReadResult; i++) {
                    // signed 16 bit
                    toTransform[i] = (double) sData[i] / 32768.0;
                    mSData.add(sData[i]);
                }
                // showDebug(Log.DEBUG, "Samples");
                // analyzeFrequencies(toTransform);

                // mTransformer.ft(toTransform);
                // showDebug(Log.DEBUG, Integer.toString(bufferReadResult)
                // + " samples transformed");

                // showDebug(Log.DEBUG, "FFT");
                shotDetected = analyzeFrequencies(toTransform);
                if (shotDetected) {
                    showDebug(Log.DEBUG,
                            "Shot detected: " + Integer.toString(totalSamples)
                                    + " + "
                                    + Integer.toString(bufferReadResult)
                                    + " samples "
                    );
                    long shotMillis = mStartMillis + totalSamples * 1000
                            / mFrequency;
                    ShootingFrag.onShotDetected(shotMillis);
                    shotDetected = false;
                }

                totalSamples += bufferReadResult;

            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }

        showDebug(Log.DEBUG, "total " + totalSamples + " samples processed");

    }

    private boolean analyzeFrequencies(double[] fftData) {
        // TODO weiter verbessern.
        // laut geht schon, zB klatschen aber scrheien soll nicht gehen
        double meanVal;
        double meanMinVal;
        double sumVal = 0d;
        double sumMinVal = 0d;
        double minVal = 0.3d; // minimum value
        int numMin = 0; // number of samples over minimum value

        for (double aFftData : fftData) {
            // mFFTData.add(fftData[i]);
            double d = Math.abs(aFftData);
            sumVal += d;
            if (aFftData > minVal) {
                sumMinVal += d;
                numMin += 1;
            }
        }
        meanVal = sumVal / fftData.length;
        meanMinVal = sumMinVal;
        if (numMin > 0)
            meanMinVal /= numMin;

        if (meanVal > mFFTMeanValMin && sumVal > mFFTSumValMin
                && meanMinVal > mFFTMeanMinValMin
                && sumMinVal > mFFTSumMinValMin) {
            showDebug(Log.DEBUG,
                    String.format("FFT data: %.3f", meanVal) + " "
                            + String.format("%.3f", sumVal) + " "
                            + String.format("%.3f", meanMinVal) + " "
                            + String.format("%.3f", sumMinVal)
            );
            return true;
        }
        return false;
    }

    private void saveSamples() {
        // saves samples as raw pcm data in file

        // save when avail
        if (!mSData.isEmpty()) {
            // showDebug(Log.DEBUG, "saving samples");

            // get unique filename
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
            File file = new File(getMyDirName() + "Samples-" + dateFormat.format(date) + ".pcm");

            FileOutputStream fos = null;
            DataOutputStream os = null;
            try {
                // showDebug(Log.DEBUG, "opening file " + fileName);
                fos = new FileOutputStream(file.getPath());
                os = new DataOutputStream(new BufferedOutputStream(fos));
                // showDebug(Log.DEBUG, "file " + fileName + " opened");
                for (Short aMSData : mSData) {
                    os.writeShort(aMSData);
                }
                fos.close();
                // showDebug(Log.DEBUG, "file " + fileName + " closed");
                showDebug(Log.DEBUG, mSData.size() + " samples saved to " + file.getPath());
                makeFileDiscoverable(file, (Context) MainActivity.Context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void makeFileDiscoverable(File file, Context context) {
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(file)));
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

    @SuppressLint("SimpleDateFormat")
    private String getFilename() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
        File file = new File(LOG_TAG + "-" + dateFormat.format(date) + ".pcm");
        return getMyDirName() + file.getName();
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

    private void showDebug(int level, String string) {
        if (mMainActivity != null)
            mMainActivity.showDebug(level, LOG_TAG, string);
    }


    // Caller must implement this interface
    public interface OnShotDetectedListener {
        // is called when a shot has been detected millis is the time in
        // milliseconds passed since startrecording
    }

}