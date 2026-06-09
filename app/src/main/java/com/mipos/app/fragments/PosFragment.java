package com.mipos.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.snackbar.Snackbar;
import com.mipos.app.adapters.CartAdapter;
import com.mipos.app.adapters.MenuAdapter;
import com.mipos.app.databinding.FragmentPosBinding;
import com.mipos.app.dialogs.PaymentBottomSheet;
import com.mipos.app.models.CartItem;
import com.mipos.app.utils.CurrencyUtils;
import com.mipos.app.utils.PrinterManager;
import com.mipos.app.viewmodel.PosViewModel;
import java.util.List;
import java.util.stream.Collectors;

public class PosFragment extends Fragment {
    private FragmentPosBinding binding;
    private PosViewModel viewModel;
    private MenuAdapter menuAdapter;
    private CartAdapter cartAdapter;

    private final String[] categories = {"mie_ayam", "minuman", "tambahan"};
    private final String[] categoryLabels = {"Mie Ayam", "Minuman", "Tambahan"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(PosViewModel.class);

        setupMenuRecycler();
        setupCartRecycler();
        setupTabs();
        observeViewModel();

        viewModel.loadMenus();

        binding.btnBayar.setOnClickListener(v -> {
            if (viewModel.cartLiveData.getValue() == null ||
                viewModel.cartLiveData.getValue().isEmpty()) return;

            PaymentBottomSheet sheet = new PaymentBottomSheet(viewModel);
            sheet.show(getParentFragmentManager(), "payment");
        });
    }

    private void setupMenuRecycler() {
        menuAdapter = new MenuAdapter(menu -> viewModel.addToCart(menu));
        binding.rvMenu.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvMenu.setAdapter(menuAdapter);
    }

    private void setupCartRecycler() {
        cartAdapter = new CartAdapter(new CartAdapter.CartActionListener() {
            public void onIncrease(int pos) { viewModel.increaseQty(pos); }
            public void onDecrease(int pos) { viewModel.decreaseQty(pos); }
        });
        binding.rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCart.setAdapter(cartAdapter);
    }

    private void setupTabs() {
        for (String label : categoryLabels) {
            binding.tabCategory.addTab(binding.tabCategory.newTab().setText(label));
        }
        binding.tabCategory.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            public void onTabSelected(TabLayout.Tab tab) {
                viewModel.selectedCategory.setValue(categories[tab.getPosition()]);
                filterMenuByCategory(categories[tab.getPosition()]);
            }
            public void onTabUnselected(TabLayout.Tab tab) {}
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void filterMenuByCategory(String category) {
        var allMenus = viewModel.menusLiveData.getValue();
        if (allMenus == null) return;
        var filtered = allMenus.stream()
            .filter(m -> m.getCategory().equals(category))
            .collect(Collectors.toList());
        menuAdapter.setMenus(filtered);
    }

    private void observeViewModel() {
        viewModel.menusLiveData.observe(getViewLifecycleOwner(), menus -> {
            String cat = viewModel.selectedCategory.getValue();
            if (cat == null) cat = "mie_ayam";
            filterMenuByCategory(cat);
        });

        viewModel.cartLiveData.observe(getViewLifecycleOwner(), cart ->
            cartAdapter.setItems(cart));

        viewModel.totalLiveData.observe(getViewLifecycleOwner(), total ->
            binding.tvTotal.setText(CurrencyUtils.format(total)));

        viewModel.transactionSuccessLiveData.observe(getViewLifecycleOwner(), success -> {
            if (!Boolean.TRUE.equals(success)) return;
            List<CartItem> lastCart = viewModel.lastCartSnapshot;
            long lastTotal = viewModel.lastTotalSnapshot;
            String lastMethod = viewModel.lastPaymentMethod;
            if (lastCart == null || lastCart.isEmpty()) return;
            PrinterManager.getInstance().print(
                requireContext(), lastCart, lastTotal, lastMethod, 0,
                new PrinterManager.PrintCallback() {
                    @Override public void onSuccess() { /* silent */ }
                    @Override public void onError(String message) {
                        requireActivity().runOnUiThread(() -> {
                            if (binding == null) return;
                            Snackbar.make(binding.getRoot(),
                                    "Printer: " + message, Snackbar.LENGTH_LONG)
                                .setAction("Coba Lagi", v ->
                                    PrinterManager.getInstance().print(
                                        requireContext(), lastCart, lastTotal, lastMethod, 0,
                                        new PrinterManager.PrintCallback() {
                                            @Override public void onSuccess() {}
                                            @Override public void onError(String m) {}
                                        }))
                                .show();
                        });
                    }
                });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
