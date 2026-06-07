package com.mipos.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.mipos.app.R;
import com.mipos.app.activities.LoginActivity;
import com.mipos.app.repository.AuthRepository;
import com.mipos.app.utils.SessionManager;

public class ProfilFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profil, container, false);

        var user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            ((TextView) view.findViewById(R.id.tvName)).setText(user.getName());
            ((TextView) view.findViewById(R.id.tvRole)).setText(
                user.isOwner() ? "Owner" : "Kasir");
        }

        ((MaterialButton) view.findViewById(R.id.btnLogout)).setOnClickListener(v -> {
            new AuthRepository().logout();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            requireActivity().finish();
        });

        return view;
    }
}
