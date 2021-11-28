package fr.vmaxime.billboards.inventory;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.vmaxime.billboards.BillboardSign;
import fr.vmaxime.billboards.Billboards;
import fr.vmaxime.billboards.util.Lang;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class ConfirmInventory implements InventoryProvider {

    private final Player player;
    private final BillboardSign sign;
    private final Consumer<?> onBought;
    private final SmartInventory INVENTORY;

    public ConfirmInventory(Player player, BillboardSign sign, Consumer<?> onBought) {
        this.player = player;
        this.sign = sign;
        this.onBought = onBought;
        INVENTORY = SmartInventory.builder()
                .id("billboardInventory")
                .provider(this)
                .title(Billboards.INVENTORY_CONFIRM_TITLE)
                .size(3, 9)
                .manager(Billboards.getInventoryManager())
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {

        long price = sign.getPrice();

        ItemStack item = new ItemStack(Material.LIME_TERRACOTTA);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(Lang.INVENTORY_CONFIRM_I_CONFIRM.getText(true));
        String[] lore = Lang.INVENTORY_CONFIRM_I_CONFIRM_LORE.getText(true).split(Pattern.quote("||"));
        for (int i = 0; i < lore.length; i++)
            lore[i] = lore[i].replace("%price%", String.valueOf(price));
        meta.setLore(Arrays.asList(lore));

        item.setItemMeta(meta);

        contents.set(1, 2, ClickableItem.of(item, event -> {

            Economy economy = Billboards.getEconomy();
            double balance = economy.getBalance(player);

            if (balance >= price) {
                if (economy.withdrawPlayer(player, price).transactionSuccess())
                    onBought.accept(null);
                else {
                    player.sendMessage(Lang.CHAT_ERROR_WHILE_PURCHASING.getText(true));
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_FALL, 2f, 2f);
                }
            }

        }));

        item = new ItemStack(Material.RED_TERRACOTTA);
        meta = item.getItemMeta();
        meta.setDisplayName(Lang.INVENTORY_CONFIRM_CANCEL.getText(true));
        item.setItemMeta(meta);

        contents.set(1, 6, ClickableItem.of(item, event -> {

            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_HURT, 1f, 1f);
            INVENTORY.close(player);

        }));

        item = new ItemStack(Material.OAK_SIGN);
        meta = item.getItemMeta();
        meta.setDisplayName(Lang.INVENTORY_CONFIRM_SIGN.getText(true));
        lore = Lang.INVENTORY_CONFIRM_SIGN_LORE.getText(true).split(Pattern.quote("||"));
        for (int i = 0; i < lore.length; i++)
            lore[i] = lore[i]
                    .replace("%slot%", String.valueOf(sign.getSlot()))
                    .replace("%billboard%", sign.getBillboard().getName())
                    .replace("%time%", Billboards.formatTime(sign.getRentDuration()));
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);

        contents.set(1, 4, ClickableItem.empty(item));

    }

    @Override
    public void update(Player player, InventoryContents contents) {}

    public void open() {
        INVENTORY.open(player);
    }

}
