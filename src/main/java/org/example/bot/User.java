package org.example.bot;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {
    private String name;
    private String UID;
    private boolean registered;
    private boolean deposited;
    private Date lastTimeTexted;

    public User(String name, String UID, boolean registered, boolean deposited, Date lastTimeTexted) {
        this.name = name;
        this.UID = UID;
        this.registered = registered;
        this.deposited = deposited;
        this.lastTimeTexted = lastTimeTexted;
    }

    public String getUID() {
        return UID;
    }

    public Date getLastTimeTexted() {
        return lastTimeTexted;
    }

    public void setLastTimeTexted(Date lastTimeTexted) {
        this.lastTimeTexted = lastTimeTexted;
    }

    public boolean isDeposited() {
        return deposited;
    }

    public void setDeposited(boolean deposited) {
        this.deposited = deposited;
    }

    public User() {
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