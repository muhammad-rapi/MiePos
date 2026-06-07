package com.mipos.app.models;

public class CartItem {
    private String menuId;
    private String menuName;
    private long price;
    private int qty;

    public CartItem(String menuId, String menuName, long price, int qty) {
        this.menuId = menuId;
        this.menuName = menuName;
        this.price = price;
        this.qty = qty;
    }

    public String getMenuId() { return menuId; }
    public String getMenuName() { return menuName; }
    public long getPrice() { return price; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
    public long getSubtotal() { return price * qty; }
}
