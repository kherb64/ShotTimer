package kherb64.android.shottimer;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends FragmentActivity implements
        ActionBar.TabListener,
        GunnersFragment.OnGunnerSelectedListener {

    private static final int RESULT_SETTINGS = 1;
    private static final String LOG_TAG = "Main";

    public static Object Context;
    public SettingsFragment mSettingsFrag;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private GunnersFragment mGunnersFrag;
    private ShootingFragment mShootingFrag;
    private ScoringFragment mScoringFrag;
    private AudioFragment mAudioFrag;

    private String mGunner;
    private SharedPreferences mSharedPrefs;
    private int mLogLevel = Log.DEBUG;

    // OnSharedPreferenceChangeListener mPrefsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showDebug(Log.VERBOSE, "onCreate");
        Context = getBaseContext();
        setContentView(R.layout.activity_main);
        // mMainActivity = this;

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager
                .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        actionBar.setSelectedNavigationItem(position);
                    }
                });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(actionBar.newTab()
                    .setText(mSectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(this));
        }

        loadPrefs();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        showDebug(Log.VERBOSE, "onRestart");
    }

    @Override
    public void onStart() {
        super.onStart();
        showDebug(Log.VERBOSE, "onStart");
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
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
        String Gunner = savedInstanceState.getString("mGunner");

        showDebug(Log.DEBUG, "class members " + Gunner + " restored");

        setGunner(Gunner);

        // showDebug("class members " + mGunner + " set");
    }

    private void saveMyMembers(Bundle outState) {
        outState.putString("mGunner", mGunner);
        showDebug(Log.DEBUG, "class members " + mGunner + " saved");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab,
                              FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab,
                                FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab,
                                FragmentTransaction fragmentTransaction) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_addGunner:
                addGunner();
                return true;
            case R.id.action_settings:
                openSettings();
                return true;
            case R.id.action_about:
                openAbout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openAbout() {
        // TODO Auto-generated method stub

    }

    private void addGunner() {
        // TODO Auto-generated method stub
        // AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // builder.setTitle(R.string.action_addGunner)
        // .setMessage(R.string.under_development)
        // .setPositiveButton(R.string.ok,
        // new DialogInterface.OnClickListener() {
        // public void onClick(DialogInterface dialog, int id) {
        // dialog.cancel();
        // }
        // });
        // AlertDialog alert = builder.create();
        // alert.show();

        // Intent intent = new Intent(this, GunnerActivity.class);
        // // EditText editText = (EditText) findViewById(R.id.edit_message);
        // // String message = editText.getText().toString();
        // // intent.putExtra(EXTRA_MESSAGE, message);
        // startActivity(intent);
        // TODO receive newly created gunner and select it, if any

        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, R.string.under_development,
                Toast.LENGTH_SHORT);
        toast.show();
    }

    private void openSettings() {

        Intent i = new Intent(this, SettingsActivity.class);
        startActivityForResult(i, RESULT_SETTINGS);

        // // TODO avoid overlapping settings fragment
        // if (mSettingsFrag == null) {
        // // Display the fragment as the main content.
        // mSettingsFrag = new SettingsFragment();
        // getFragmentManager().beginTransaction()
        // .add(android.R.id.content, mSettingsFrag).commit();
        // // TODO .addToBackStack();
        // }
        // // TODO hide SettingsFragment

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SETTINGS:
                loadPrefs();
                break;

        }

    }

    @Override
    public void onGunnerSelected(String string) {
        setGunner(string);
        if (mShootingFrag != null) mShootingFrag.resetShooting();
    }

    public void clearGunner() {
        setGunner("");
    }

    public String getGunner() {
        if (mGunner == null) mGunner = "";
        return mGunner;
    }

    String setGunner(String newGunner) {
        if (mGunner == null) mGunner = "";
        if (!mGunner.equals(newGunner)) {
            // showDebug("changing gunner from " + mGunner + " to " + string);
            changeGunner(newGunner);
        }
        return mGunner;
    }

/*    public void onShootingSelected(int position) {
        // TODO Auto-generated method stub
    }
*/

    public void changeGunner(String newGunner) {
        if (newGunner == null) newGunner = "";

        showDebug(Log.DEBUG, "changing Gunner from " + mGunner + " to " + newGunner);
        mGunner = newGunner;
        try {
            getActionBar().setTitle(mGunner);
        }
        catch (Exception e) {
            showDebug(Log.WARN, "could not set ActionBarTitle");
        }

        if (mShootingFrag != null) mShootingFrag.onGunnerChanged(mGunner);
    }

    public void resetShooting(View v) {
        if (mShootingFrag != null)
            mShootingFrag.resetShooting();

    }

    public void standBy(View v) {
        if (mShootingFrag != null)
            mShootingFrag.standBy();

    }

    public void startShooting(View v) {
        if (mShootingFrag != null)
            mShootingFrag.startShooting();

    }

    public void ShotWasFired(View v) {
        if (mShootingFrag != null)
            mShootingFrag.ShotWasFired();

    }

    public void stopShooting(View v) {
        if (mShootingFrag != null)
            mShootingFrag.stopShooting();

    }

    public void loadSamples(View v) {
        if (mAudioFrag != null)
            mAudioFrag.loadSamples();

    }

    private void showDebug(int level, String string) {
        showDebug(level, LOG_TAG, string);
    }

    public void showDebug(int level, String tag, String string) {
        // Logs if actual mPriority is greater or equals priority

        if (mLogLevel <= level)
            Log.println(level, tag, string);
        /* Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                string, Toast.LENGTH_SHORT);
        toast.show(); */
    }


    private void loadPrefs() {
        // loads preferences into mSharedPref
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (mShootingFrag != null)
            mShootingFrag.loadPrefs(mSharedPrefs);

    }

    public void setFragmentHandle(ShootingFragment shootingFragment) {
        mShootingFrag = shootingFragment;
    }

    public interface OnGunnerChangedListener {
        public void onGunnerChanged(String newGunner);
    }


    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public DummySectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_dummy,
                    container, false);
            TextView dummyTextView = (TextView) rootView
                    .findViewById(R.id.section_label);
            dummyTextView.setText(Integer.toString(getArguments().getInt(
                    ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        FragmentManager mFm;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFm = fm;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            Fragment fragment;
            switch (position) {
                case 0:
                    if (mGunnersFrag == null)
                        mGunnersFrag = new GunnersFragment();
                    return mGunnersFrag;
                case 1:
                    if (mShootingFrag == null)
                        mShootingFrag = new ShootingFragment();
                    return mShootingFrag;
                case 2:
                    if (mScoringFrag == null)
                        mScoringFrag = new ScoringFragment();
                    return mScoringFrag;
                case 3:
                    if (mAudioFrag == null)
                        mAudioFrag = new AudioFragment();
                    return mAudioFrag;
                default:
                    fragment = new DummySectionFragment();
                    break;
            }
            Bundle args = new Bundle();
            args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            // Show total number of pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_activity_gunner_selection);
                case 1:
                    return getString(R.string.title_activity_shooting);
                case 2:
                    return getString(R.string.title_activity_scoring);
                case 3:
                    return getString(R.string.title_activity_audio);
                default:
                    return getString(R.string.title_section_Dummy).toUpperCase(l);
            }
        }
    }
}