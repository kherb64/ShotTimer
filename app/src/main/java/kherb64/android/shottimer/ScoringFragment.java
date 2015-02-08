package kherb64.android.shottimer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ScoringFragment extends Fragment {

	public ScoringFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scoring, container,
                false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Defined Array values to show in ListView
		String[] mTargetArray = new String[] { "T1", "T2", "T3", "P4", "P5" };

		// Define a new Adapter
		// First parameter - Context
		// Second parameter - Layout for the row
		// Forth - the Array of data
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, mTargetArray);

		// Get ListView object from xml
		ListView listView = (ListView) getActivity().findViewById(
				R.id.listTargets);
		listView.setAdapter(adapter);

	}

}
