////////////////////////////////////////////////////////////////////////////////
// Title        Project
//
// Date         February 1 2018
// Author       Lennart J Klein  (info@lennartklein.nl)
// Project      UurtjeFactuurtje
// Assignment   App Studio, University of Amsterdam
////////////////////////////////////////////////////////////////////////////////

package nl.lennartklein.uurtjefactuurtje;

import com.google.android.gms.wearable.DataMap;

import java.io.Serializable;

/**
 * A project
 */
public class Project implements Serializable {

    private static final long serialVersionUID = -7060210544600464481L;
    private String id;
    private String userId;
    private String name;
    private String companyId;
    private String companyName;
    private String date;
    private String lastInvoice;
    private double hourPrice;
    private int status;

    // Empty constructor for FireBase
    public Project() {}

    public Project(String userId, String name, String companyId, String companyName,
                   String date, String lastInvoice, double hourPrice, int status) {
        this.userId = userId;
        this.name = name;
        this.companyId = companyId;
        this.companyName = companyName;
        this.date = date;
        this.lastInvoice = lastInvoice;
        this.hourPrice = hourPrice;
        this.status = status;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public DataMap getDataMap() {
        DataMap map = new DataMap();
        map.putString("id", id);
        map.putString("userId", userId);
        map.putString("name", name);
        map.putString("companyId", companyId);
        map.putString("companyName", companyName);
        map.putString("date", date);
        map.putString("lastInvoice", lastInvoice);
        map.putDouble("hourPrice", hourPrice);

        return map;
    }

}
