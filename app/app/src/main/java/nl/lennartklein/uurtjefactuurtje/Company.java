package nl.lennartklein.uurtjefactuurtje;

/**
 * A company
 */
public class Company {
    private String id;
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
    private String streetNr;
    private String userId;
    private String website;

    // Empty constructor for FireBase
    public Company() {}

    public Company(String bank, String btw, String city, String contact, String kvk, String mail,
                   String name, String postal, int primary, String street, String streetNr,
                   String userId, String website) {
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
        this.streetNr = streetNr;
        this.userId = userId;
        this.website = website;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getStreetNr() {
        return streetNr;
    }

    public void setStreetNr(String streetNr) {
        this.streetNr = streetNr;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}