package com.termux.app.terminal.session;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.termux.R;
import com.termux.shared.termux.shell.command.runner.terminal.TermuxSession;
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
    private int mLastAnimatedPosition = -1;

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
        Context context = holder.itemView.getContext();

        // Staggered entry animation
        if (position > mLastAnimatedPosition) {
            animateItemEntry(holder.itemView, position);
            mLastAnimatedPosition = position;
        }

        String name = session.mSessionName;
        String title = session.getTitle();

        holder.titleView.setText(name != null ? name : "Session " + (position + 1));
        holder.subtitleView.setText(title != null ? title : "No activity");

        boolean isRunning = session.isRunning();

        // Update strike-through for finished sessions
        if (isRunning) {
            holder.titleView.setPaintFlags(holder.titleView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.titleView.setPaintFlags(holder.titleView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        // LED status indicator
        if (holder.statusIndicator != null) {
            if (isRunning) {
                holder.statusIndicator.setBackgroundResource(R.drawable.session_active_indicator);
            } else {
                holder.statusIndicator.setBackgroundResource(R.drawable.session_idle_indicator);
            }
        }

        // Pin icon
        holder.pinIcon.setVisibility(termuxSession.isPinned() ? View.VISIBLE : View.GONE);

        // Session icon tinting based on state
        if (holder.sessionIcon != null) {
            int iconTint;
            if (position == mSelectedPosition) {
                iconTint = ContextCompat.getColor(context, R.color.terminux_accent_primary);
            } else if (isRunning) {
                iconTint = ContextCompat.getColor(context, R.color.terminux_on_surface);
            } else {
                iconTint = ContextCompat.getColor(context, R.color.terminux_on_surface_dim);
            }
            holder.sessionIcon.setColorFilter(iconTint);
        }

        // Highlight selected session with TerminuX accent
        if (position == mSelectedPosition) {
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.terminux_accent_primary));
            holder.cardView.setStrokeWidth(dpToPx(context, 1.5f));
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.terminux_card_selected_bg));
            holder.titleView.setTextColor(ContextCompat.getColor(context, R.color.terminux_accent_primary));
        } else {
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.terminux_card_stroke));
            holder.cardView.setStrokeWidth(dpToPx(context, 1f));
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.terminux_card_bg));
            holder.titleView.setTextColor(ContextCompat.getColor(context, R.color.terminux_on_bg));
        }

        holder.itemView.setOnClickListener(v -> {
            int prev = mSelectedPosition;
            mSelectedPosition = holder.getAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(mSelectedPosition);
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
                int prev = mSelectedPosition;
                mSelectedPosition = i;
                notifyItemChanged(prev);
                notifyItemChanged(mSelectedPosition);
                break;
            }
        }
    }

    public void resetAnimationPosition() {
        mLastAnimatedPosition = -1;
    }

    private void animateItemEntry(View view, int position) {
        view.setAlpha(0f);
        view.setTranslationY(30f);
        view.setScaleX(0.95f);
        view.setScaleY(0.95f);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(
            ObjectAnimator.ofFloat(view, "alpha", 0f, 1f),
            ObjectAnimator.ofFloat(view, "translationY", 30f, 0f),
            ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f),
            ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1f)
        );
        animSet.setDuration(350);
        animSet.setStartDelay(position * 60L);
        animSet.setInterpolator(new DecelerateInterpolator(1.5f));
        animSet.start();
    }

    private int dpToPx(Context context, float dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView cardView;
        final TextView titleView;
        final TextView subtitleView;
        final ImageView sessionIcon;
        final ImageView pinIcon;
        final View statusIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            titleView = itemView.findViewById(R.id.session_title);
            subtitleView = itemView.findViewById(R.id.session_subtitle);
            sessionIcon = itemView.findViewById(R.id.session_icon);
            pinIcon = itemView.findViewById(R.id.pin_icon);
            statusIndicator = itemView.findViewById(R.id.session_status_indicator);
        }
    }
}
