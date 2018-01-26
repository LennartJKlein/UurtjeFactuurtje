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
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Creates a PDF for an invoice (containing necessary info for accountancy)
 */

public class GeneratePdf {

    private Context mContext;
    private Invoice invoice;
    private User user;
    private Company receiver;
    private Project project;
    private List<Work> works;
    private String filepath;

    // Data / template
    private Resources res;
    private String title;
    private String labelDate;
    private String labelInvoice_number;
    private String labelDescription;
    private String labelHours;
    private String labelPrice;
    private String labelSubtotal;
    private String labelBtw;
    private String labelTotalPrice;
    private String disclaimer;
    private DecimalFormat currency = new DecimalFormat("0.00");

    public GeneratePdf(Context mContext, Invoice invoice) {
        this.mContext = mContext;
        this.invoice = invoice;
        this.res = mContext.getResources();
        this.works = new ArrayList<>();
    }

    /**
     * Create a file of this invoice
     */
    public void createFile() {
        if (isExternalStorageWritable()) {
            getDataAndBuild();
        } else {
            Log.d("Data", "Cannot write to device storage.");
            Toast.makeText(mContext,
                    res.getString(R.string.error_no_permission),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void getDataAndBuild() {
        DatabaseReference db = PersistentDatabase.getReference();
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {
                String userId = invoice.getUserId();
                String companyId = invoice.getCompanyId();
                String projectId = invoice.getProjectId();

                // Fetch related user (sender), receiver and project
                user = data.child("users").child(userId).getValue(User.class);
                receiver = data.child("companies").child(userId).child(companyId).getValue(Company.class);
                project = data.child("projects").child(userId).child(projectId).getValue(Project.class);

                // Fetch all work for this project
                DataSnapshot dataWorks = data.child("work").child(userId).child(projectId).child("unpaid");
                for (DataSnapshot workSnapshot : dataWorks.getChildren()) {
                    Work work = workSnapshot.getValue(Work.class);
                    if (work != null) {
                        works.add(work);
                    }
                }

                // Build the document now
                buildDocument();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    /**
     * Create a document for this invoice
     * @return string of the filepath
     */
    private void buildDocument() {

        filepath = getInvoiceStoragePath();

        getTemplate();

        // Generate the document
        Document doc = new Document();
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(filepath));

            doc.open();
            writeSalutation(doc);
            writeSender(doc);
            writeInvoiceDetails(doc);
            writeWorkTable(doc);
            writeFooter(doc);
            doc.close();

            setWorkPaid();

            openFile();

        } catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(mContext,
                    res.getString(R.string.error_no_file_made), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private String getInvoiceStoragePath() {
        String folder_main = "UurtjeFactuurtje";
        String root = Environment.getExternalStorageDirectory() +  "/" + folder_main;

        String filename = "#" + invoice.getInvoice_number() + " " + receiver.getName() + ".pdf";
        String filepath = root + "/" + filename;

        File dir = new File(root);
        if (!dir.mkdirs()) {
            Log.d("File", "Directories not created");
        }

        return filepath;
    }

    private void getTemplate() {
        title = res.getString(R.string.invoice);
        labelDate = res.getString(R.string.label_date);
        labelInvoice_number = res.getString(R.string.label_invoice_number);
        labelDescription = res.getString(R.string.description);
        labelHours = res.getString(R.string.hours);
        labelPrice = res.getString(R.string.price);
        labelSubtotal = res.getString(R.string.label_subtotal);
        labelBtw = res.getString(R.string.label_btw);
        labelTotalPrice = res.getString(R.string.label_total_price);
        disclaimer = res.getString(R.string.disclaimer,
                user.getPayDue(), user.getBank(), user.getName());
    }

    private void writeSalutation(Document doc) {
        try {
            doc.add(new Paragraph(receiver.getName()));
            doc.add(new Paragraph("T.a.v. " + receiver.getContact()));
            doc.add(new Paragraph(receiver.getStreet() + " " + receiver.getStreetNr()));
            doc.add(new Paragraph(receiver.getPostal() + "  " + receiver.getCity()));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private void writeSender(Document doc) {
        try {
            doc.add(createParagraph(user.getCompanyName(), 2));
            doc.add(createParagraph(user.getPostal() + "  " + user.getCity(), 2));
            doc.add(createParagraph("KvK: " + user.getKvk(), 2));
            doc.add(createParagraph("BTW: " + user.getBtw(), 2));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private void writeInvoiceDetails(Document doc) {
        try {
            doc.add(createTitle(title));
            doc.add(new Paragraph(labelDate + ": " + invoice.getDate()));
            doc.add(new Paragraph(labelInvoice_number + ": #" + invoice.getInvoice_number()));
            doc.add(Chunk.NEWLINE);
            doc.add(new Paragraph(project.getName()));
            doc.add(Chunk.NEWLINE);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private void writeWorkTable(Document doc) {
        try {
            PdfPTable table = new PdfPTable(3);

            // Table - head
            table.addCell(createCellHeader(labelDescription, 0));
            table.addCell(createCellHeader(labelHours, 1));
            table.addCell(createCellHeader(labelPrice, 2));

            // Table - work
            for (Work work: works) {
                table.addCell(createCell(work.getDescription(), 0, 400));
                table.addCell(createCell(String.valueOf(work.getHours()), 1, 400));
                table.addCell(createCell(res.getString(
                        R.string.placeholder_currency,
                        currency.format(work.getPrice())),
                        2,400));
            }

            // Table - totals
            String subtotal = currency.format(invoice.getTotalPrice() - invoice.getBtw());
            table.addCell(createCell("", 0, 0));
            table.addCell(createCell(labelSubtotal, 2, 700));
            table.addCell(createCell("€  " + subtotal, 2, 400));

            table.addCell(createCell("", 0, 0));
            table.addCell(createCell(labelBtw, 2, 700));
            table.addCell(createCell("€  " + currency.format(invoice.getBtw()), 2, 400));

            table.addCell(createCell("", 0, 0));
            table.addCell(createCell(labelTotalPrice, 2, 700));
            table.addCell(createCell("€  " + currency.format(invoice.getTotalPrice()), 2, 700));

            doc.add(table);

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private void writeFooter(Document doc) {
        try {
            doc.add(Chunk.NEWLINE);
            doc.add(Chunk.NEWLINE);
            doc.add(createDisclaimer(disclaimer));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private Paragraph createParagraph(String content, int align) {
        Font font = new Font(Font.FontFamily.HELVETICA, 12);
        Paragraph paragraph = new Paragraph(content, font);
        paragraph.setAlignment(align);
        return paragraph;
    }

    private Paragraph createTitle(String content) {
        Font font = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD);
        Paragraph paragraph = new Paragraph(content, font);
        paragraph.setSpacingAfter(10);
        return paragraph;
    }

    private PdfPCell createCell(String content, int align, int weight) {
        Font font;
        if (weight > 400) {
            font = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        } else {
            font = new Font(Font.FontFamily.HELVETICA, 12);
        }

        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setHorizontalAlignment(align);
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setPadding(8);

        return cell;
    }

    private PdfPCell createCellHeader(String content,int align) {
        Font font = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setHorizontalAlignment(align);
        cell.setUseVariableBorders(true);
        cell.setBorderColorTop(BaseColor.BLACK);
        cell.setBorderColorBottom(BaseColor.GRAY);
        cell.setBorderWidthTop(2);
        cell.setBorderWidthBottom(1);
        cell.setBorderWidthLeft(0);
        cell.setBorderWidthRight(0);
        cell.setPadding(8);
        return cell;
    }

    private Paragraph createDisclaimer(String content) {
        Font font = new Font(Font.FontFamily.HELVETICA, 11, Font.ITALIC);
        font.setColor(BaseColor.GRAY);
        return new Paragraph(content, font);
    }

    /**
     * Mark the work in this invoice as 'paid' in the database
     */
    private void setWorkPaid() {
        DatabaseReference dbWorkMe = PersistentDatabase.getReference().child("work").child(invoice.getUserId());
        final DatabaseReference dbWorkThis = dbWorkMe.child(invoice.getProjectId());

        dbWorkThis.child("unpaid").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot workSnapshot: dataSnapshot.getChildren()) {
                    Work work = workSnapshot.getValue(Work.class);
                    dbWorkThis.child("unpaid").child(workSnapshot.getKey()).setValue(null);
                    dbWorkThis.child("paid").push().setValue(work);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    private void openFile() {
        if (filepath != null || !filepath.equals("")) {

            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(Uri.fromFile(new File(filepath)),"application/pdf");
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            Intent intent = Intent.createChooser(target, res.getString(R.string.title_chooser));

            try {
                mContext.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(mContext, res.getString(R.string.error_no_reader),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

}
