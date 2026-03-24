package com.termux.app.terminal.suggestions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.termux.shared.logger.Logger;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages command history and provides suggestions based on current input.
 */
public class CommandSuggestionManager {

    private static final String LOG_TAG = "CommandSuggestionManager";
    private static final int MAX_HISTORY_SIZE = 1000;

    private final Set<String> mHistory = new LinkedHashSet<>();
    private String mCurrentLineBuffer = "";
    private String mCurrentSuggestion = null;

    private static CommandSuggestionManager sInstance;

    private CommandSuggestionManager() {}

    public static synchronized CommandSuggestionManager getInstance() {
        if (sInstance == null) {
            sInstance = new CommandSuggestionManager();
        }
        return sInstance;
    }

    /**
     * Intercept data sent to the terminal to track the current command line.
     */
    public synchronized void onDataSent(byte[] data, int offset, int count) {
        String input = new String(data, offset, count);
        for (char c : input.toCharArray()) {
            if (c == '\r' || c == '\n') {
                if (!mCurrentLineBuffer.trim().isEmpty()) {
                    addHistory(mCurrentLineBuffer.trim());
                }
                mCurrentLineBuffer = "";
                mCurrentSuggestion = null;
            } else if (c == 127 || c == 8) { // Backspace
                if (mCurrentLineBuffer.length() > 0) {
                    mCurrentLineBuffer = mCurrentLineBuffer.substring(0, mCurrentLineBuffer.length() - 1);
                }
            } else if (c >= 32) { // Printable characters
                mCurrentLineBuffer += c;
            }
        }
        updateSuggestion();
    }

    private void addHistory(String command) {
        mHistory.remove(command);
        mHistory.add(command);
        if (mHistory.size() > MAX_HISTORY_SIZE) {
            String first = mHistory.iterator().next();
            mHistory.remove(first);
        }
    }

    private void updateSuggestion() {
        if (mCurrentLineBuffer.isEmpty()) {
            mCurrentSuggestion = null;
            return;
        }

        String bestMatch = null;
        // Search in reverse order (most recent first)
        List<String> list = new ArrayList<>(mHistory);
        for (int i = list.size() - 1; i >= 0; i--) {
            String cmd = list.get(i);
            if (cmd.startsWith(mCurrentLineBuffer) && cmd.length() > mCurrentLineBuffer.length()) {
                bestMatch = cmd;
                break;
            }
        }
        mCurrentSuggestion = bestMatch;
    }

    @Nullable
    public synchronized String getCurrentSuggestion() {
        if (mCurrentSuggestion == null || mCurrentLineBuffer.isEmpty()) return null;
        return mCurrentSuggestion.substring(mCurrentLineBuffer.length());
    }

    public synchronized String getFullSuggestion() {
        return mCurrentSuggestion;
    }

    public synchronized void clearHistory() {
        mHistory.clear();
        mCurrentLineBuffer = "";
        mCurrentSuggestion = null;
    }
}
