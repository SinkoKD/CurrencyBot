package org.example.bot;

import java.io.Serializable;

public class User implements Serializable {
    private String name;
    private String UID;
    private boolean registered;
    private boolean deposited;


    public String getUID() {
        return UID;
    }

    public boolean isDeposited() {
        return deposited;
    }

    public void setDeposited(boolean deposited) {
        this.deposited = deposited;
    }

    public User(String name, String UID, boolean registered, boolean deposited) {
        this.name = name;
        this.UID = UID;
;
        this.registered = registered;
        this.deposited = deposited;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
