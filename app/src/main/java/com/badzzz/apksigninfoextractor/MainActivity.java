package com.badzzz.apksigninfoextractor;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.badzzz.apkfileextractor.databinding.ActivityMainBinding;
import com.badzzz.apkfileextractor.fragments.AppListFragment;
import com.badzzz.apkfileextractor.utils.ADUtils;
import com.badzzz.apkfileextractor.utils.AppViewUtils;
import com.badzzz.apkfileextractor.utils.AppsInfoHandler;
import com.badzzz.apkfileextractor.utils.ToastUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayoutMediator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;

    private List<AppsInfoHandler.AppInfo> userApps = new ArrayList<>();
    private List<AppsInfoHandler.AppInfo> sysApps = new ArrayList<>();


    private Queue<WeakReference<ISearchTextChangeListener>> listeners = new ConcurrentLinkedQueue<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        loadAppsList();
        setupComponents();
        ADUtils.setupBannerAD(this, binding.adContainer, Others.AD_UNIT_BANNER);
    }


    private void setupComponents() {

        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                for (WeakReference<ISearchTextChangeListener> listener : listeners) {
                    if (listener.get() != null) {
                        listener.get().onSearchTextChange(s.toString());
                    }
                }
            }
        });
    }


    private void loadAppsList() {
        binding.progressIndicator.setVisibility(View.VISIBLE);
        AppsInfoHandler.getAllInstalledApps(new AppsInfoHandler.AppsInfoHandlerListener() {
            @Override
            public void onAppsInfoHandler(List<AppsInfoHandler.AppInfo> appsInfo) {
                runOnUiThread(() -> {
                    binding.progressIndicator.setVisibility(View.GONE);
                    afterAppsLoaded(appsInfo);
                });
            }
        });
    }


    private void afterAppsLoaded(List<AppsInfoHandler.AppInfo> appsInfo) {
        if (appsInfo == null || appsInfo.size() == 0) {
            ToastUtils.shortToast(this, R.string.no_apps_found);
        } else {
            for (AppsInfoHandler.AppInfo appInfo : appsInfo) {
                if (!appInfo.isSys()) {
                    userApps.add(appInfo);
                } else {
                    sysApps.add(appInfo);
                }
            }

            binding.viewPager.setAdapter(new ViewPagerAdapter(this));
            new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
                switch (position) {
                    case 0:
                        tab.setText(R.string.tab_title_user_installed_apps);
                        break;
                    case 1:
                        tab.setText(R.string.tab_title_system_apps);
                        break;
                }
            }).attach();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                showAboutDialog();
                break;

            case R.id.rate:
                AppViewUtils.viewAPPInPlayStore(this, getPackageName());
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(this).setView(R.layout.about).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public interface ISearchTextChangeListener {
        void onSearchTextChange(String text);
    }

    class ViewPagerAdapter extends FragmentStateAdapter {

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            AppListFragment appListFragment = new AppListFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(AppListFragment.KEY_APPS, (ArrayList<? extends Parcelable>) (position == 0 ? userApps : sysApps));
            Log.i(TAG, "createFragment: " + binding.searchInput.getText().toString());
            bundle.putString(AppListFragment.KEY_SEARCH, binding.searchInput.getText().toString());
            appListFragment.setArguments(bundle);
            listeners.add(new WeakReference<>(appListFragment));
            return appListFragment;
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

}