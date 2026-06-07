package com.mipos.app.models;

public class User {
    private String uid;
    private String name;
    private String role;
    private String email;

    public User() {}

    public User(String uid, String name, String role, String email) {
        this.uid = uid;
        this.name = name;
        this.role = role;
        this.email = email;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isOwner() { return "owner".equals(role); }
}
