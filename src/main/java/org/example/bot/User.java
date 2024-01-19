package org.example.bot;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {
    private String name;
    private String UID;
    private boolean registered;

    public String getLanguageSelected() {
        return languageSelected;
    }

    public void setLanguageSelected(String languageSelected) {
        this.languageSelected = languageSelected;
    }

    private boolean deposited;
    private Date lastTimeTexted;
    private Date lastTimePressedDeposit;
    private int timesTextWasSent;
    private String languageSelected;

    public int getMinimumPercent() {
        return minimumPercent;
    }

    public void setMinimumPercent(int minimumPercent) {
        this.minimumPercent = minimumPercent;
    }

    public User(String name, String UID, boolean registered, boolean deposited,
                Date lastTimeTexted, Date lastTimePressedDeposit, int timesTextWasSent,
                String languageSelected, boolean canWriteToSupport, boolean canPressDeposit,
                boolean canPressRegister, int minimumPercent) {
        this.name = name;
        this.UID = UID;
        this.registered = registered;
        this.deposited = deposited;
        this.lastTimeTexted = lastTimeTexted;
        this.lastTimePressedDeposit = lastTimePressedDeposit;
        this.timesTextWasSent = timesTextWasSent;
        this.languageSelected = languageSelected;
        this.canWriteToSupport = canWriteToSupport;
        this.canPressDeposit = canPressDeposit;
        this.canPressRegister = canPressRegister;
        this.minimumPercent = minimumPercent;
    }

    private boolean canWriteToSupport;
    private boolean canPressDeposit;
    private boolean canPressRegister;
    private int minimumPercent;

    public boolean isCanWriteToSupport() {
        return canWriteToSupport;
    }

    public void setCanWriteToSupport(boolean canWriteToSupport) {
        this.canWriteToSupport = canWriteToSupport;
    }



    public Date getLastTimePressedDeposit() {
        return lastTimePressedDeposit;
    }

    public void setLastTimePressedDeposit(Date lastTimePressedDeposit) {
        this.lastTimePressedDeposit = lastTimePressedDeposit;
    }

    public boolean isCanPressDeposit() {
        return canPressDeposit;
    }

    public void setCanPressDeposit(boolean canPressDeposit) {
        this.canPressDeposit = canPressDeposit;
    }

    public boolean isCanPressRegister() {
        return canPressRegister;
    }

    public void setCanPressRegister(boolean canPressRegister) {
        this.canPressRegister = canPressRegister;
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