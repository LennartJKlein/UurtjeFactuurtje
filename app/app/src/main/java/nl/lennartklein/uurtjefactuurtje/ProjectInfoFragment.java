package nl.lennartklein.uurtjefactuurtje;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Settings page with explanation about the app
 */
public class ProjectInfoFragment extends Fragment implements View.OnClickListener {

    // Global references
    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_info, container, false);

        // Set global references
        mContext = getActivity();

        // TODO: load information on project

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.action_delete:
                verifyDelete();
                break;
        }
    }

    public void verifyDelete() {
        //TODO: verify
        deleteProject();
    }

    public void deleteProject() {
        // TODO: delete from database
    }
}
