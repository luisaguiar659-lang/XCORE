package com.xcore.app.support;

public class SupportGroup {
    public enum Unit { MINUTES, HOURS }
    private final String id, groupName, message;
    private final long interval;
    private final Unit unit;
    private final boolean active;

    public SupportGroup(String id, String groupName, String message, long interval, Unit unit, boolean active) {
        this.id=id; this.groupName=groupName; this.message=message; this.interval=interval; this.unit=unit; this.active=active;
    }
    public String getId(){return id;}
    public String getGroupName(){return groupName;}
    public String getMessage(){return message;}
    public long getInterval(){return interval;}
    public Unit getUnit(){return unit;}
    public boolean isActive(){return active;}
}
