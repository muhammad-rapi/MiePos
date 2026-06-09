package com.mipos.app.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.mipos.app.models.Transaction;
import com.mipos.app.repository.TransactionRepository;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LaporanViewModel extends ViewModel {
    private final TransactionRepository repo = new TransactionRepository();

    public final MutableLiveData<List<Transaction>> transactionsLiveData = new MutableLiveData<>();
    public final MutableLiveData<Long> totalPemasukan = new MutableLiveData<>(0L);
    public final MutableLiveData<Integer> totalTransaksi = new MutableLiveData<>(0);
    public final MutableLiveData<Long> rataRata = new MutableLiveData<>(0L);
    public final MutableLiveData<Map<String, Long>> byPaymentMethod = new MutableLiveData<>();
    public final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    public final MutableLiveData<String> selectedPeriod = new MutableLiveData<>("daily");

    public void loadReport(String period) {
        selectedPeriod.setValue(period);
        Date[] range = getDateRange(period);
        repo.getTransactionsByRange(range[0], range[1], data -> {
            transactionsLiveData.postValue(data);
            totalTransaksi.postValue(data.size());

            long total = 0;
            Map<String, Long> byMethod = new HashMap<>();
            for (Transaction t : data) {
                total += t.getTotal();
                byMethod.merge(t.getPaymentMethod(), t.getTotal(), Long::sum);
            }
            totalPemasukan.postValue(total);
            rataRata.postValue(data.isEmpty() ? 0L : total / data.size());
            byPaymentMethod.postValue(byMethod);
        }, errorLiveData);
    }

    // Keep backward compat
    public void loadTodayReport() {
        loadReport("daily");
    }

    private Date[] getDateRange(String period) {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();

        switch (period) {
            case "weekly":
                start.set(Calendar.DAY_OF_WEEK, start.getFirstDayOfWeek());
                break;
            case "monthly":
                start.set(Calendar.DAY_OF_MONTH, 1);
                break;
            default: // daily
                break;
        }
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);

        return new Date[]{start.getTime(), end.getTime()};
    }
}
