package nl.lennartklein.uurtjefactuurtje;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * A page of all the unpaid work of a project
 */
public class ProjectWorkFragment extends Fragment implements View.OnClickListener {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // Global references
    private Context mContext;
    private Resources res;
    private Button newInvoice;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbUsersMe;
    private DatabaseReference dbInvoicesMe;

    // Data
    Project project;
    User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_work, container, false);

        setAuth();

        // Set database references
        db = PersistentDatabase.getReference();
        dbUsersMe = db.child("users").child(currentUser.getUid());
        dbInvoicesMe = db.child("invoices").child(currentUser.getUid());

        // Set UI references
        mContext = getActivity();
        res = mContext.getResources();
        newInvoice = view.findViewById(R.id.action_create_invoice);

        // Get data
        project = (Project) getArguments().getSerializable("PROJECT");
        getUserInfo();

        newInvoice.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.action_create_invoice:
                createNewInvoice();
                break;
        }
    }

    private void setAuth() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }

    private void getUserInfo() {
        dbUsersMe.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(mContext,
                        getResources().getString(R.string.error_no_projects), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNewInvoice() {
        // TODO: get all the work from 'work'
        // TODO: calculate the fields for the new invoice
        // TODO: add invoice ID to all work entries (and tick as invoiced)

        Toast.makeText(mContext, res.getString(R.string.generating_invoice), Toast.LENGTH_SHORT).show();

        // Add one to invoiceNumber of user
        long newInvoiceNumber = Long.valueOf(user.getInvoiceNumber()) + 1;
        dbUsersMe.child("invoiceNumber").setValue(String.valueOf(newInvoiceNumber));

        // Create new invoice object
        Invoice invoice = new Invoice();
        invoice.setDate(getDateToday(0));
        invoice.setEndDate(getDateToday(Integer.valueOf(user.getPayDue())));
        invoice.setInvoice_number(user.getInvoiceNumber() );
        invoice.setUserId(currentUser.getUid());
        invoice.setCompanyId(project.getCompanyId());
        invoice.setProjectId(project.getId());
        invoice.setBtw(178.17);
        invoice.setTotalPrice(848.44);

        invoice.createFile(mContext);

        insertIntoDatabase(invoice);
    }

    public String getDateToday(int extraDays) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        c.add(Calendar.DATE, extraDays);
        return df.format(c.getTime());
    }

    public void insertIntoDatabase(Invoice invoice) {
        dbInvoicesMe.push().setValue(invoice);
    }

}
