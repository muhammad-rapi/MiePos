package com.mipos.app.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.mipos.app.R;
import com.mipos.app.databinding.ActivityMainBinding;
import com.mipos.app.fragments.LaporanFragment;
import com.mipos.app.fragments.PosFragment;
import com.mipos.app.fragments.ProfilFragment;
import com.mipos.app.utils.SessionManager;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            loadFragment(new PosFragment());
        }

        binding.bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment;
            int id = item.getItemId();
            if (id == R.id.nav_pos) {
                fragment = new PosFragment();
            } else if (id == R.id.nav_laporan) {
                var user = SessionManager.getInstance().getCurrentUser();
                if (user != null && user.isOwner()) {
                    fragment = new LaporanFragment();
                } else {
                    return false;
                }
            } else {
                fragment = new ProfilFragment();
            }
            loadFragment(fragment);
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit();
    }
}
