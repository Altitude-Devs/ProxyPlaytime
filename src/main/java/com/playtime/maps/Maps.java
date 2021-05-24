package com.playtime.maps;

import com.playtime.util.objects.Groups;
import com.playtime.util.objects.PlaytimePlayer;
import com.playtime.util.objects.PlaytimeSeen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Maps {

    public static HashMap<String, Groups> groups = new HashMap<>();

    public static HashMap<String, String> seenServers = new HashMap<>();

    public static HashMap<UUID, PlaytimePlayer> playtimePlayers = new HashMap<>();

    public static HashMap<UUID, PlaytimeSeen> playtimeSeen = new HashMap<>();

    public static ArrayList<PlaytimePlayer> loggedOutPlayers = new ArrayList<>();

}
