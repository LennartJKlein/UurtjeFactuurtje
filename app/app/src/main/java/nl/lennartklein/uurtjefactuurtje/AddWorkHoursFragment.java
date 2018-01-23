package nl.lennartklein.uurtjefactuurtje;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A form for adding timed work to a project
 */
public class AddWorkHoursFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_work_hours, container, false);

        return view;
    }

}
