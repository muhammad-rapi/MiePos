package com.mipos.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mipos.app.R;
import com.mipos.app.models.CartItem;
import com.mipos.app.utils.CurrencyUtils;
import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private List<CartItem> items = new ArrayList<>();
    private final CartActionListener listener;

    public interface CartActionListener {
        void onIncrease(int position);
        void onDecrease(int position);
    }

    public CartAdapter(CartActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = items.get(position);
        holder.tvName.setText(item.getMenuName());
        holder.tvSubtotal.setText(CurrencyUtils.format(item.getSubtotal()));
        holder.tvQty.setText(String.valueOf(item.getQty()));
        final int bindPos = position;
        holder.btnIncrease.setOnClickListener(v -> listener.onIncrease(bindPos));
        holder.btnDecrease.setOnClickListener(v -> listener.onDecrease(bindPos));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSubtotal, tvQty;
        ImageButton btnIncrease, btnDecrease;
        ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tvCartName);
            tvSubtotal = view.findViewById(R.id.tvCartSubtotal);
            tvQty = view.findViewById(R.id.tvQty);
            btnIncrease = view.findViewById(R.id.btnIncrease);
            btnDecrease = view.findViewById(R.id.btnDecrease);
        }
    }
}
