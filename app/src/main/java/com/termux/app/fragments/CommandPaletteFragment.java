package com.termux.app.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.termux.R;
import com.termux.app.TermuxActivity;
import com.termux.shared.termux.shell.command.runner.terminal.TermuxSession;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandPaletteFragment extends BottomSheetDialogFragment {

    private List<PaletteItem> mAllItems = new ArrayList<>();
    private List<PaletteItem> mFilteredItems = new ArrayList<>();
    private PaletteAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_command_palette, container, false);

        TermuxActivity activity = (TermuxActivity) getActivity();
        if (activity == null) return view;

        setupItems(activity);

        EditText searchInput = view.findViewById(R.id.palette_search_input);
        RecyclerView recyclerView = view.findViewById(R.id.palette_recycler_view);

        mFilteredItems.addAll(mAllItems);
        mAdapter = new PaletteAdapter(mFilteredItems, item -> {
            item.action.run();
            dismiss();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void setupItems(TermuxActivity activity) {
        // Actions
        mAllItems.add(new PaletteItem("New Session", "Create a new terminal session", () -> activity.getTermuxTerminalSessionClient().addNewSession(false, null)));
        mAllItems.add(new PaletteItem("Plugin Hub", "Manage Termux plugins", () -> activity.startActivity(new android.content.Intent(activity, com.termux.app.activities.PluginHubActivity.class))));
        mAllItems.add(new PaletteItem("Extra Keys Editor", "Customize toolbar keys", () -> activity.startActivity(new android.content.Intent(activity, com.termux.app.activities.ExtraKeysEditorActivity.class))));
        mAllItems.add(new PaletteItem("Settings", "App preferences", () -> activity.startActivity(new android.content.Intent(activity, com.termux.app.activities.SettingsActivity.class))));

        // Sessions
        for (TermuxSession session : activity.getTermuxService().getTermuxSessions()) {
            String name = session.getTerminalSession().mSessionName;
            String title = session.getTerminalSession().getTitle();
            mAllItems.add(new PaletteItem("Switch to: " + (name != null ? name : "Session"), title != null ? title : "", () -> {
                activity.getTermuxTerminalSessionClient().setCurrentSession(session.getTerminalSession());
            }));
        }
    }

    private void filter(String query) {
        mFilteredItems.clear();
        if (query.isEmpty()) {
            mFilteredItems.addAll(mAllItems);
        } else {
            String lowerQuery = query.toLowerCase();
            mFilteredItems.addAll(mAllItems.stream()
                    .filter(item -> item.title.toLowerCase().contains(lowerQuery) || item.subtitle.toLowerCase().contains(lowerQuery))
                    .collect(Collectors.toList()));
        }
        mAdapter.notifyDataSetChanged();
    }

    private static class PaletteItem {
        String title;
        String subtitle;
        Runnable action;

        PaletteItem(String title, String subtitle, Runnable action) {
            this.title = title;
            this.subtitle = subtitle;
            this.action = action;
        }
    }

    private static class PaletteAdapter extends RecyclerView.Adapter<PaletteAdapter.ViewHolder> {
        private final List<PaletteItem> items;
        private final OnItemClickListener listener;

        interface OnItemClickListener {
            void onItemClick(PaletteItem item);
        }

        PaletteAdapter(List<PaletteItem> items, OnItemClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PaletteItem item = items.get(position);
            holder.text1.setText(item.title);
            holder.text2.setText(item.subtitle);
            holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;

            ViewHolder(View v) {
                super(v);
                text1 = v.findViewById(android.R.id.text1);
                text2 = v.findViewById(android.R.id.text2);
            }
        }
    }
}
