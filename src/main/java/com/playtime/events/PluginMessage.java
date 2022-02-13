package com.playtime.events;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.playtime.Playtime;
import com.playtime.commands.commandUtils.SeenPlayer;
import com.playtime.database.Queries;
import com.playtime.util.objects.PlaytimeSeen;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.util.Optional;
import java.util.UUID;

public class PluginMessage {

    private final ChannelIdentifier identifier;

    public PluginMessage(ChannelIdentifier identifier){
        this.identifier = identifier;
    }

    @Subscribe
    public void onPluginMessageEvent(PluginMessageEvent event){
        if (!event.getIdentifier().equals(identifier)) return;
        event.setResult(PluginMessageEvent.ForwardResult.handled());

        if(event.getSource() instanceof Player) {
            return;
        }
        if (!(event.getSource() instanceof ServerConnection serverConnection)) return;
        // Read the data written to the message
        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String channel = in.readUTF();
        switch (channel) {
            case "playerplaytime":
                UUID uuid = UUID.fromString(in.readUTF());

                PlaytimeSeen playtimeSeen = Queries.getLastSeen(uuid);
                if (playtimeSeen == null) return;

                Optional<Player> playerOptional = Playtime.getInstance().getServer().getPlayer(uuid);
                Component component = SeenPlayer.getLastSeen(uuid, playerOptional.orElse(null));

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("playtimeresponse");
                out.writeLong(playtimeSeen.getLastSeen());
                out.writeUTF(GsonComponentSerializer.gson().serialize(component));
                serverConnection.sendPluginMessage(identifier, out.toByteArray());
                break;
            default:
                break;
        }
    }

}
