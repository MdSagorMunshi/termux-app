package com.termux.app.terminal.session;

import com.termux.shared.termux.shell.TermuxShellManager;
import com.termux.shared.termux.shell.command.runner.terminal.TermuxSession;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages sessions with filtering and workspace support.
 */
public class SessionManager {

    private final TermuxShellManager mShellManager;
    private String mCurrentFilter = "";
    private String mCurrentWorkspace = null; // null means "All" or "Default"

    public SessionManager(TermuxShellManager shellManager) {
        this.mShellManager = shellManager;
    }

    public List<TermuxSession> getFilteredSessions() {
        List<TermuxSession> sessions = mShellManager.mTermuxSessions;
        
        return sessions.stream()
                .filter(s -> mCurrentWorkspace == null || s.getWorkspaceId().equals(mCurrentWorkspace))
                .filter(s -> mCurrentFilter.isEmpty() || 
                        (s.getTerminalSession().mSessionName != null && s.getTerminalSession().mSessionName.contains(mCurrentFilter)) ||
                        (s.getTerminalSession().getTitle() != null && s.getTerminalSession().getTitle().contains(mCurrentFilter)))
                .sorted((s1, s2) -> {
                    // Pinned sessions first
                    if (s1.isPinned() && !s2.isPinned()) return -1;
                    if (!s1.isPinned() && s2.isPinned()) return 1;
                    return 0;
                })
                .collect(Collectors.toList());
    }

    public void setFilter(String filter) {
        this.mCurrentFilter = filter;
    }

    public void setWorkspace(String workspaceId) {
        this.mCurrentWorkspace = workspaceId;
    }

    public List<String> getAllWorkspaces() {
        return mShellManager.mTermuxSessions.stream()
                .map(TermuxSession::getWorkspaceId)
                .distinct()
                .collect(Collectors.toList());
    }

    public void pinSession(TermuxSession session, boolean pin) {
        session.setPinned(pin);
    }

    public void moveSessionToWorkspace(TermuxSession session, String workspaceId) {
        session.setWorkspaceId(workspaceId);
    }
}
