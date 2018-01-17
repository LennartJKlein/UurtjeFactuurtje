package nl.lennartklein.uurtjefactuurtje;

/**
 * A company
 */
public class Company {
    private String bank;
    private String btw;
    private String city;
    private String contact;
    private String kvk;
    private String mail;
    private String name;
    private String postal;
    private int primary;
    private String street;
    private String street_nr;
    private String user_id;
    private String website;

    // Empty constructor for FireBase
    public Company() {}

    public Company(String bank, String btw, String city, String contact, String kvk, String mail,
                   String name, String postal, int primary, String street, String street_nr,
                   String user_id, String website) {
        this.bank = bank;
        this.btw = btw;
        this.city = city;
        this.contact = contact;
        this.kvk = kvk;
        this.mail = mail;
        this.name = name;
        this.postal = postal;
        this.primary = primary;
        this.street = street;
        this.street_nr = street_nr;
        this.user_id = user_id;
        this.website = website;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getBtw() {
        return btw;
    }

    public void setBtw(String btw) {
        this.btw = btw;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getKvk() {
        return kvk;
    }

    public void setKvk(String kvk) {
        this.kvk = kvk;
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

    public String getPostal() {
        return postal;
    }

    public void setPostal(String postal) {
        this.postal = postal;
    }

    public int getPrimary() {
        return primary;
    }

    public void setPrimary(int primary) {
        this.primary = primary;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreet_nr() {
        return street_nr;
    }

    public void setStreet_nr(String street_nr) {
        this.street_nr = street_nr;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}