package com.badzzz.apksigninfoextractor.fragments;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
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
import com.badzzz.apksigninfoextractor.utils.ClipUtils;
import com.badzzz.apksigninfoextractor.utils.ShareUtils;
import com.badzzz.apksigninfoextractor.utils.ToastUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
                                    showSignature(appInfo);
                                    showInterstitial();
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

        private void showSignature(AppsInfoHandler.AppInfo appInfo) {
            byte[] signBytes = APPUtils.getAppSignatureBytes(XApplication.getInstance(), appInfo.getAppPackage());
            X509Certificate certificate = APPUtils.generateCertificate(signBytes);

            if (certificate == null) {
                ToastUtils.shortToast(getContext(), "error");
            } else {
                String md5 = APPUtils.sign(signBytes, APPUtils.DigestAlgorithmType.MD5);
                String sh1 = APPUtils.sign(signBytes, APPUtils.DigestAlgorithmType.SHA1);
                String sha256 = APPUtils.sign(signBytes, APPUtils.DigestAlgorithmType.SHA256);

                StringBuilder signInfo = new StringBuilder();
                String title = null;

                title = getResources().getString(R.string.title_appname);
                signInfo.append(title);
                signInfo.append("\n");
                signInfo.append(appInfo.getAppName());
                signInfo.append("\n\n");

                title = getResources().getString(R.string.title_pkg);
                signInfo.append(title);
                signInfo.append("\n");
                signInfo.append(appInfo.getAppPackage());
                signInfo.append("\n\n");

                title = getResources().getString(R.string.sign_owner);
                signInfo.append(title);
                signInfo.append("\n");
                signInfo.append(certificate.getSubjectDN());
                signInfo.append("\n\n");

                title = getResources().getString(R.string.sign_issuer);
                signInfo.append(title);
                signInfo.append("\n");
                signInfo.append(certificate.getIssuerDN());
                signInfo.append("\n\n");

                title = getResources().getString(R.string.sign_valid_date);
                signInfo.append(title);
                signInfo.append("\n");
                signInfo.append(certificate.getNotBefore() + " - " + certificate.getNotAfter());
                signInfo.append("\n\n");

                title = getResources().getString(R.string.sign_serial_number);
                signInfo.append(title);
                signInfo.append("\n");
                signInfo.append(certificate.getSerialNumber().toString(16));
                signInfo.append("\n\n");

                title = getResources().getString(R.string.sign_certificate_fingerprints);
                signInfo.append(title);
                signInfo.append("\n");
                signInfo.append("MD5: \n" + md5 + "\n\n");
                signInfo.append("SHA1: \n" + sh1 + "\n\n");
                signInfo.append("SHA256: \n" + sha256 + "\n");

                new MaterialAlertDialogBuilder(getContext()).setTitle(R.string.action_view_signature).setMessage(signInfo.toString()).setPositiveButton(R.string.action_send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ShareUtils.shareSimgpleText(getContext(), signInfo.toString());
                    }
                }).setNegativeButton(R.string.action_copy, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        boolean result = ClipUtils.copy(XApplication.getInstance(), signInfo.toString());
                        if (!result) {
                            ToastUtils.shortToast(getContext(), R.string.copy_file_failed);
                        }
                    }
                }).show();
            }
        }
    }
}



