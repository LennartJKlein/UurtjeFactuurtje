////////////////////////////////////////////////////////////////////////////////
// Title        GeneratePdf
//
// Date         February 1 2018
// Author       Lennart J Klein  (info@lennartklein.nl)
// Project      UurtjeFactuurtje
// Assignment   App Studio, University of Amsterdam
////////////////////////////////////////////////////////////////////////////////

package nl.lennartklein.uurtjefactuurtje;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

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

/**
 * Creates a PDF for an invoice (containing necessary info for accountancy)
 */

public class GeneratePdf {

    // Data
    private Context mContext;
    private Activity mActivity;
    private Invoice invoice;
    private String filepath;

    // Template
    private Resources res;
    private String title;
    private String labelDate;
    private String labelInvoiceNr;
    private String labelDescription;
    private String labelHours;
    private String labelPrice;
    private String labelSubtotal;
    private String labelBtw;
    private String labelTotalPrice;
    private String disclaimer;
    private DecimalFormat currencyFormat = new DecimalFormat("0.00");
    private DecimalFormat timeFormat = new DecimalFormat("0.00");

    public GeneratePdf(Activity mActivity, Context mContext, Invoice invoice) {
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.invoice = invoice;
        this.res = mContext.getResources();
    }

    /**
     * Create a file of this invoice
     */
    public String createFile() {
        if (isExternalStorageWritable()) {

            return buildDocument();

        } else {
            Toast.makeText(mContext, res.getString(R.string.error_no_permission),
                    Toast.LENGTH_SHORT).show();

            return null;
        }
    }

    /**
     * Create a document for this invoice
     * @return string of the filepath
     */
    private String buildDocument() {

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

            openFile();

        } catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(mContext,
                    res.getString(R.string.error_no_file_made), Toast.LENGTH_SHORT).show();
        }

        return filepath;
    }

    private boolean isExternalStorageWritable() {
        if (ContextCompat.checkSelfPermission(mActivity,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(mActivity,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0x3);

            } else {
                ActivityCompat.requestPermissions(mActivity,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0x3);
            }
        }

        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private String getInvoiceStoragePath() {
        String folder_main = "UurtjeFactuurtje";
        String root = Environment.getExternalStorageDirectory() +  "/" + folder_main;

        String filename = "#" + invoice.getInvoiceNr() + " " + invoice.getCompany().getName() + ".pdf";
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
        labelInvoiceNr = res.getString(R.string.label_invoice_number);
        labelDescription = res.getString(R.string.description);
        labelHours = res.getString(R.string.hours);
        labelPrice = res.getString(R.string.price);
        labelSubtotal = res.getString(R.string.label_subtotal);
        labelBtw = res.getString(R.string.label_btw);
        labelTotalPrice = res.getString(R.string.label_total_price);
        disclaimer = res.getString(R.string.disclaimer,
                invoice.getUser().getPayDue(),
                invoice.getUser().getBank(),
                invoice.getUser().getName()
        );
    }

    private void writeSalutation(Document doc) {
        try {
            Company company = invoice.getCompany();
            doc.add(new Paragraph(company.getName()));
            doc.add(new Paragraph("T.a.v. " + company.getContact()));
            doc.add(new Paragraph(company.getStreet() + " " + company.getStreetNr()));
            doc.add(new Paragraph(company.getPostal() + "  " + company.getCity()));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private void writeSender(Document doc) {
        try {
            User user = invoice.getUser();
            doc.add(createParagraph(user.getCompanyName(), 2));
            doc.add(createParagraph(user.getPostal() + "  " + user.getCity(), 2));
            if (user.getKvk() != null) {
                doc.add(createParagraph("KvK: " + user.getKvk(), 2));
            }
            if (user.getBtw() != null) {
                doc.add(createParagraph("BTW: " + user.getBtw(), 2));
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private void writeInvoiceDetails(Document doc) {
        try {
            Project project = invoice.getProject();
            doc.add(createTitle(title));
            doc.add(new Paragraph(labelDate + ": " + invoice.getDate()));
            doc.add(new Paragraph(labelInvoiceNr + ": #" + invoice.getInvoiceNr()));
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
            for (Work work: invoice.getWorks()) {

                String hours = "-";
                if (work.getHours() > 0) {
                    hours = String.valueOf(timeFormat.format(work.getHours()));
                }

                table.addCell(createCell(work.getDescription(), 0, 400));
                table.addCell(createCell(hours, 1, 400));
                table.addCell(createCell(res.getString(
                        R.string.placeholder_currency,
                        currencyFormat.format(work.getPrice())),
                        2,400));
            }

            // Table - totals
            String subtotal = currencyFormat.format(invoice.getTotalPrice() - invoice.getBtw());
            table.addCell(createCell("", 0, 0));
            table.addCell(createCell(labelSubtotal, 2, 700));
            table.addCell(createCell("€  " + subtotal, 2, 400));

            table.addCell(createCell("", 0, 0));
            table.addCell(createCell(labelBtw, 2, 700));
            table.addCell(createCell("€  " + currencyFormat.format(invoice.getBtw()), 2, 400));

            table.addCell(createCell("", 0, 0));
            table.addCell(createCell(labelTotalPrice, 2, 700));
            table.addCell(createCell("€  " + currencyFormat.format(invoice.getTotalPrice()), 2, 700));

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
     * Opens the file with a native chooser
     */
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
