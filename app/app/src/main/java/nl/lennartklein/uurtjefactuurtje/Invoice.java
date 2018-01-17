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

import static android.content.ContentValues.TAG;

/**
 * An invoice
 */
public class Invoice {
    private double btw;
    private String company_id;
    private String date;
    private String end_date;
    private String invoice_number;
    private String project_id;
    private String sender_id;
    private double total_price;
    private String user_id;
    private User user;
    private Company sender;
    private Company receiver;
    private Project project;
    private String filepath;
    private Context mContext;

    // Empty constructor for FireBase
    public Invoice() {}

    public Invoice(double btw, String company_id, String date, String end_date,
                   String invoice_number, String project_id, String sender_id,
                   double total_price, String user_id) {
        this.btw = btw;
        this.company_id = company_id;
        this.date = date;
        this.end_date = end_date;
        this.invoice_number = invoice_number;
        this.project_id = project_id;
        this.sender_id = sender_id;
        this.total_price = total_price;
        this.user_id = user_id;
    }

    public double getBtw() {
        return btw;
    }

    public void setBtw(double btw) {
        this.btw = btw;
    }

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public String getInvoice_number() {
        return invoice_number;
    }

    public void setInvoice_number(String invoice_number) {
        this.invoice_number = invoice_number;
    }

    public String getProject_id() {
        return project_id;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public double getTotal_price() {
        return total_price;
    }

    public void setTotal_price(double total_price) {
        this.total_price = total_price;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Company getSender() {
        return sender;
    }

    public void setSender(Company sender) {
        this.sender = sender;
    }

    public Company getReceiver() {
        return receiver;
    }

    public void setReceiver(Company receiver) {
        this.receiver = receiver;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    /**
     * Create a file of this invoice
     */
    public void createFile(Context mContext) {

        if (isExternalStorageWritable()) {

            this.mContext = mContext;
            fetchData();

        } else {
            Log.d("Data", "Cannot write to device storage.");
        }
    }

    public void fetchData() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setUser(dataSnapshot.child("users").child(user_id).getValue(User.class));
                setSender(dataSnapshot.child("companies").child(sender_id).getValue(Company.class));
                setReceiver(dataSnapshot.child("companies").child(company_id).getValue(Company.class));
                setProject(dataSnapshot.child("projects").child(project_id).getValue(Project.class));

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
    public String buildDocument() {

        Resources res = mContext.getResources();

        // Get filename and directory
        filepath = getInvoiceStoragePath();

        // Template labels and texts
        String title = res.getString(R.string.invoice);
        String label_date = res.getString(R.string.label_date);
        String label_invoice_number = res.getString(R.string.label_invoice_number);
        String label_description = res.getString(R.string.description);
        String label_hours = res.getString(R.string.hours);
        String label_price = res.getString(R.string.price);
        String label_subtotal = res.getString(R.string.label_subtotal);
        String label_btw = res.getString(R.string.label_btw);
        String label_total_price = res.getString(R.string.label_total_price);
        String disclaimer = res.getString(R.string.disclaimer,
                user.getPay_due(), sender.getBank(), sender.getContact());

        // Generate the document
        Document doc = new Document();
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(filepath));
            doc.open();

            // To
            doc.add(new Paragraph(receiver.getName()));
            doc.add(new Paragraph("T.a.v. " + receiver.getContact()));
            doc.add(new Paragraph(receiver.getStreet() + " " + receiver.getStreet_nr()));
            doc.add(new Paragraph(receiver.getPostal() + "  " + receiver.getCity()));

            // From
            doc.add(createParagraph(sender.getName(), 2));
            doc.add(createParagraph(sender.getStreet() + " " + sender.getStreet_nr(), 2));
            doc.add(createParagraph(sender.getPostal() + "  " + sender.getCity(), 2));
            doc.add(createParagraph("KvK: " + sender.getKvk(), 2));
            doc.add(createParagraph("BTW: " + sender.getBtw(), 2));

            // Details
            doc.add(createTitle(title));
            doc.add(new Paragraph(label_date + ": " + getDate()));
            doc.add(new Paragraph(label_invoice_number + ": #" + getInvoice_number()));
            doc.add(Chunk.NEWLINE);
            doc.add(new Paragraph(project.getName()));
            doc.add(Chunk.NEWLINE);

            // Table - head
            PdfPTable table = new PdfPTable(3);
            table.addCell(createCellHeader(label_description, 0));
            table.addCell(createCellHeader(label_hours, 1));
            table.addCell(createCellHeader(label_price, 2));

            // Table - work
            // TODO: for loop through work entries
            table.addCell(createCell("Work 1", 0, 400));
            table.addCell(createCell("5,0", 1, 400));
            table.addCell(createCell("€  75,00", 2, 400));
            table.addCell(createCell("Work 2 and another thing", 0, 400));
            table.addCell(createCell("2,5", 1, 400));
            table.addCell(createCell("€  37,50", 2, 400));

            table.addCell(createCell("", 0, 0));
            table.addCell(createCell(label_subtotal, 2, 700));
            String subtotal = String.format("%.2f", getTotal_price() - getBtw());
            table.addCell(createCell("€  " + subtotal, 2, 400));
            table.addCell(createCell("", 0, 0));
            table.addCell(createCell(label_btw, 2, 700));
            table.addCell(createCell("€  " + getBtw(), 2, 400));
            table.addCell(createCell("", 0, 0));
            table.addCell(createCell(label_total_price, 2, 700));
            table.addCell(createCell("€  " + getTotal_price(), 2, 700));
            doc.add(table);

            // Foot
            doc.add(Chunk.NEWLINE);
            doc.add(Chunk.NEWLINE);
            doc.add(createDisclaimer(disclaimer));

            // Close and launch the file
            doc.close();
            openFile(mContext);

        } catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
        }

        return filepath;
    }

    public Paragraph createParagraph(String content, int align) {
        Font font = new Font(Font.FontFamily.HELVETICA, 12);
        Paragraph paragraph = new Paragraph(content, font);
        paragraph.setAlignment(align);
        return paragraph;
    }

    public Paragraph createTitle(String content) {
        Font font = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD);
        Paragraph paragraph = new Paragraph(content, font);
        return paragraph;
    }

    public PdfPCell createCell(String content,int align, int weight) {
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

    public PdfPCell createCellHeader(String content,int align) {
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

    public Paragraph createDisclaimer(String content) {
        Font font = new Font(Font.FontFamily.HELVETICA, 11, Font.ITALIC);
        Paragraph paragraph = new Paragraph(content, font);
        return paragraph;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public String getInvoiceStoragePath() {
        String folder_main = "UurtjeFactuurtje";
        String root = Environment.getExternalStorageDirectory() +  "/" + folder_main;

        String filename = "#" + getInvoice_number() + " " + receiver.getName() + ".pdf";
        String filepath = root + "/" + filename;

        File dir = new File(root);
        if (!dir.mkdirs()) {
            Log.d("File", "Directories not created");
        }

        return filepath;
    }

    public void openFile(Context mContext) {
        if (filepath != "" || filepath != null) {

            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(Uri.fromFile(new File(filepath)),"application/pdf");
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            Intent intent = Intent.createChooser(target, mContext.getResources().getString(R.string.title_chooser));

            try {
                mContext.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(mContext, mContext.getResources().getString(R.string.error_no_reader), Toast.LENGTH_LONG).show();
            }
        }
    }


}
