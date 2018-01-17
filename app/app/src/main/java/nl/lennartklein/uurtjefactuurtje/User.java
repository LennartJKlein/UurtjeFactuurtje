package nl.lennartklein.uurtjefactuurtje;

/**
 * A user
 */

public class User {
    private String invoice_number;
    private String mail;
    private String name;
    private int pay_due;
    private String phone;

    // Empty constructor for FireBase
    public User() {}

    public User(String invoice_number, String mail, String name, int pay_due, String phone) {
        this.invoice_number = invoice_number;
        this.mail = mail;
        this.name = name;
        this.pay_due = pay_due;
        this.phone = phone;
    }

    public String getInvoice_number() {
        return invoice_number;
    }

    public void setInvoice_number(String invoice_number) {
        this.invoice_number = invoice_number;
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

    public int getPay_due() {
        return pay_due;
    }

    public void setPay_due(int pay_due) {
        this.pay_due = pay_due;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
