package kherb64.android.shottimer;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class GunnersFragment extends Fragment {
	private OnGunnerSelectedListener mCallback;

    public GunnersFragment () {
    }

	// Container Activity must implement this interface
	public interface OnGunnerSelectedListener {
		public void onGunnerSelected(String string);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (OnGunnerSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnGunnerSelectedListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gunners, container,
                false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// setListAdapter(new ArrayAdapter<String>(getActivity(),
		// android.R.layout.simple_list_item_1, Cheeses.sCheeseStrings));

		// Defined Array values to show in ListView
		final String[] mNameArray = new String[] { "Günter", "Gast 1",
				"Gast 2", "Henry", "Herbert", "Karl", "Leo I", "Leo II",
				"Martin", "Mike" };

		// Define a new Adapter
		// First parameter - Context
		// Second parameter - Layout for the row
		// Forth - the Array of data
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_activated_1, mNameArray);

		// Get ListView object from xml
		ListView listView = (ListView) getActivity().findViewById(
				R.id.listGunners);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				mCallback.onGunnerSelected(mNameArray[position]);
			}

		});
	}

}
