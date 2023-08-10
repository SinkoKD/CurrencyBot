package org.example.bot;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {
    private String name;
    private String UID;
    private boolean registered;
    private boolean deposited;
    private Date lastTimeTexted;
    private int timesTextWasSent;
    private boolean canWriteToSupport;

    public boolean isCanWriteToSupport() {
        return canWriteToSupport;
    }

    public void setCanWriteToSupport(boolean canWriteToSupport) {
        this.canWriteToSupport = canWriteToSupport;
    }

    public User(String name, String UID, boolean registered, boolean deposited, Date lastTimeTexted, int timesTextWasSent, boolean canWriteToSupport) {
        this.name = name;
        this.UID = UID;
        this.registered = registered;
        this.deposited = deposited;
        this.lastTimeTexted = lastTimeTexted;
        this.timesTextWasSent = timesTextWasSent;
        this.canWriteToSupport = canWriteToSupport;
    }

    public String getUID() {
        return UID;
    }

    public int getTimesTextWasSent() {
        return timesTextWasSent;
    }

    public void setTimesTextWasSent(int timesTextWasSent) {
        this.timesTextWasSent = timesTextWasSent;
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