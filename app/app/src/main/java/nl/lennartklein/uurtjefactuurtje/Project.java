package nl.lennartklein.uurtjefactuurtje;

/**
 * A project
 */
public class Project {
    private String user_id;
    private String name;
    private String company_id;
    private String company_name;
    private String date;
    private String last_invoice;
    private double hour_price;

    // Empty constructor for FireBase
    public Project() {}

    public Project(String user_id, String name, String company_id, String company_name,
                   String date, String last_invoice, double hour_price) {
        this.user_id = user_id;
        this.name = name;
        this.company_id = company_id;
        this.company_name = company_name;
        this.date = date;
        this.last_invoice = last_invoice;
        this.hour_price = hour_price;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLast_invoice() {
        return last_invoice;
    }

    public void setLast_invoice(String last_invoice) {
        this.last_invoice = last_invoice;
    }

    public double getHour_price() {
        return hour_price;
    }

    public void setHour_price(double hour_price) {
        this.hour_price = hour_price;
    }
}
