package nl.lennartklein.uurtjefactuurtje;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,GoogleApiClient.OnConnectionFailedListener {

    // Authentication
    private static final int RC_SIGN_IN = 2;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInOptions mGoogleSignInOptions;
    private FirebaseAuth mAuth;
    private DatabaseReference db;
    private DatabaseReference dbUsers;

    // Global references
    Context mContext;
    private SignInButton googleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Global references
        googleButton = findViewById(R.id.action_login_google);
        mContext = this;

        // Authentication
        mAuth = FirebaseAuth.getInstance();

        // Get database references
        db = PersistentDatabase.getReference();
        dbUsers = db.child("users");

        googleButton.setOnClickListener(this);

        mGoogleSignInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleSignInOptions)
                .build();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.action_login_google:
                signIn();
                break;
        }
    }

    private void signIn() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleTask(task);
        }
    }

    private void handleTask(Task<GoogleSignInAccount> task) {
        try {
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount account = task.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);

        } catch (ApiException e) {
            // Google Sign In failed, update UI appropriately
            Log.d("Sign in", "Google sign in failed", e);
            Toast.makeText(this, getResources().getString(R.string.error_no_login), Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Sign in", "signInWithCredential:success");
                            saveUserData();
                            enterApp(true);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Sign in", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.error_no_login), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserData() {
        final String userId = mAuth.getCurrentUser().getUid();
        final String userMail = mAuth.getCurrentUser().getEmail();

        // If new, save the user ID in the database
        dbUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(userId)) {
                    User newUser = new User(
                            userMail,
                            Calendar.getInstance().get(Calendar.YEAR) + "0001",
                            "14");
                    db.child("users").child(userId).setValue(newUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(mContext,
                        getResources().getString(R.string.error_no_projects), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enterApp(boolean isLogged) {
        if (isLogged) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, getResources().getString(R.string.error_no_login), Toast.LENGTH_SHORT).show();
    }

}
