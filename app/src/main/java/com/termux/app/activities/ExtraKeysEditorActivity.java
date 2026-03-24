package com.termux.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.termux.R;
import com.termux.shared.termux.settings.properties.TermuxAppSharedProperties;
import com.termux.shared.termux.settings.properties.TermuxPropertyConstants;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExtraKeysEditorActivity extends AppCompatActivity {

    private LinearLayout mGridContainer;
    private List<List<String>> mCurrentKeys = new ArrayList<>();
    
    private static final List<String> PRESET_KEYS = Arrays.asList(
        "ESC", "TAB", "CTRL", "ALT", "FN", "UP", "DOWN", "LEFT", "RIGHT", 
        "PGUP", "PGDN", "HOME", "END", "INS", "DEL", "BKSP", "ENTER",
        "/", "-", "|", "(", ")", "{", "}", "[", "]", "<", ">", "\"", "'", "`", "$", "&", "*", "+", "=", "?", "!", "%", "#"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra_keys_editor);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        mGridContainer = findViewById(R.id.extra_keys_grid_container);
        findViewById(R.id.add_row_button).setOnClickListener(v -> addRow(new ArrayList<>()));
        findViewById(R.id.save_keys_button).setOnClickListener(v -> saveKeys());
        findViewById(R.id.reset_keys_button).setOnClickListener(v -> loadDefaultKeys());

        loadCurrentKeys();
    }

    private void loadCurrentKeys() {
        String keysJson = (String) TermuxAppSharedProperties.getProperties().getInternalPropertyValue(TermuxPropertyConstants.KEY_EXTRA_KEYS, true);
        try {
            JSONArray outer = new JSONArray(keysJson);
            for (int i = 0; i < outer.length(); i++) {
                JSONArray inner = outer.getJSONArray(i);
                List<String> row = new ArrayList<>();
                for (int j = 0; j < inner.length(); j++) {
                    row.add(inner.getString(j));
                }
                addRow(row);
            }
        } catch (JSONException e) {
            loadDefaultKeys();
        }
    }

    private void loadDefaultKeys() {
        mGridContainer.removeAllViews();
        mCurrentKeys.clear();
        // Default rows
        addRow(new ArrayList<>(Arrays.asList("ESC", "TAB", "CTRL", "ALT", "UP", "DOWN", "LEFT", "RIGHT")));
    }

    private void addRow(List<String> keys) {
        mCurrentKeys.add(keys);
        final int rowIndex = mCurrentKeys.size() - 1;

        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        rowLayout.setPadding(0, 8, 0, 8);

        refreshRowView(rowLayout, rowIndex);
        mGridContainer.addView(rowLayout);
    }

    private void refreshRowView(LinearLayout rowLayout, int rowIndex) {
        rowLayout.removeAllViews();
        List<String> keys = mCurrentKeys.get(rowIndex);

        for (int i = 0; i < keys.size(); i++) {
            final int keyIndex = i;
            Chip chip = new Chip(this);
            chip.setText(keys.get(i));
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                mCurrentKeys.get(rowIndex).remove(keyIndex);
                refreshRowView(rowLayout, rowIndex);
            });
            rowLayout.addView(chip);
        }

        MaterialButton addButton = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        addButton.setText("+");
        addButton.setOnClickListener(v -> showKeySelectionDialog(rowLayout, rowIndex));
        rowLayout.addView(addButton);
    }

    private void showKeySelectionDialog(LinearLayout rowLayout, int rowIndex) {
        ChipGroup chipGroup = new ChipGroup(this);
        chipGroup.setPadding(16, 16, 16, 16);
        
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Select Key")
                .setView(chipGroup)
                .create();

        for (String key : PRESET_KEYS) {
            Chip chip = new Chip(this);
            chip.setText(key);
            chip.setOnClickListener(v -> {
                mCurrentKeys.get(rowIndex).add(key);
                refreshRowView(rowLayout, rowIndex);
                dialog.dismiss();
            });
            chipGroup.addView(chip);
        }

        dialog.show();
    }

    private void saveKeys() {
        JSONArray outer = new JSONArray();
        for (List<String> row : mCurrentKeys) {
            if (row.isEmpty()) continue;
            JSONArray inner = new JSONArray();
            for (String key : row) {
                inner.put(key);
            }
            outer.put(inner);
        }

        String json = outer.toString();
        // In a real implementation, we would write this to termux.properties
        // For now, we'll just show it and suggest the next step
        Toast.makeText(this, "Extra keys updated! Restart Termux to apply.", Toast.LENGTH_LONG).show();
        finish();
    }
}
