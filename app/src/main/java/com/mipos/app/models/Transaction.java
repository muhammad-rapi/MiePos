package com.mipos.app.models;

import com.google.firebase.Timestamp;
import java.util.List;

public class Transaction {
    private String id;
    private List<TransactionItem> items;
    private long total;
    private String paymentMethod;
    private String kasirId;
    private String kasirName;
    private Timestamp timestamp;
    private String status;

    public Transaction() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public List<TransactionItem> getItems() { return items; }
    public void setItems(List<TransactionItem> items) { this.items = items; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getKasirId() { return kasirId; }
    public void setKasirId(String kasirId) { this.kasirId = kasirId; }
    public String getKasirName() { return kasirName; }
    public void setKasirName(String kasirName) { this.kasirName = kasirName; }
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
