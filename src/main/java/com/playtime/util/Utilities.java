package com.playtime.util;

import com.playtime.Playtime;
import com.velocitypowered.api.proxy.Player;
import net.luckperms.api.model.user.User;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Utilities {

    public static String convertTime(long timeInMillis){
        return convertTime((int) TimeUnit.MILLISECONDS.toMinutes(timeInMillis));
    }

    private static String convertTime(int timeInMinutes) {
        int days = (int) TimeUnit.MINUTES.toDays(timeInMinutes);
        int hours = (int) (TimeUnit.MINUTES.toHours(timeInMinutes) - TimeUnit.DAYS.toHours(days));
        int minutes = (int) (TimeUnit.MINUTES.toMinutes(timeInMinutes) - TimeUnit.HOURS.toMinutes(hours)
                - TimeUnit.DAYS.toMinutes(days));

        StringBuilder stringBuilder = new StringBuilder();

        if (days != 0) {
            stringBuilder.append(days).append(days == 1 ? " day, " : " days, ");
        }
        if (hours != 0) {
            stringBuilder.append(hours).append(hours == 1 ? " hour, " : " hours, ");
        }
        stringBuilder.append(minutes).append(minutes == 1 ? " minute, " : " minutes, ");

        return stringBuilder.substring(0, stringBuilder.length() - 2);
    }

    public static UUID getPlayerUUID(String playerName) {
        Playtime instance = Playtime.getInstance();
        Optional<Player> player = instance.getServer().getPlayer(playerName);

        if (player.isPresent()) return player.get().getUniqueId();

        User user = instance.getLuckPerms().getUserManager().getUser(playerName);
        return (user == null) ? null : user.getUniqueId();
    }

    public static String getPlayerName(UUID uuid) {
        Playtime instance = Playtime.getInstance();
        Optional<Player> player = instance.getServer().getPlayer(uuid);

        if (player.isPresent()) return player.get().getUsername();

        User user = instance.getLuckPerms().getUserManager().getUser(uuid);
        return (user == null) ? uuid.toString() : user.getUsername();
    }
//    public static String convertTime(int timeInMinutes) {
//        int days = (int) TimeUnit.MINUTES.toDays(timeInMinutes);
//        int hours = (int) (TimeUnit.MINUTES.toHours(timeInMinutes) - TimeUnit.DAYS.toHours(days));
//        int minutes = (int) (TimeUnit.MINUTES.toMinutes(timeInMinutes) - TimeUnit.HOURS.toMinutes(hours)
//                - TimeUnit.DAYS.toMinutes(days));
//
//        String d = ChatColor.GRAY + "" + days + ChatColor.WHITE + " day";
//        String h = ChatColor.GRAY + "" + hours + ChatColor.WHITE + " hour";
//        String m = ChatColor.GRAY + "" + minutes + ChatColor.WHITE + " minute";
//        String c = ChatColor.WHITE + ", ";
//
//        if(days == 0) {
//            if(hours == 0) {
//                if(minutes == 1) {
//                    return m;
//                } else {
//                    return m + "s";
//                }
//            } else {
//                if(hours == 1) {
//                    if(minutes == 1) {
//                        return h + c + m;
//                    } else {
//                        if(minutes == 0) {
//                            return h;
//                        }
//                        return h + c + m + "s";
//                    }
//                } else {
//                    if(minutes == 1) {
//                        return h + "s" + c + m;
//                    } else {
//                        if (minutes == 0) {
//                            return h + "s";
//                        }
//                        return h + "s" + c + m + "s";
//                    }
//                }
//            }
//        } else {
//            if(days == 1) {
//                if(hours == 1) {
//                    if(minutes == 1) {
//                        return d + c + h + c + m;
//                    } else {
//                        if(minutes == 0) {
//                            return d + c + h;
//                        }
//                        return d + c + h + c + m + "s";
//                    }
//                } else {
//                    if(minutes == 1) {
//                        if(hours == 0) {
//                            return d + c + m;
//                        }
//                        return d + c + h + "s" + c + m;
//                    } else {
//                        if(minutes == 0) {
//                            if(hours == 0) {
//                                return d;
//                            }
//                            return d + c + h + "s";
//                        } else {
//                            if(hours == 0) {
//                                return d + c + m + "s";
//                            }
//                            return d + c + h + "s" + c + m + "s";
//                        }
//                    }
//                }
//            } else {
//                if(hours == 1) {
//                    if(minutes == 1) {
//                        return d + "s" + c + h + c + m;
//                    } else {
//                        if(minutes == 0) {
//                            return d + "s" + c + h;
//                        }
//                        return d + "s" + c + h + c + m + "s";
//                    }
//                } else {
//                    if(minutes == 1) {
//                        if(hours == 0) {
//                            return d + "s" + c + m;
//                        }
//                        return d + "s" + c + h + "s" + c + m;
//                    } else {
//                        if(minutes == 0) {
//                            if(hours == 0) {
//                                return d + "s";
//                            }
//                            return d + "s" + c + h + "s";
//                        } else {
//                            if(hours == 0) {
//                                return d + "s" + c + m + "s";
//                            }
//                            return d + "s" + c + h + "s" + c + m + "s";
//                        }
//
//                    }
//                }
//            }
//        }
//    }
//
//    public static String format(String m) {
//        return ChatColor.translateAlternateColorCodes('&', m);
//    }

}
