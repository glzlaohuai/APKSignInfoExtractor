package com.badzzz.apksigninfoextractor.fragments;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.badzzz.apksigninfoextractor.MainActivity;
import com.badzzz.apksigninfoextractor.Others;
import com.badzzz.apksigninfoextractor.R;
import com.badzzz.apksigninfoextractor.XApplication;
import com.badzzz.apksigninfoextractor.databinding.AppsListBinding;
import com.badzzz.apksigninfoextractor.databinding.ItemAppBinding;
import com.badzzz.apksigninfoextractor.utils.ADUtils;
import com.badzzz.apksigninfoextractor.utils.APPUtils;
import com.badzzz.apksigninfoextractor.utils.AppViewUtils;
import com.badzzz.apksigninfoextractor.utils.AppsInfoHandler;
import com.badzzz.apksigninfoextractor.utils.StorageUtils;
import com.badzzz.apksigninfoextractor.utils.ToastUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AppListFragment extends Fragment implements MainActivity.ISearchTextChangeListener {

    public static final String KEY_APPS = "apps";
    public static final String KEY_SEARCH = "search";
    private String TAG = "AppListFragment";
    private String searchText;
    private List<AppsInfoHandler.AppInfo> apps = new ArrayList<>();

    private AppsListBinding binding;

    private ExecutorService executors = Executors.newSingleThreadExecutor();


    public AppListFragment() {
        super();
        TAG = TAG + "_" + hashCode();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            ArrayList<Parcelable> tmp = new ArrayList<>(getArguments().getParcelableArrayList("apps"));
            if (tmp != null) {
                for (Parcelable p : tmp) {
                    if (p instanceof AppsInfoHandler.AppInfo) {
                        ((AppsInfoHandler.AppInfo) p).restoreIcon(XApplication.getInstance());
                    }
                    apps.add((AppsInfoHandler.AppInfo) p);
                }
            }
            searchText = getArguments().getString(KEY_SEARCH);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.apps_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i(TAG, "onViewCreated: " + apps.size() + ", search: " + searchText);
        binding = AppsListBinding.bind(view);
        binding.appsList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.appsList.setAdapter(new XAdapter(apps, searchText));
    }

    @Override
    public void onSearchTextChange(String text) {
        searchText = text;
        afterSearchTextUpdated();

        if (getArguments() != null) {
            getArguments().putString(KEY_SEARCH, searchText);
        }
    }

    private void afterSearchTextUpdated() {
        if (binding.appsList.getAdapter() != null) {
            ((XAdapter) binding.appsList.getAdapter()).filterWithSearch(searchText);
        }
    }

    class XAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_EMPTY = 0;
        private static final int VIEW_TYPE_CONTENT = 1;
        private List<AppsInfoHandler.AppInfo> usedApps;
        private List<AppsInfoHandler.AppInfo> allApps;

        public XAdapter(List<AppsInfoHandler.AppInfo> apps, String searchText) {
            this.allApps = apps;
            this.usedApps = new ArrayList<>();
            this.usedApps.addAll(filterAppsWithSearchText(searchText));
        }


        private List<AppsInfoHandler.AppInfo> filterAppsWithSearchText(String text) {
            if (TextUtils.isEmpty(text)) {
                return allApps;
            } else {
                text = text.toLowerCase();
                List<AppsInfoHandler.AppInfo> result = new ArrayList<>();
                for (AppsInfoHandler.AppInfo app : allApps) {
                    if (app.getAppName().toLowerCase().contains(text) || app.getAppPackage().toLowerCase().contains(text) || app.getVersionName().toLowerCase().contains(text)) {
                        result.add(app);
                    }
                }
                return result;
            }
        }

        public synchronized void filterWithSearch(String text) {
            usedApps.clear();
            usedApps.addAll(filterAppsWithSearchText(text));
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_CONTENT) {
                return new XHolder(getLayoutInflater().inflate(R.layout.item_app, parent, false));
            } else {
                return new RecyclerView.ViewHolder(getLayoutInflater().inflate(R.layout.item_empty, parent, false)) {

                };
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof XHolder) {
                ((XHolder) holder).bind(usedApps.get(position));
            }
        }


        @Override
        public int getItemViewType(int position) {
            if (usedApps.size() == 0) {
                return VIEW_TYPE_EMPTY;
            }
            return VIEW_TYPE_CONTENT;
        }

        @Override
        public int getItemCount() {
            if (usedApps.size() == 0) {
                return 1;
            }
            return usedApps.size();
        }
    }

    class XHolder extends RecyclerView.ViewHolder {

        ItemAppBinding binding;

        public XHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemAppBinding.bind(itemView);
        }

        private void showInterstitial() {
            if (ADUtils.isInterstitialAvailable(Others.AD_UNIT_INTERSTITIAL_VIEW)) {
                ADUtils.showInterstitial(Others.AD_UNIT_INTERSTITIAL_VIEW, getActivity());
            }
            ADUtils.loadInterstitial(getContext(), Others.AD_UNIT_INTERSTITIAL_VIEW);
        }

        public void bind(AppsInfoHandler.AppInfo appInfo) {
            binding.appName.setText(appInfo.getAppName());
            binding.appPackage.setText(appInfo.getAppPackage());
            binding.appIcon.setImageDrawable(appInfo.getIcon());
            binding.appVersionName.setText(appInfo.getVersionName());

            binding.buttonSignView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    executors.execute(new Runnable() {
                        @Override
                        public void run() {
                            binding.buttonSignView.post(new Runnable() {
                                @Override
                                public void run() {
                                    showSignature(appInfo.getAppPackage());
                                    binding.buttonSignView.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            showInterstitial();
                                        }
                                    }, 500);
                                }
                            });
                        }
                    });
                }
            });


            binding.buttonViewInSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        AppViewUtils.viewAppInSettings(XApplication.getInstance(), appInfo.getAppPackage());
                    } catch (ActivityNotFoundException e) {
                        ToastUtils.shortToast(getContext(), R.string.open_settings_failed);
                    }
                }
            });

            binding.buttonViewInPlayStore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        AppViewUtils.viewAPPInPlayStore(XApplication.getInstance(), appInfo.getAppPackage());
                    } catch (ActivityNotFoundException e) {
                        ToastUtils.shortToast(getContext(), R.string.open_play_store_failed);
                    }
                }
            });
        }

        private void showSignature(String pkg) {
            // TODO: 2022/4/14

            new MaterialAlertDialogBuilder(getContext()).setTitle(R.string.action_view_signature).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });


        }
    }
}



