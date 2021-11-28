package fr.vmaxime.billboards.inventory;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.vmaxime.billboards.Billboard;
import fr.vmaxime.billboards.BillboardSign;
import fr.vmaxime.billboards.Billboards;
import fr.vmaxime.billboards.util.Lang;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class BillboardInventory implements InventoryProvider {

    private final Billboard billboard;
    private final Player player;

    private final SmartInventory INVENTORY;

    public BillboardInventory(Billboard billboard, Player player) {
        this.billboard = billboard;
        this.player = player;


        INVENTORY = SmartInventory.builder()
                .id("billboardInventory")
                .provider(this)
                .title(ChatColor.translateAlternateColorCodes('&', Billboards.INVENTORY_TITLE))
                .size(6, 9)
                .manager(Billboards.getInventoryManager())
                .build();
    }


    @Override
    public void init(Player player, InventoryContents contents) {

        for (int slot = 0; slot < 6*9; slot++) {

            BillboardSign sign = billboard.getSign(slot);
            ItemStack item = getItem(sign);
            ClickableItem cItem = getClickableItem(sign, item);

            contents.firstEmpty().ifPresent(slotPos -> contents.set(slotPos, cItem));

        }

    }

    @Override
    public void update(Player player, InventoryContents contents) {
        contents.fill(null);
        init(player, contents);
    }

    public void open() {
        INVENTORY.open(player);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 3f);
    }

    private ItemStack getItem(BillboardSign sign) {

        ItemStack item = new ItemStack(sign == null ? Material.BLACK_STAINED_GLASS_PANE : sign.getOwner() == null ? Material.GREEN_STAINED_GLASS_PANE : Material.BROWN_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();

        String itemName = sign == null ? Lang.INVENTORY_SLOT_NOT_ASSIGNED.getText(true) : sign.getOwner() != null ? Lang.INVENTORY_SIGN_ALREADY_RENTED.getText(true) : Lang.INVENTORY_SIGN_AVAILABLE.getText(true);
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemName));

        if (sign != null) {

            Player owner = sign.getOwner() != null ? Bukkit.getPlayer(sign.getOwner()) : null;

            if (owner != null) {

                boolean playerIsOwner = player.getUniqueId().equals(owner.getUniqueId());
                List<String> lore = new ArrayList<>(Arrays.asList(
                        Lang.INVENTORY_SIGN_OWNER.getText(true).replace("%player%", owner.getName()),
                        Lang.INVENTORY_SIGN_RENT_END.getText(true).replace("%time%", Billboards.formatTime(sign.getRentEndAt() - System.currentTimeMillis())),
                        ""
                ));

                if (playerIsOwner || player.hasPermission("billboard.remove"))
                    lore.add((playerIsOwner ? Lang.INVENTORY_SIGN_RIGHT_CLICK_TO_EDIT : Lang.INVENTORY_SIGN_RIGHT_CLICK_TO_DELETE).getText(true));

                meta.setLore(lore);
            } else {

                String[] lines = Lang.INVENTORY_SIGN_LORE.getText(true).split(Pattern.quote("||"));
                for (int i = 0; i < lines.length; i++)
                    lines[i] = lines[i]
                            .replace("%price%", String.valueOf(sign.getPrice()))
                            .replace("%time%", Billboards.formatTime(sign.getRentDuration()));
                meta.setLore(Arrays.asList(lines));

            }
        }

        item.setItemMeta(meta);
        return item;
    }

    private ClickableItem getClickableItem(BillboardSign sign, ItemStack item) {

        return ClickableItem.of(item, event -> {

            if (sign == null || sign.getOwner() != null) {

                if (sign != null && sign.getOwner() != null && event.getClick() == ClickType.RIGHT) {
                    if (sign.getOwner().equals(player.getUniqueId()))
                        Bukkit.getScheduler().runTask(Billboards.getInstance(), () -> Billboards.editSign(sign, player));
                    else {
                        sign.setOwner(null, -1L);
                        Bukkit.getScheduler().runTask(Billboards.getInstance(), sign::resetLines);
                        player.sendMessage(Lang.CHAT_RENT_DELETED.getText(true));
                        player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 2f, 2f);
                    }
                }

                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 2f, 2f);
                return;

            }

            Block block = sign.getLocation().getBlock();

            if (!block.getType().toString().toLowerCase(Locale.ROOT).contains("wall_sign"))
                block.setType(Material.OAK_WALL_SIGN);

            Economy economy = Billboards.getEconomy();
            double balance = economy.getBalance(player);

            if (balance >= sign.getPrice())
                new ConfirmInventory(player, sign, o -> Bukkit.getScheduler().runTask(Billboards.getInstance(), () -> Billboards.getSignEditor().open(player, sign, lines -> Billboards.buySign(sign, player, lines)))).open();
            else {

                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 2f, 2f);
                player.sendMessage(Lang.CHAT_NOT_ENOUGH_MONEY.getText(true));

            }

        });

    }

}
