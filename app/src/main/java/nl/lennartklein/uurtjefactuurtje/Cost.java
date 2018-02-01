////////////////////////////////////////////////////////////////////////////////
// Title        Cost
//
// Date         February 1 2018
// Author       Lennart J Klein  (info@lennartklein.nl)
// Project      UurtjeFactuurtje
// Assignment   App Studio, University of Amsterdam
////////////////////////////////////////////////////////////////////////////////

package nl.lennartklein.uurtjefactuurtje;

/**
 * A cost the company has made
 */

public class Cost {
    private String key;
    private double btw;
    private String companyId;
    private String companyName;
    private String date;
    private String description;
    private String invoiceNr;
    private double price;
    private String userId;

    // Empty constructor for FireBase
    public Cost() {}

    public Cost(double btw, String companyId, String companyName, String date, String description,
                String invoiceNr,double price, String userId) {
        this.btw = btw;
        this.companyId = companyId;
        this.companyName = companyName;
        this.date = date;
        this.description = description;
        this.invoiceNr = invoiceNr;
        this.price = price;
        this.userId = userId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInvoiceNr() {
        return invoiceNr;
    }

    public void setInvoiceNr(String invoiceNr) {
        this.invoiceNr = invoiceNr;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
