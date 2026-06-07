package com.mipos.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mipos.app.R;
import com.mipos.app.models.MenuItem;
import com.mipos.app.utils.CurrencyUtils;
import java.util.ArrayList;
import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
    private List<MenuItem> menus = new ArrayList<>();
    private final OnMenuClickListener listener;

    public interface OnMenuClickListener {
        void onMenuClick(MenuItem menu);
    }

    public MenuAdapter(OnMenuClickListener listener) {
        this.listener = listener;
    }

    public void setMenus(List<MenuItem> menus) {
        this.menus = menus;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_menu_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuItem menu = menus.get(position);
        holder.tvName.setText(menu.getName());
        holder.tvPrice.setText(CurrencyUtils.format(menu.getPrice()));

        switch (menu.getCategory()) {
            case "minuman": holder.tvEmoji.setText("🥤"); break;
            case "tambahan": holder.tvEmoji.setText("🍢"); break;
            default: holder.tvEmoji.setText("🍜"); break;
        }

        holder.itemView.setOnClickListener(v -> listener.onMenuClick(menu));
    }

    @Override
    public int getItemCount() { return menus.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName, tvPrice;
        ViewHolder(View view) {
            super(view);
            tvEmoji = view.findViewById(R.id.tvEmoji);
            tvName = view.findViewById(R.id.tvMenuName);
            tvPrice = view.findViewById(R.id.tvMenuPrice);
        }
    }
}
