package nl.lennartklein.uurtjefactuurtje;

import java.util.Calendar;

/**
 * A user
 */

public class User {
    private String invoiceNumber;
    private String mail;
    private String name;
    private String payDue;
    private String phone;

    // Empty constructor for FireBase
    public User() {}

    public User(String mail) {
        this.invoiceNumber = Calendar.getInstance().get(Calendar.YEAR) + "0001";
        this.mail = mail;
        this.payDue = "14";
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPayDue() {
        return payDue;
    }

    public void setPayDue(String payDue) {
        this.payDue = payDue;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
