package wei.shawn.tmdb_demo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class TabFragment extends Fragment {

    int index;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(R.layout.movie_list, container, false);
        Bundle args = getArguments();
        index = (int)args.get("index");

        ListView listView = rootView.findViewById(R.id.movie_list);
        listView.setAdapter(new MovieListAdapter(index));

        return rootView;
    }
}
