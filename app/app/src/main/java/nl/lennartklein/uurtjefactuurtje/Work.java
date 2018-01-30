package nl.lennartklein.uurtjefactuurtje;

import java.io.Serializable;

/**
 * A entry of work (hours or product)
 */
public class Work implements Serializable {

    private static final long serialVersionUID = -7060210544600464481L;
    private String id;
    private String userId;
    private String projectId;
    private String invoiceId;
    private String description;
    private String date;
    private double hours;
    private double price;

    // Empty constructor for FireBase
    Work() {}

    public Work(String id, String userId, String projectId, String invoiceId, String description,
                String date, double hours, double price) {
        this.id = id;
        this.userId = userId;
        this.projectId = projectId;
        this.invoiceId = invoiceId;
        this.description = description;
        this.date = date;
        this.hours = hours;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getHours() {
        return hours;
    }

    public void setHours(double hours) {
        this.hours = hours;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
