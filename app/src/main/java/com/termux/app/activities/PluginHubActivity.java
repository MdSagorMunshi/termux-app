package com.termux.app.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.termux.R;
import com.termux.shared.termux.TermuxConstants;

import java.util.ArrayList;
import java.util.List;

public class PluginHubActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin_hub);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.plugin_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<PluginInfo> plugins = new ArrayList<>();
        plugins.add(new PluginInfo("Termux:API", "Expose Android API to command line. Access camera, sensors, SMS, and more.", TermuxConstants.TERMUX_API_PACKAGE_NAME));
        plugins.add(new PluginInfo("Termux:Boot", "Run scripts when your device boots up.", TermuxConstants.TERMUX_BOOT_PACKAGE_NAME));
        plugins.add(new PluginInfo("Termux:Float", "Run Termux in a floating window.", TermuxConstants.TERMUX_FLOAT_PACKAGE_NAME));
        plugins.add(new PluginInfo("Termux:Styling", "Customize fonts and color schemes easily.", TermuxConstants.TERMUX_STYLING_PACKAGE_NAME));
        plugins.add(new PluginInfo("Termux:Tasker", "Integrate Termux with Tasker automation.", TermuxConstants.TERMUX_TASKER_PACKAGE_NAME));
        plugins.add(new PluginInfo("Termux:Widget", "Create home screen shortcuts for your scripts.", TermuxConstants.TERMUX_WIDGET_PACKAGE_NAME));

        recyclerView.setAdapter(new PluginAdapter(this, plugins));
    }

    private static class PluginInfo {
        String name;
        String description;
        String packageName;

        PluginInfo(String name, String description, String packageName) {
            this.name = name;
            this.description = description;
            this.packageName = packageName;
        }

        boolean isInstalled(PackageManager pm) {
            try {
                pm.getPackageInfo(packageName, 0);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }
    }

    private static class PluginAdapter extends RecyclerView.Adapter<PluginAdapter.ViewHolder> {
        private final Context context;
        private final List<PluginInfo> plugins;
        private final PackageManager pm;

        PluginAdapter(Context context, List<PluginInfo> plugins) {
            this.context = context;
            this.plugins = plugins;
            this.pm = context.getPackageManager();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plugin_card, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PluginInfo plugin = plugins.get(position);
            holder.name.setText(plugin.name);
            holder.description.setText(plugin.description);

            boolean installed = plugin.isInstalled(pm);
            if (installed) {
                holder.statusChip.setText("Installed");
                holder.statusChip.setChipBackgroundColorResource(android.R.color.holo_green_dark);
                holder.statusChip.setTextColor(context.getColor(android.R.color.white));
                holder.actionButton.setText("Open Settings");
                holder.actionButton.setOnClickListener(v -> {
                    Intent intent = pm.getLaunchIntentForPackage(plugin.packageName);
                    if (intent != null) {
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "No settings activity found for this plugin.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                holder.statusChip.setText("Not Installed");
                holder.statusChip.setChipBackgroundColorResource(android.R.color.darker_gray);
                holder.statusChip.setTextColor(context.getColor(android.R.color.white));
                holder.actionButton.setText("Install from F-Droid");
                holder.actionButton.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://f-droid.org/packages/" + plugin.packageName));
                    context.startActivity(intent);
                });
            }
        }

        @Override
        public int getItemCount() {
            return plugins.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, description;
            Chip statusChip;
            Button actionButton;

            ViewHolder(View view) {
                super(view);
                name = view.findViewById(R.id.plugin_name);
                description = view.findViewById(R.id.plugin_description);
                statusChip = view.findViewById(R.id.plugin_status_chip);
                actionButton = view.findViewById(R.id.plugin_action_button);
            }
        }
    }
}
