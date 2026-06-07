package com.mipos.app.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.mipos.app.models.Transaction;
import com.mipos.app.repository.TransactionRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LaporanViewModel extends ViewModel {
    private final TransactionRepository repo = new TransactionRepository();

    public final MutableLiveData<List<Transaction>> transactionsLiveData = new MutableLiveData<>();
    public final MutableLiveData<Long> totalPemasukan = new MutableLiveData<>(0L);
    public final MutableLiveData<Integer> totalTransaksi = new MutableLiveData<>(0);
    public final MutableLiveData<Map<String, Long>> byPaymentMethod = new MutableLiveData<>();
    public final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public void loadTodayReport() {
        repo.getTodayTransactions(data -> {
            transactionsLiveData.postValue(data);
            totalTransaksi.postValue(data.size());

            long total = 0;
            Map<String, Long> byMethod = new HashMap<>();
            for (Transaction t : data) {
                total += t.getTotal();
                byMethod.merge(t.getPaymentMethod(), t.getTotal(), Long::sum);
            }
            totalPemasukan.postValue(total);
            byPaymentMethod.postValue(byMethod);
        }, errorLiveData);
    }
}
