package com.termux.app.terminal.session;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.termux.R;
import com.termux.shared.termux.shell.command.runner.terminal.TermuxSession;
import com.termux.shared.theme.ThemeUtils;
import com.termux.terminal.TerminalSession;

import java.util.List;

public class ModernSessionAdapter extends RecyclerView.Adapter<ModernSessionAdapter.ViewHolder> {

    public interface OnSessionClickListener {
        void onSessionClick(TermuxSession session);
        void onSessionLongClick(TermuxSession session);
    }

    private final SessionManager mSessionManager;
    private final OnSessionClickListener mListener;
    private int mSelectedPosition = 0;

    public ModernSessionAdapter(SessionManager sessionManager, OnSessionClickListener listener) {
        this.mSessionManager = sessionManager;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session_modern, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TermuxSession termuxSession = mSessionManager.getFilteredSessions().get(position);
        TerminalSession session = termuxSession.getTerminalSession();

        // Animation
        holder.itemView.setAlpha(0f);
        holder.itemView.setTranslationY(20f);
        holder.itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setStartDelay(position * 50L)
                .start();

        String name = session.mSessionName;
        String title = session.getTitle();

        holder.titleView.setText(name != null ? name : "Session " + (position + 1));
        holder.subtitleView.setText(title != null ? title : "No activity");

        boolean isRunning = session.isRunning();
        if (isRunning) {
            holder.titleView.setPaintFlags(holder.titleView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.titleView.setPaintFlags(holder.titleView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        holder.pinIcon.setVisibility(termuxSession.isPinned() ? View.VISIBLE : View.GONE);

        // Highlight selected
        if (position == mSelectedPosition) {
            int primaryColor = ThemeUtils.getSystemAttrColor(holder.cardView.getContext(), com.google.android.material.R.attr.colorPrimary);
            int primaryContainerColor = ThemeUtils.getSystemAttrColor(holder.cardView.getContext(), com.google.android.material.R.attr.colorPrimaryContainer);
            holder.cardView.setStrokeColor(primaryColor);
            holder.cardView.setCardBackgroundColor(primaryContainerColor);
        } else {
            holder.cardView.setStrokeColor(Color.TRANSPARENT);
            holder.cardView.setCardBackgroundColor(Color.TRANSPARENT);
        }

        holder.itemView.setOnClickListener(v -> {
            mSelectedPosition = holder.getAdapterPosition();
            notifyDataSetChanged();
            mListener.onSessionClick(termuxSession);
        });

        holder.itemView.setOnLongClickListener(v -> {
            mListener.onSessionLongClick(termuxSession);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return mSessionManager.getFilteredSessions().size();
    }

    public void setSelectedSession(TerminalSession session) {
        List<TermuxSession> sessions = mSessionManager.getFilteredSessions();
        for (int i = 0; i < sessions.size(); i++) {
            if (sessions.get(i).getTerminalSession() == session) {
                mSelectedPosition = i;
                notifyDataSetChanged();
                break;
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView cardView;
        final TextView titleView;
        final TextView subtitleView;
        final ImageView sessionIcon;
        final ImageView pinIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            titleView = itemView.findViewById(R.id.session_title);
            subtitleView = itemView.findViewById(R.id.session_subtitle);
            sessionIcon = itemView.findViewById(R.id.session_icon);
            pinIcon = itemView.findViewById(R.id.pin_icon);
        }
    }
}
