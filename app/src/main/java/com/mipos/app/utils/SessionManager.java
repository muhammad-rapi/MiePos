package com.mipos.app.utils;

import com.mipos.app.models.User;

public class SessionManager {
    private static final SessionManager instance = new SessionManager();
    private volatile User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        return instance;
    }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }
    public void clear() { currentUser = null; }
}
