////////////////////////////////////////////////////////////////////////////////
// Title        AddCostActivity
// Parent       OverviewFragment
//
// Date         February 1 2018
// Author       Lennart J Klein  (info@lennartklein.nl)
// Project      UurtjeFactuurtje
// Assignment   App Studio, University of Amsterdam
////////////////////////////////////////////////////////////////////////////////

package nl.lennartklein.uurtjefactuurtje;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.weiwangcn.betterspinner.library.BetterSpinner;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * A dialog with a form to add a cost
 */

public class AddCostActivity extends AppCompatActivity implements View.OnClickListener {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbCostsMe;
    private DatabaseReference dbCompaniesMe;
    private StorageReference sb;
    private StorageReference sbCostsMe;

    // UI references
    private Context mContext;
    private Resources res;
    private EditText fieldDescription;
    private BetterSpinner fieldRelations;
    private EditText fieldInvoiceNr;
    private TextView fieldDate;
    private EditText fieldEuros;
    private EditText fieldCents;
    private EditText fieldFile;
    private ImageButton actionAddCompany;
    private Button actionAdd;
    private Button actionCancel;

    // Data
    private List<Company> relations;
    private List<String> relationNames;
    private ArrayAdapter<String> relationsAdapter;
    private String date;
    private int year;
    private int month;
    private int day;
    final private double TAX_RATE = 0.21;
    private FilePickerDialog filePicker;
    private String[] filePaths;
    private String filePath;

    // Constants
    public static final int PERMISSIONS_REQUEST_CODE = 0;
    DialogProperties fileProperties = new DialogProperties();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cost);

        setAuth();

        setReferences();

        // Set UI references
        mContext = this;
        res = mContext.getResources();
        fieldDescription = findViewById(R.id.field_description);
        fieldRelations = findViewById(R.id.field_company);
        fieldDate = findViewById(R.id.field_date);
        fieldInvoiceNr = findViewById(R.id.field_invoice_nr);
        fieldEuros = findViewById(R.id.field_price_euros);
        fieldCents = findViewById(R.id.field_price_cents);
        fieldFile = findViewById(R.id.field_file);
        actionAddCompany = findViewById(R.id.action_create_company);
        actionAdd = findViewById(R.id.action_add);
        actionCancel = findViewById(R.id.action_cancel);

        // Set click listeners
        fieldDate.setOnClickListener(this);
        fieldFile.setOnClickListener(this);
        actionAddCompany.setOnClickListener(this);
        actionAdd.setOnClickListener(this);
        actionCancel.setOnClickListener(this);

        initiateDatePicker();

        initiateFilePicker();

        getRelations();
    }

    /**
     * Sets the FireBase authentication and current user
     */
    private void setAuth() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }

    /**
     * Sets the FireBase references
     */
    private void setReferences() {
        db = PersistentDatabase.getReference();
        dbCompaniesMe = db.child("companies").child(currentUser.getUid());
        dbCostsMe = db.child("costs").child(currentUser.getUid());
        sb = FirebaseStorage.getInstance().getReference();
        sbCostsMe = sb.child("costs").child(currentUser.getUid());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.field_date:
                showDatePicker();
                break;
            case R.id.field_file:
                checkPermissionsAndOpenFilePicker();
                break;
            case R.id.action_create_company:
                startAddCompanyActivity();
                break;
            case R.id.action_add:
                validateFields();
                break;
            case R.id.action_cancel:
                closeActivity();
                break;
        }
    }

    /**
     * Fetches all relations of this user
     */
    private void getRelations() {
        relations = new ArrayList<>();
        relationNames = new ArrayList<>();
        relationsAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_dropdown_item_1line, relationNames);
        fieldRelations.setAdapter(relationsAdapter);

        dbCompaniesMe.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot companySnapshot: dataSnapshot.getChildren()) {
                    Company company = companySnapshot.getValue(Company.class);

                    if (company != null) {
                        company.setId(companySnapshot.getKey());

                        relations.add(company);

                        if (company.getName() != null) {
                            relationNames.add(company.getName());
                        }
                    }
                }

                relationsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Data", String.valueOf(databaseError));
            }
        });
    }

    private void initiateDatePicker() {
        Calendar cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);
    }

    private void showDatePicker() {
        DatePickerDialog picker = new DatePickerDialog(
                mContext,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        year = i;
                        month = i1;
                        day = i2;
                        setDateField();
                    }
                },
                year, month, day);
        picker.show();
    }

    private void setDateField() {
        date = year + "-" + String.format("%02d", (month + 1)) + "-" + String.format("%02d", day);
        fieldDate.setText(date);
    }

    private void initiateFilePicker() {
        fileProperties.selection_mode = DialogConfigs.SINGLE_MODE;
        fileProperties.selection_type = DialogConfigs.FILE_SELECT;
        fileProperties.root = new File(Environment.getExternalStorageDirectory().getPath());
        fileProperties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        fileProperties.offset = new File(DialogConfigs.DEFAULT_DIR);
        fileProperties.extensions = new String[]{"pdf", "doc", "docx", "xls", "xlsx", "csv"};

        filePicker = new FilePickerDialog(this, fileProperties);
        filePicker.setTitle(R.string.title_pick_file);
    }

    private void checkPermissionsAndOpenFilePicker() {
        String permission = android.Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                Toast.makeText(mContext, res.getString(R.string.error_no_permission),
                        Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_CODE);
            }
        } else {
            showFilePicker();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showFilePicker();
                } else {
                    Toast.makeText(mContext, res.getString(R.string.error_no_permission),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void showFilePicker() {
        filePicker.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                filePaths = files;
                filePath = files[0];
                setFileField(filePath);
            }
        });
        filePicker.show();
    }

    private void setFileField(String filePath) {
        String filename = filePath.substring(filePath.lastIndexOf("/")+1);
        fieldFile.setText(filename);
    }

    public void closeActivity() {
        finish();
    }


    /**
     * Validates the user's input
     */
    public void validateFields() {

        // Reset previous errors
        fieldDescription.setError(null);
        fieldRelations.setError(null);
        fieldDate.setError(null);
        fieldInvoiceNr.setError(null);
        boolean cancel = false;
        View focusView = null;

        // Store values
        final String description = fieldDescription.getText().toString();
        final String companyName = fieldRelations.getText().toString();
        final String date = fieldDate.getText().toString();
        final String invoiceNr = fieldInvoiceNr.getText().toString();
        final String priceEuros = fieldEuros.getText().toString();
        final String priceCents = fieldCents.getText().toString();
        final String price = priceEuros + "." + priceCents;

        // Check for a valid inputs
        if (TextUtils.isEmpty(description)) {
            fieldDescription.setError(getString(R.string.error_field_required));
            focusView = fieldDescription;
            cancel = true;
        }
        if (TextUtils.isEmpty(companyName)) {
            fieldRelations.setError(getString(R.string.error_field_required));
            focusView = fieldRelations;
            cancel = true;
        }
        if (TextUtils.isEmpty(date)) {
            fieldDate.setError(getString(R.string.error_field_required));
            focusView = fieldDate;
            cancel = true;
        }
        if (TextUtils.isEmpty(invoiceNr)) {
            fieldInvoiceNr.setError(getString(R.string.error_field_required));
            focusView = fieldInvoiceNr;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            insertInDatabase(description, companyName, date, invoiceNr, price, filePath);
        }
    }

    /**
     * Inserts a new cost in the database
     */
    public void insertInDatabase(String description, String companyName, String date,
                                 String invoiceNr, String price, String filePath) {

        final DatabaseReference dbCostNew = dbCostsMe.push();

        Company company = fetchCompany(companyName);

        // Build the cost object
        Cost cost = new Cost();
        cost.setPrice(Double.parseDouble(price));
        cost.setBtw(Math.round((cost.getPrice() * TAX_RATE) * 100.0) / 100.0);
        cost.setPrice(Double.parseDouble(price) + cost.getBtw());
        cost.setCompanyId(company.getId());
        cost.setCompanyName(company.getName());
        cost.setDate(date);
        cost.setDescription(description);
        cost.setInvoiceNr(invoiceNr);
        cost.setUserId(currentUser.getUid());

        dbCostNew.setValue(cost);

        // Upload the attachment
        uploadFile(filePath, dbCostNew);

        closeActivity();
    }

    public Company fetchCompany(String companyName) {
        if (!companyName.equals("")) {
            int companyPosition = relationNames.indexOf(companyName);
            return relations.get(companyPosition);
        } else {
            return null;
        }
    }

    public void uploadFile(String filePath, final DatabaseReference dbEntity) {
        Log.d("attachment", filePath);

        if (filePath != null && !filePath.equals("")) {
            Uri file = Uri.fromFile(new File(filePath));
            Log.d("attachment", dbEntity.getKey());
            UploadTask uploadTask = sbCostsMe.child(dbEntity.getKey()).putFile(file);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    dbEntity.child("filePath").setValue(downloadUrl);
                    Log.d("downloadUrl", "" + downloadUrl);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(mContext, res.getString(R.string.error_no_upload)  + exception,
                            Toast.LENGTH_LONG).show();
                    Log.d("attachment", res.getString(R.string.error_no_upload)  + exception);
                }
            });
        }
    }

    /**
     * Opens a dialog fragment to add a company
     */
    private void startAddCompanyActivity() {
        Intent addCompanyIntent = new Intent(mContext, AddCompanyActivity.class);
        startActivity(addCompanyIntent);
    }
}
