package nl.lennartklein.uurtjefactuurtje;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * A page of all the unpaid work of a project
 */
public class ProjectWorkFragment extends Fragment implements View.OnClickListener {

    // Global references
    private Context mContext;
    private Resources res;
    private Button newInvoice;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_work, container, false);

        // Set global references
        mContext = getActivity();
        res = mContext.getResources();

        newInvoice = view.findViewById(R.id.action_create_invoice);
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

    private void createNewInvoice() {
        // TODO: get all the work from 'work'
        // TODO: get current invoice numbering (+1)
        // TODO: calculate the fields for the new invoice
        // TODO: add invoice to the database (with correct id's)
        // TODO: add invoice ID to all work entries (and tick as invoiced)
        // TODO: open a fragment with the info of the invoice

        // TODO: share? on click:
        // TODO: use information of invoice (class object by ID) for creating a document

        Toast.makeText(mContext, res.getString(R.string.generating_invoice), Toast.LENGTH_SHORT).show();

        Invoice invoice = new Invoice();
        invoice.setBtw(178.17);
        invoice.setCompanyId("SLggEREWROWEHERWHWET");
        invoice.setDate("2017-09-10");
        invoice.setEndDate("2017-10-10"); // c.add(Calendar.DATE, 5);
        invoice.setInvoice_number("20170034");
        invoice.setProjectId("KpHHIHIHIHIHIHIHIH");
        invoice.setSenderId("Ghsdgshkwerwerw");
        invoice.setTotalPrice(848.44);
        invoice.setUserId("sdflkjsskdjbdfg");

        invoice.createFile(mContext);

    }

}
