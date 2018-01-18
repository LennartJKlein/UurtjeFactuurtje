package nl.lennartklein.uurtjefactuurtje;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Settings page with a form for user info
 */
public class SettingsUserFragment extends Fragment implements View.OnClickListener {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser current_user;

    // Global references
    private Context mContext;
    private Button actionSignOut;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_user, container, false);

        // Set global references
        mContext = getActivity();
        actionSignOut = view.findViewById(R.id.action_logout);
        actionSignOut.setOnClickListener(this);

        setAuth();

        return view;
    }

    private void setAuth() {
        auth = FirebaseAuth.getInstance();
        current_user = auth.getCurrentUser();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.action_logout:
                signOut();
                break;
        }
    }

    private void signOut() {
        auth.signOut();
        getActivity().getFragmentManager().popBackStack();
        getActivity().finish();
        startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
