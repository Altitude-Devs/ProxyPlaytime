package com.playtime.util.objects;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.function.Function;

public class PlaytimeTop {

    private final LinkedList<TopPlayer> totalTop = new LinkedList<>();
    private final LinkedList<TopPlayer> monthlyTop = new LinkedList<>();
    private final LinkedList<TopPlayer> weeklyTop = new LinkedList<>();
    private final LinkedList<TopPlayer> dailyTop = new LinkedList<>();

    public synchronized <R> R processTop(TopType topType, Function<LinkedList<TopPlayer>, R> function) {
        switch (topType) {
            case TOTAL -> {
                return function.apply(totalTop);
            }
            case MONTHLY -> {
                return function.apply(monthlyTop);
            }
            case WEEKLY -> {
                return function.apply(weeklyTop);
            }
            case DAILY -> {
                return function.apply(dailyTop);
            }
            default -> {
                return null;
            }
        }
    }

    public synchronized void prepUpdateTop(TopType topType) {
        switch (topType) {
            case TOTAL -> {
                totalTop.clear();
            }
            case MONTHLY -> {
                monthlyTop.clear();
            }
            case WEEKLY -> {
                weeklyTop.clear();
            }
            case DAILY -> {
                dailyTop.clear();
            }
        }
    }

    public synchronized void addToTop(TopType topType, TopPlayer topPlayer) {
        switch (topType) {
            case TOTAL -> {
                totalTop.add(topPlayer);
            }
            case MONTHLY -> {
                monthlyTop.add(topPlayer);
            }
            case WEEKLY -> {
                weeklyTop.add(topPlayer);
            }
            case DAILY -> {
                dailyTop.add(topPlayer);
            }
        }
    }

    public synchronized void sort(TopType topType) {
        switch (topType) {
            case TOTAL -> totalTop.sort(Comparator.comparingLong(TopPlayer::getSessionTime).reversed());
            case MONTHLY -> monthlyTop.sort(Comparator.comparingLong(TopPlayer::getSessionTime).reversed());
            case WEEKLY -> weeklyTop.sort(Comparator.comparingLong(TopPlayer::getSessionTime).reversed());
            case DAILY -> dailyTop.sort(Comparator.comparingLong(TopPlayer::getSessionTime).reversed());
        }
    }
}
