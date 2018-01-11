package nl.lennartklein.uurtjefactuurtje;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * See relations of this user
 */
public class RelationsFragment extends Fragment {

    // Global references
    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_relations, container, false);

        // Set global references
        mContext = getActivity();

        return view;
    }
}
