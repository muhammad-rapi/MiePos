package com.mipos.app.repository;

import androidx.lifecycle.MutableLiveData;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.mipos.app.models.CartItem;
import com.mipos.app.models.Transaction;
import com.mipos.app.models.TransactionItem;
import com.mipos.app.utils.SessionManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class TransactionRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void saveTransaction(List<CartItem> cartItems, long total,
                                 String paymentMethod,
                                 MutableLiveData<Boolean> successLiveData,
                                 MutableLiveData<String> errorLiveData) {
        var currentUser = SessionManager.getInstance().getCurrentUser();

        List<TransactionItem> items = new ArrayList<>();
        for (CartItem c : cartItems) items.add(new TransactionItem(c));

        Transaction transaction = new Transaction();
        transaction.setItems(items);
        transaction.setTotal(total);
        transaction.setPaymentMethod(paymentMethod);
        transaction.setKasirId(currentUser.getUid());
        transaction.setKasirName(currentUser.getName());
        transaction.setTimestamp(Timestamp.now());
        transaction.setStatus("completed");

        db.collection("transactions").add(transaction)
            .addOnSuccessListener(ref -> successLiveData.setValue(true))
            .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public void getTodayTransactions(Consumer<List<Transaction>> onSuccess,
                                      MutableLiveData<String> errorLiveData) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Timestamp startOfDay = new Timestamp(new Date(cal.getTimeInMillis()));

        db.collection("transactions")
            .whereGreaterThanOrEqualTo("timestamp", startOfDay)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(snapshots -> {
                List<Transaction> list = new ArrayList<>();
                for (var doc : snapshots) {
                    Transaction t = doc.toObject(Transaction.class);
                    t.setId(doc.getId());
                    list.add(t);
                }
                onSuccess.accept(list);
            })
            .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }
}
