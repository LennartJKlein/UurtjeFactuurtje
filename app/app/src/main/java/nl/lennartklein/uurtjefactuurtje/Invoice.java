package nl.lennartklein.uurtjefactuurtje;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Objects;

import static android.content.ContentValues.TAG;

/**
 * An invoice
 */
public class Invoice {
    private double btw;
    private String companyId;
    private String date;
    private String endDate;
    private String invoice_number;
    private String projectId;
    private double totalPrice;
    private String userId;
    private double TAX_RATE = 0.21;

    // Empty constructor for FireBase
    public Invoice() {}

    public Invoice(double btw, String companyId, String date, String endDate,
                   String invoice_number, String projectId, String userId, double totalPrice) {
        this.btw = btw;
        this.companyId = companyId;
        this.date = date;
        this.endDate = endDate;
        this.invoice_number = invoice_number;
        this.projectId = projectId;
        this.totalPrice = totalPrice;
        this.userId = userId;
    }

    public double getBtw() {
        return btw;
    }

    public void setBtw(double btw) {
        this.btw = btw;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getInvoice_number() {
        return invoice_number;
    }

    public void setInvoice_number(String invoice_number) {
        this.invoice_number = invoice_number;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void calculatePrice() {
        DatabaseReference dbWorkMe = PersistentDatabase.getReference().child("work").child(getUserId());
        final DatabaseReference dbWorkThis = dbWorkMe.child(getProjectId());

        dbWorkThis.child("unpaid").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                double totalPrice = 0.00;
                for (DataSnapshot workSnapshot: dataSnapshot.getChildren()) {
                    Work work = workSnapshot.getValue(Work.class);
                    if (work != null) {
                        totalPrice += work.getPrice();
                    }
                }

                double btw = totalPrice * TAX_RATE;
                setBtw(btw);

                setTotalPrice(totalPrice + btw);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

}
