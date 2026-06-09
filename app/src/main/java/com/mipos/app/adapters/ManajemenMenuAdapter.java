package com.mipos.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.button.MaterialButton;
import com.mipos.app.R;
import com.mipos.app.models.MenuItem;
import com.mipos.app.utils.CurrencyUtils;
import java.util.ArrayList;
import java.util.List;

public class ManajemenMenuAdapter extends RecyclerView.Adapter<ManajemenMenuAdapter.ViewHolder> {
    private List<MenuItem> menus = new ArrayList<>();
    private final OnMenuActionListener listener;

    public interface OnMenuActionListener {
        void onEdit(MenuItem menu);
        void onToggleAvailable(MenuItem menu);
    }

    public ManajemenMenuAdapter(OnMenuActionListener listener) {
        this.listener = listener;
    }

    public void setMenus(List<MenuItem> menus) {
        this.menus = new ArrayList<>(menus);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_menu_manajemen, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuItem menu = menus.get(position);
        holder.tvNama.setText(menu.getName());
        holder.tvHarga.setText(CurrencyUtils.format(menu.getPrice()));
        holder.tvKategori.setText(menu.getCategory() != null ? menu.getCategory() : "-");
        holder.switchTersedia.setOnCheckedChangeListener(null);
        holder.switchTersedia.setChecked(menu.isAvailable());
        holder.switchTersedia.setOnCheckedChangeListener((btn, checked) -> listener.onToggleAvailable(menu));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(menu));
    }

    @Override
    public int getItemCount() { return menus.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvHarga, tvKategori;
        SwitchMaterial switchTersedia;
        MaterialButton btnEdit;

        ViewHolder(View view) {
            super(view);
            tvNama = view.findViewById(R.id.tvNamaMenu);
            tvHarga = view.findViewById(R.id.tvHargaMenu);
            tvKategori = view.findViewById(R.id.tvKategoriMenu);
            switchTersedia = view.findViewById(R.id.switchTersedia);
            btnEdit = view.findViewById(R.id.btnEditMenu);
        }
    }
}
