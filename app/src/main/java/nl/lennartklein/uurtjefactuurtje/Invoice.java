package nl.lennartklein.uurtjefactuurtje;

import java.util.ArrayList;
import java.util.List;

/**
 * An invoice
 */
public class Invoice {
    private String key;
    private double btw;
    private String companyId;
    private Company company;
    private String date;
    private String endDate;
    private String invoiceNr;
    private String projectId;
    private Project project;
    private double totalPrice;
    private String userId;
    private User user;
    private List<Work> works = new ArrayList<>();
    private String filePath;

    // Empty constructor for FireBase
    public Invoice() {}

    public Invoice(double btw, String companyId, String date, String endDate, String filePath,
                   String invoiceNr, String projectId, String userId, double totalPrice) {
        this.btw = btw;
        this.companyId = companyId;
        this.date = date;
        this.endDate = endDate;
        this.invoiceNr = invoiceNr;
        this.projectId = projectId;
        this.totalPrice = totalPrice;
        this.filePath = filePath;
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

    public String getInvoiceNr() {
        return invoiceNr;
    }

    public void setInvoiceNr(String invoiceNr) {
        this.invoiceNr = invoiceNr;
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<Work> getWorks() {
        return works;
    }

    public void addWork(Work work) {
        this.works.add(work);
    }
}
