package kherb64.android.shottimer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

class ShotRecorder1 {
	private static final String LOG_TAG = "ShotRecorder1";
	private String mDirName = null;
	private MediaRecorder mRecorder = null;
	private final int mTargetDir = 2;
	MediaPlayer mPlayer = null;

	public ShotRecorder1() {
		mDirName = getDirName();
		prepareRecording();
	}

	void prepareRecording() {
		stopRecording();

		Log.d(LOG_TAG, "preparing a new MediaRecorder");
		// displayToast("preparing a new MediaRecorder");
		mRecorder = new MediaRecorder();
		mRecorder.reset();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mRecorder.setOutputFile(getFilename());

		Log.d(LOG_TAG, "MediaRecorder successfully prepared");
		// displayToast("MediaRecorder successfully prepared");
	}

	public void startRecording() {
		Log.d(LOG_TAG, "starting MediaRecorder");

		try {
			mRecorder.prepare();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
		}

		try {
			mRecorder.start();
		} catch (IllegalStateException e) {
			Log.e(LOG_TAG, "start() failed");
		}
	}

	void stopRecording() {
		if (mRecorder != null) {
			Log.d(LOG_TAG, "stopping MediaRecorder");
			// displayToast("stopping MediaRecorder");
			try {
				mRecorder.stop();
				mRecorder.reset();
				mRecorder.release();
				mRecorder = null;
			} catch (Exception e) {
				Log.e(LOG_TAG, "stopping Mediarecorder failed");
			}
		}
	}

	private String getDirName() {
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
		Log.d(LOG_TAG, "directory " + dirName + " set");
		displayToast("directory " + dirName + " set");
		return dirName;

	}

	@SuppressLint("SimpleDateFormat")
	private String getFilename() {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
		File file = new File(LOG_TAG + "-" + dateFormat.format(date) + ".3gp");
		return mDirName + file.getName();
	}

	private void displayToast(String string) {
		Toast toast = Toast.makeText(
				((Context) MainActivity.Context).getApplicationContext(),
				string, Toast.LENGTH_SHORT);
		toast.show();
	}

}