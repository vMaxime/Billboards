package fr.vmaxime.billboards.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import fr.vmaxime.billboards.BillboardSign;
import fr.vmaxime.billboards.Billboards;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SignEditor implements Listener {

    private final Map<UUID, SignResponse> editing = new HashMap<>();

    public SignEditor() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(Billboards.getInstance(), PacketType.Play.Client.UPDATE_SIGN) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();

                if (player == null)
                    return;

                SignResponse response = editing.remove(player.getUniqueId());

                if (response == null)
                    return;

                event.setCancelled(true);
                response.onSignFinish(event.getPacket().getStringArrays().read(0));

            }
        });
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        editing.remove(id);
    }

    public void open(Player player, BillboardSign sign, SignResponse response) {
        Location location = sign.getLocation();
        BlockPosition position = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        PacketContainer openSign = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR);
        PacketContainer signData = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.TILE_ENTITY_DATA);

        openSign.getBlockPositionModifier().write(0, position);

        NbtCompound signNBT = (NbtCompound) signData.getNbtModifier().read(0);

        if (sign.getLines() != null)
            for (int i = 0; i < 4; i++)
                if (sign.getLines()[i] != null) {
                    String line = sign.getLines()[i];
                    signNBT.put("Text" + (i + 1), String.format("{\"text\":\"%s\"}", line));
                }
        signNBT.put("x", position.getX());
        signNBT.put("y", position.getY());
        signNBT.put("z", position.getZ());
        signNBT.put("id", "minecraft:sign");

        signData.getBlockPositionModifier().write(0, position);
        signData.getIntegers().write(0, 8);
        signData.getNbtModifier().write(0, signNBT);

        try {

            ProtocolLibrary.getProtocolManager().sendServerPacket(player, signData);
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, openSign);

        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        editing.put(player.getUniqueId(), response);
    }

    /**
     * Set lines of a Sign
     * @param sign Sign we want to set lines
     * @param lines new lines
     */
    public static void setLines(Sign sign, String... lines) {
        for (int i = 0; i < 4; i++) {
            String line = lines != null ? lines[i] : "";
            sign.setLine(i, ChatColor.translateAlternateColorCodes('&', line != null ? line : ""));
        }
        sign.update();
    }

    public interface SignResponse {

        void onSignFinish(String... lines);

    }

}