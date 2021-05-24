package com.playtime.util.objects;

public class Groups {
    private final String groupName;
    private final int timeRequired;
    private final String rankUpCommand;
    private final String broadcastMessage;
    private final String playerTitleMessage;


    public Groups(String groupName, int timeRequired, String rankUpCommand, String broadcastMessage, String playerTitleMessage) {
        this.groupName = groupName;
        this.timeRequired = timeRequired;
        this.rankUpCommand = rankUpCommand;
        this.broadcastMessage = broadcastMessage;
        this.playerTitleMessage = playerTitleMessage;
    }

    public String getGroupName() {
        return groupName;
    }

    public int getTimeRequired() {
        return timeRequired;
    }

    public String getRankUpCommand() {
        return rankUpCommand;
    }

    public String getBroadcastMessage() {
        return broadcastMessage;
    }

    public String getPlayerTitleMessage() {
        return playerTitleMessage;
    }
}
