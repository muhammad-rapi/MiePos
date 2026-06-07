package com.mipos.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mipos.app.R;
import com.mipos.app.models.Transaction;
import com.mipos.app.utils.CurrencyUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private List<Transaction> transactions = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction t = transactions.get(position);
        holder.tvTotal.setText(CurrencyUtils.format(t.getTotal()));

        String methodEmoji;
        switch (t.getPaymentMethod() != null ? t.getPaymentMethod() : "") {
            case "qris": methodEmoji = "📱 QRIS"; break;
            case "debit": methodEmoji = "💳 Debit"; break;
            default: methodEmoji = "💵 Cash"; break;
        }
        holder.tvMethod.setText(methodEmoji);

        if (t.getTimestamp() != null) {
            holder.tvTime.setText(sdf.format(t.getTimestamp().toDate()));
        }

        if (t.getItems() != null && !t.getItems().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(2, t.getItems().size()); i++) {
                if (i > 0) sb.append(", ");
                sb.append(t.getItems().get(i).getMenuName());
            }
            if (t.getItems().size() > 2) sb.append(" +").append(t.getItems().size() - 2);
            holder.tvItems.setText(sb.toString());
        }
    }

    @Override
    public int getItemCount() { return transactions.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvItems, tvTotal, tvMethod;
        ViewHolder(View view) {
            super(view);
            tvTime = view.findViewById(R.id.tvTransTime);
            tvItems = view.findViewById(R.id.tvTransItems);
            tvTotal = view.findViewById(R.id.tvTransTotal);
            tvMethod = view.findViewById(R.id.tvTransMethod);
        }
    }
}
