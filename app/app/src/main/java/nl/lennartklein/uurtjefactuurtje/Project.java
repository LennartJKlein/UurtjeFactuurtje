package nl.lennartklein.uurtjefactuurtje;

/**
 * A project
 */
public class Project {
    private String userId;
    private String name;
    private String companyId;
    private String companyName;
    private String date;
    private String lastInvoice;
    private double hourPrice;

    // Empty constructor for FireBase
    public Project() {}

    public Project(String userId, String name, String companyId, String companyName,
                   String date, String lastInvoice, double hourPrice) {
        this.userId = userId;
        this.name = name;
        this.companyId = companyId;
        this.companyName = companyName;
        this.date = date;
        this.lastInvoice = lastInvoice;
        this.hourPrice = hourPrice;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getLastInvoice() {
        return lastInvoice;
    }

    public void setLastInvoice(String lastInvoice) {
        this.lastInvoice = lastInvoice;
    }

    public double getHourPrice() {
        return hourPrice;
    }

    public void setHourPrice(double hourPrice) {
        this.hourPrice = hourPrice;
    }
}
