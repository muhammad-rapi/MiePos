package com.mipos.app.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.mipos.app.models.CartItem;
import com.mipos.app.models.MenuItem;
import com.mipos.app.repository.MenuRepository;
import com.mipos.app.repository.TransactionRepository;
import java.util.ArrayList;
import java.util.List;

public class PosViewModel extends ViewModel {
    private final MenuRepository menuRepository = new MenuRepository();
    private final TransactionRepository transactionRepository = new TransactionRepository();

    public final MutableLiveData<List<MenuItem>> menusLiveData = new MutableLiveData<>();
    public final MutableLiveData<List<CartItem>> cartLiveData = new MutableLiveData<>(new ArrayList<>());
    public final MutableLiveData<Long> totalLiveData = new MutableLiveData<>(0L);
    public final MutableLiveData<Boolean> transactionSuccessLiveData = new MutableLiveData<>();
    public final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    public final MutableLiveData<String> selectedCategory = new MutableLiveData<>("mie_ayam");

    public void loadMenus() {
        menuRepository.getMenus(menusLiveData, errorLiveData);
    }

    public void addToCart(MenuItem menu) {
        List<CartItem> cart = new ArrayList<>(cartLiveData.getValue());
        for (CartItem item : cart) {
            if (item.getMenuId().equals(menu.getId())) {
                item.setQty(item.getQty() + 1);
                cartLiveData.setValue(cart);
                recalcTotal(cart);
                return;
            }
        }
        cart.add(new CartItem(menu.getId(), menu.getName(), menu.getPrice(), 1));
        cartLiveData.setValue(cart);
        recalcTotal(cart);
    }

    public void increaseQty(int position) {
        List<CartItem> cart = new ArrayList<>(cartLiveData.getValue());
        CartItem item = cart.get(position);
        item.setQty(item.getQty() + 1);
        cartLiveData.setValue(cart);
        recalcTotal(cart);
    }

    public void decreaseQty(int position) {
        List<CartItem> cart = new ArrayList<>(cartLiveData.getValue());
        CartItem item = cart.get(position);
        if (item.getQty() <= 1) {
            cart.remove(position);
        } else {
            item.setQty(item.getQty() - 1);
        }
        cartLiveData.setValue(cart);
        recalcTotal(cart);
    }

    public void clearCart() {
        cartLiveData.setValue(new ArrayList<>());
        totalLiveData.setValue(0L);
    }

    private void recalcTotal(List<CartItem> cart) {
        long total = 0;
        for (CartItem item : cart) total += item.getSubtotal();
        totalLiveData.setValue(total);
    }

    public void processPayment(String paymentMethod) {
        List<CartItem> cart = cartLiveData.getValue();
        if (cart == null || cart.isEmpty()) {
            errorLiveData.setValue("Keranjang kosong");
            return;
        }
        long total = totalLiveData.getValue();
        transactionRepository.saveTransaction(cart, total, paymentMethod,
            transactionSuccessLiveData, errorLiveData);
    }
}
