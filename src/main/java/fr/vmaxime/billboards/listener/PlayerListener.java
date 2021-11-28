package fr.vmaxime.billboards.listener;

import fr.vmaxime.billboards.Billboard;
import fr.vmaxime.billboards.BillboardSign;
import fr.vmaxime.billboards.Billboards;
import fr.vmaxime.billboards.inventory.BillboardInventory;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block == null)
            return;

        Billboard billboard = Billboards.getBillboardManager().getBillboard(block.getLocation());

        if (billboard == null)
            return;

        event.setCancelled(true);
        new BillboardInventory(billboard, player).open();

    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Block block = event.getBlock();

        BillboardSign sign = Billboards.getBillboardManager().getSign(block.getLocation());

        if (sign == null)
            return;

        event.setCancelled(true);
        if (player.isOp()) {
            player.sendMessage(ChatColor.RED + "You can't break billboard signs..");
        }

    }

}
