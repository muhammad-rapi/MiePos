package com.mipos.app.models;

public class TransactionItem {
    private String menuId;
    private String menuName;
    private long price;
    private int qty;
    private long subtotal;

    public TransactionItem() {}

    public TransactionItem(CartItem cartItem) {
        this.menuId = cartItem.getMenuId();
        this.menuName = cartItem.getMenuName();
        this.price = cartItem.getPrice();
        this.qty = cartItem.getQty();
        this.subtotal = cartItem.getSubtotal();
    }

    public String getMenuId() { return menuId; }
    public void setMenuId(String menuId) { this.menuId = menuId; }
    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }
    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
    public long getSubtotal() { return subtotal; }
    public void setSubtotal(long subtotal) { this.subtotal = subtotal; }
}
