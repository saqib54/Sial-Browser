
package com.example.sialbrowser;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.sialbrowser.databinding.ActivityBrowserBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;

public class BrowserActivity extends AppCompatActivity {

    private static final String TAG = "BrowserActivity";
    private ActivityBrowserBinding binding;
    private ProgressBar progressBar;
    private EditText urlBar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private WebViewAdapter adapter;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "BrowserPrefs";
    private static final String BOOKMARKS_KEY = "bookmarks";
    private static final String HISTORY_KEY = "history";
    private static final String DOWNLOADS_KEY = "downloads";
    private static final String JAVASCRIPT_KEY = "javaScriptEnabled";
    private static final String DARK_MODE_KEY = "darkModeEnabled";
    private static final int MAX_TABS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        applyTheme(sharedPreferences.getBoolean(DARK_MODE_KEY, false));

        binding = ActivityBrowserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeViews();
        setupToolbar();
        setupTabs();
        setupButtonListeners();

        // Check for URL to load from AboutActivity
        String loadUrl = getIntent().getStringExtra("loadUrl");
        if (loadUrl != null && !loadUrl.isEmpty()) {
            loadUrl(loadUrl);
        }
    }

    private void applyTheme(boolean isDarkModeEnabled) {
        AppCompatDelegate.setDefaultNightMode(isDarkModeEnabled ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void initializeViews() {
        progressBar = binding.progressBar;
        urlBar = binding.urlBar;
        tabLayout = binding.tabLayout;
        viewPager = binding.viewPager;
    }

    private void setupToolbar() {
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
    }

    private void setupTabs() {
        adapter = new WebViewAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateProgressBarVisibility();
            }
        });

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    tab.setText("Tab " + (position + 1));
                    tab.setCustomView(createTabView(position));
                }).attach();
    }

    private View createTabView(int position) {
        View tabView = LayoutInflater.from(this).inflate(R.layout.tab_item, null);
        TextView tabText = tabView.findViewById(R.id.tabText);
        ImageButton closeButton = tabView.findViewById(R.id.closeTabButton);
        tabText.setText("Tab " + (position + 1));
        closeButton.setVisibility(View.GONE);
        tabText.setOnLongClickListener(v -> {
            closeButton.setVisibility(View.VISIBLE);
            return true;
        });
        closeButton.setOnClickListener(v -> showCloseTabDialog(position));
        return tabView;
    }

    private void showCloseTabDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Close Tab")
                .setMessage("Do you want to close this tab?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    adapter.closeTab(position);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                    TabLayout.Tab tab = tabLayout.getTabAt(position);
                    if (tab != null) {
                        View tabView = tab.getCustomView();
                        if (tabView != null) {
                            tabView.findViewById(R.id.closeTabButton).setVisibility(View.GONE);
                        }
                    }
                })
                .setOnCancelListener(dialog -> {
                    TabLayout.Tab tab = tabLayout.getTabAt(position);
                    if (tab != null) {
                        View tabView = tab.getCustomView();
                        if (tabView != null) {
                            tabView.findViewById(R.id.closeTabButton).setVisibility(View.GONE);
                        }
                    }
                })
                .show();
    }

    private void setupButtonListeners() {
        binding.backButton.setOnClickListener(v -> navigateBack());
        binding.forwardButton.setOnClickListener(v -> navigateForward());
        binding.homeButton.setOnClickListener(v -> loadUrl("https://www.google.com"));
        binding.reloadButton.setOnClickListener(v -> reload());
        binding.newTabButton.setOnClickListener(v -> adapter.addTab());
        urlBar.setOnEditorActionListener((v, actionId, event) -> {
            String url = urlBar.getText().toString().trim();
            if (!url.isEmpty() && !url.startsWith("http")) url = "https://" + url;
            loadUrl(url);
            return true;
        });
    }

    private void loadUrl(String url) {
        WebView currentWebView = adapter.getCurrentWebView();
        if (currentWebView != null && url != null && !url.isEmpty()) {
            currentWebView.loadUrl(url);
            addToHistory(url, currentWebView.getTitle());
        }
    }

    private void navigateBack() {
        WebView currentWebView = adapter.getCurrentWebView();
        if (currentWebView != null && currentWebView.canGoBack()) {
            currentWebView.goBack();
        } else {
            showToast("No back history");
        }
    }

    private void navigateForward() {
        WebView currentWebView = adapter.getCurrentWebView();
        if (currentWebView != null && currentWebView.canGoForward()) {
            currentWebView.goForward();
        } else {
            showToast("No forward history");
        }
    }

    private void reload() {
        WebView currentWebView = adapter.getCurrentWebView();
        if (currentWebView != null) {
            currentWebView.reload();
        }
    }

    private void updateProgressBarVisibility() {
        WebView currentWebView = adapter.getCurrentWebView();
        if (currentWebView != null) {
            progressBar.setVisibility(currentWebView.getProgress() < 100 ? View.VISIBLE : View.GONE);
        }
    }

    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    private void addBookmark(String url) {
        Executors.newSingleThreadExecutor().execute(() -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Set<String> bookmarks = sharedPreferences.getStringSet(BOOKMARKS_KEY, new HashSet<>());
            bookmarks.add(url);
            editor.putStringSet(BOOKMARKS_KEY, bookmarks).apply();
            showToast("Bookmark added: " + url);
        });
    }

    private void addToHistory(String url, String title) {
        Executors.newSingleThreadExecutor().execute(() -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Set<String> history = sharedPreferences.getStringSet(HISTORY_KEY, new HashSet<>());
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            String historyEntry = timestamp + " | " + (title != null && !title.isEmpty() ? title : "No Title") + " | " + url;
            history.add(historyEntry);
            editor.putStringSet(HISTORY_KEY, history).apply();
        });
    }

    private void addToDownloads(String url) {
        Executors.newSingleThreadExecutor().execute(() -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Set<String> downloads = sharedPreferences.getStringSet(DOWNLOADS_KEY, new HashSet<>());
            downloads.add(url);
            editor.putStringSet(DOWNLOADS_KEY, downloads).apply();
            new Handler(Looper.getMainLooper()).post(() ->
                    showToast("Added to Downloads: " + url));
        });
    }

    private void showBookmarks() {
        Set<String> bookmarks = sharedPreferences.getStringSet(BOOKMARKS_KEY, new HashSet<>());
        List<String> bookmarkList = new ArrayList<>(bookmarks);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bookmarkList);
        new AlertDialog.Builder(this)
                .setTitle("Bookmarks")
                .setAdapter(adapter, (dialog, which) -> loadUrl(bookmarkList.get(which)))
                .setNegativeButton("Close", null)
                .show();
    }

    private void showHistory() {
        Set<String> history = sharedPreferences.getStringSet(HISTORY_KEY, new HashSet<>());
        List<String> historyList = new ArrayList<>(history);

        HistoryAdapter adapter = new HistoryAdapter(this, historyList);

        new AlertDialog.Builder(this)
                .setTitle("History")
                .setAdapter(adapter, (dialog, which) -> {
                    String[] parts = historyList.get(which).split(" \\| ");
                    if (parts.length == 3) {
                        loadUrl(parts[2]);
                    }
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showDownloads() {
        Set<String> downloads = sharedPreferences.getStringSet(DOWNLOADS_KEY, new HashSet<>());
        List<String> downloadsList = new ArrayList<>(downloads);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, downloadsList);
        new AlertDialog.Builder(this)
                .setTitle("Downloads")
                .setAdapter(adapter, (dialog, which) -> showToast("Opening: " + downloadsList.get(which)))
                .setNegativeButton("Close", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.browser_menu, menu);
        Menu settingsMenu = menu.addSubMenu("Settings");
        settingsMenu.add(0, R.id.menu_dark_mode, 0, "Dark Mode").setCheckable(true);
        settingsMenu.add(0, R.id.menu_light_mode, 0, "Light Mode").setCheckable(true);
        boolean isDarkMode = sharedPreferences.getBoolean(DARK_MODE_KEY, false);
        settingsMenu.findItem(R.id.menu_dark_mode).setChecked(isDarkMode);
        settingsMenu.findItem(R.id.menu_light_mode).setChecked(!isDarkMode);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        WebView currentWebView = adapter.getCurrentWebView();
        if (currentWebView == null) return false;

        int id = item.getItemId();
        if (id == R.id.menu_close_tab) {
            adapter.closeTab(viewPager.getCurrentItem());
            return true;
        } else if (id == R.id.menu_bookmarks) {
            showBookmarks();
            return true;
        } else if (id == R.id.menu_history) {
            showHistory();
            return true;
        } else if (id == R.id.menu_downloads) {
            showDownloads();
            return true;
        } else if (id == R.id.menu_exit) {
            finish();
            return true;
        } else if (id == R.id.menu_dark_mode) {
            sharedPreferences.edit().putBoolean(DARK_MODE_KEY, true).apply();
            applyTheme(true);
            return true;
        } else if (id == R.id.menu_light_mode) {
            sharedPreferences.edit().putBoolean(DARK_MODE_KEY, false).apply();
            applyTheme(false);
            return true;
        } else if (id == R.id.menu_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            intent.putExtra("previousUrl", currentWebView.getUrl());
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        WebView currentWebView = adapter.getCurrentWebView();
        if (currentWebView != null && currentWebView.canGoBack()) {
            currentWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        WebView webView = (WebView) v;
        WebView.HitTestResult result = webView.getHitTestResult();

        if (result.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
            menu.setHeaderTitle("Image Options");
            menu.add(0, v.getId(), 0, "Download Image").setOnMenuItemClickListener(item -> {
                String imageUrl = result.getExtra();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    addToDownloads(imageUrl);
                }
                return true;
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            for (WebView webView : adapter.webViews) {
                webView.destroy();
            }
            adapter.webViews.clear();
        }
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            progressBar.setVisibility(View.GONE);
            urlBar.setText(url);
            updateProgressBarVisibility();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            loadUrl(url);
            return true;
        }
    }

    private class CustomWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            progressBar.setProgress(newProgress);
            updateProgressBarVisibility();
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            addToHistory(view.getUrl(), title);
        }
    }

    private class WebViewAdapter extends RecyclerView.Adapter<WebViewAdapter.ViewHolder> {
        private final AppCompatActivity activity;
        private final List<WebView> webViews = new ArrayList<>();

        public WebViewAdapter(AppCompatActivity activity) {
            this.activity = activity;
            addTab();
        }

        public void addTab() {
            if (webViews.size() >= MAX_TABS) {
                showToast("Maximum tab limit reached");
                return;
            }
            WebView webView = new WebView(activity);
            WebSettings webSettings = webView.getSettings();
            boolean isJavaScriptEnabled = activity.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .getBoolean(JAVASCRIPT_KEY, true);
            webSettings.setJavaScriptEnabled(isJavaScriptEnabled);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setDatabaseEnabled(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            webView.setNestedScrollingEnabled(false);
            webView.setWebViewClient(new CustomWebViewClient());
            webView.setWebChromeClient(new CustomWebChromeClient());

            activity.registerForContextMenu(webView);

            webView.loadUrl("https://www.google.com");
            webViews.add(webView);
            notifyItemInserted(webViews.size() - 1);
        }

        public void closeTab(int position) {
            if (webViews.size() <= 1) {
                showToast("Cannot close the last tab");
                return;
            }
            if (position >= 0 && position < webViews.size()) {
                WebView webView = webViews.remove(position);
                webView.destroy();
                notifyItemRemoved(position);
                if (position == viewPager.getCurrentItem()) {
                    viewPager.setCurrentItem(Math.max(0, position - 1));
                }
            }
        }

        public WebView getCurrentWebView() {
            int position = viewPager.getCurrentItem();
            return position >= 0 && position < webViews.size() ? webViews.get(position) : null;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            FrameLayout frameLayout = new FrameLayout(parent.getContext());
            frameLayout.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            return new ViewHolder(frameLayout);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.frameLayout.removeAllViews();
            if (position < webViews.size()) {
                holder.frameLayout.addView(webViews.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return webViews.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            FrameLayout frameLayout;

            public ViewHolder(FrameLayout frameLayout) {
                super(frameLayout);
                this.frameLayout = frameLayout;
            }
        }
    }

    private class HistoryAdapter extends ArrayAdapter<String> {
        private final List<String> historyList;

        public HistoryAdapter(Context context, List<String> historyList) {
            super(context, 0, historyList);
            this.historyList = historyList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.history_item, parent, false);
            }

            TextView timestampTextView = convertView.findViewById(R.id.timestampTextView);
            TextView titleTextView = convertView.findViewById(R.id.titleTextView);
            TextView urlTextView = convertView.findViewById(R.id.urlTextView);

            String historyEntry = historyList.get(position);
            String[] parts = historyEntry.split(" \\| ");

            if (parts.length == 3) {
                timestampTextView.setText(parts[0]);
                titleTextView.setText(parts[1]);
                urlTextView.setText(parts[2]);
            }

            return convertView;
        }
    }
}
