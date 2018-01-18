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
    private String companyId;
    private String date;
    private String endDate;
    private String invoice_number;
    private String projectId;
    private String senderId;
    private double totalPrice;
    private String userId;
    private User user;
    private Company sender;
    private Company receiver;
    private Project project;
    private String filepath;
    private Context mContext;

    // Empty constructor for FireBase
    public Invoice() {}

    public Invoice(double btw, String companyId, String date, String endDate,
                   String invoice_number, String projectId, String senderId,
                   double totalPrice, String userId) {
        this.btw = btw;
        this.companyId = companyId;
        this.date = date;
        this.endDate = endDate;
        this.invoice_number = invoice_number;
        this.projectId = projectId;
        this.senderId = senderId;
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

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
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
                setUser(dataSnapshot.child("users").child(userId).getValue(User.class));
                setSender(dataSnapshot.child("companies").child(senderId).getValue(Company.class));
                setReceiver(dataSnapshot.child("companies").child(companyId).getValue(Company.class));
                setProject(dataSnapshot.child("projects").child(projectId).getValue(Project.class));

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
        String labelDate = res.getString(R.string.label_date);
        String labelInvoice_number = res.getString(R.string.label_invoice_number);
        String labelDescription = res.getString(R.string.description);
        String labelHours = res.getString(R.string.hours);
        String labelPrice = res.getString(R.string.price);
        String labelSubtotal = res.getString(R.string.label_subtotal);
        String labelBtw = res.getString(R.string.label_btw);
        String labelTotalPrice = res.getString(R.string.label_total_price);
        String disclaimer = res.getString(R.string.disclaimer,
                user.getPayDue(), sender.getBank(), sender.getContact());

        // Generate the document
        Document doc = new Document();
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(filepath));
            doc.open();

            // To
            doc.add(new Paragraph(receiver.getName()));
            doc.add(new Paragraph("T.a.v. " + receiver.getContact()));
            doc.add(new Paragraph(receiver.getStreet() + " " + receiver.getStreetNr()));
            doc.add(new Paragraph(receiver.getPostal() + "  " + receiver.getCity()));

            // From
            doc.add(createParagraph(sender.getName(), 2));
            doc.add(createParagraph(sender.getStreet() + " " + sender.getStreetNr(), 2));
            doc.add(createParagraph(sender.getPostal() + "  " + sender.getCity(), 2));
            doc.add(createParagraph("KvK: " + sender.getKvk(), 2));
            doc.add(createParagraph("BTW: " + sender.getBtw(), 2));

            // Details
            doc.add(createTitle(title));
            doc.add(new Paragraph(labelDate + ": " + getDate()));
            doc.add(new Paragraph(labelInvoice_number + ": #" + getInvoice_number()));
            doc.add(Chunk.NEWLINE);
            doc.add(new Paragraph(project.getName()));
            doc.add(Chunk.NEWLINE);

            // Table - head
            PdfPTable table = new PdfPTable(3);
            table.addCell(createCellHeader(labelDescription, 0));
            table.addCell(createCellHeader(labelHours, 1));
            table.addCell(createCellHeader(labelPrice, 2));

            // Table - work
            // TODO: for loop through work entries
            table.addCell(createCell("Work 1", 0, 400));
            table.addCell(createCell("5,0", 1, 400));
            table.addCell(createCell("€  75,00", 2, 400));
            table.addCell(createCell("Work 2 and another thing", 0, 400));
            table.addCell(createCell("2,5", 1, 400));
            table.addCell(createCell("€  37,50", 2, 400));

            table.addCell(createCell("", 0, 0));
            table.addCell(createCell(labelSubtotal, 2, 700));
            String subtotal = String.format("%.2f", getTotalPrice() - getBtw());
            table.addCell(createCell("€  " + subtotal, 2, 400));
            table.addCell(createCell("", 0, 0));
            table.addCell(createCell(labelBtw, 2, 700));
            table.addCell(createCell("€  " + getBtw(), 2, 400));
            table.addCell(createCell("", 0, 0));
            table.addCell(createCell(labelTotalPrice, 2, 700));
            table.addCell(createCell("€  " + getTotalPrice(), 2, 700));
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
