package fr.vmaxime.billboards.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import fr.vmaxime.billboards.Billboard;
import fr.vmaxime.billboards.BillboardSign;
import fr.vmaxime.billboards.Billboards;
import fr.vmaxime.billboards.util.Lang;
import fr.vmaxime.billboards.util.SignEditor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@CommandAlias("billboard|billboards")
@Description("Billboard plugin")
public class BillboardCommand extends BaseCommand {

    @Default
    @HelpCommand
    @CommandPermission("billboard.help")
    public static void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("create")
    @CommandPermission("billboard.create")
    @CommandCompletion("billboard_name")
    @Syntax("<billboard_name>")
    public static void onCreate(Player sender, String name) {

        if (Billboards.getBillboardManager().getBillboard(name) != null) {

            sender.sendMessage(Lang.CHAT_NAME_ALREADY_IN_USE.getText(true));
            sender.playSound(sender.getLocation(), Sound.ENTITY_VILLAGER_NO, 2f, 2f);
            return;

        }

        sender.sendMessage(Lang.CHAT_SELECT_BILLBOARD_BLOCK.getText(true));
        Billboards.getSelectionManager().makeSelect(sender, block -> {

            Billboard billboard = new Billboard(name, block.getLocation());
            Billboards.getBillboardManager().addBillboard(billboard);
            sender.playSound(sender.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2f, 2f);
            sender.sendMessage(Lang.CHAT_BILLBOARD_CREATED_SUCCESSFULLY.getText(true).replace("%billboard%", billboard.getName()));

            Billboards.saveBillboard(billboard);

        });

    }

    @Subcommand("remove billboard")
    @CommandPermission("billboard.remove")
    @CommandCompletion("@billboards")
    @Syntax("<billboard_name>")
    public static void onRemoveBillboard(Player sender, String name) {

        Billboard billboard = Billboards.getBillboardManager().getBillboard(name);

        if (billboard == null) {

            sender.sendMessage(Lang.CHAT_NAME_DOES_NOT_EXIST.getText(true));
            sender.playSound(sender.getLocation(), Sound.ENTITY_VILLAGER_NO, 2f, 2f);
            return;

        }

        Billboards.getBillboardManager().removeBillboard(billboard);
        sender.sendMessage(Lang.CHAT_BILLBOARD_DELETED_SUCCESSFULLY.getText(true).replace("%billboard%", billboard.getName()));
        sender.playSound(sender.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2f, 2f);

        Billboards.removeBillboard(billboard);

    }

    @Subcommand("add sign")
    @CommandPermission("billboard.sign")
    @CommandCompletion("@billboards price rent_duration @range:0-53")
    @Syntax("<billboard_name> <price> <rent_duration> <inventory_slot>")
    public static void onAddSign(Player sender, String billboardName, long price, String rentDuration, int slot) {

        Billboard billboard = Billboards.getBillboardManager().getBillboard(billboardName);

        if (billboard.isFull()) {

            sender.sendMessage(Lang.CHAT_BILLBOARD_FULL.getText(true));
            sender.playSound(sender.getLocation(), Sound.ENTITY_VILLAGER_NO, 2f, 2f);
            return;

        }

        sender.sendMessage(Lang.CHAT_SELECT_SIGN.getText(true));
        Billboards.getSelectionManager().makeSelect(sender, block -> {

            BillboardSign sign = billboard.getSign(slot);

            if (sign != null) {

                sender.sendMessage(Lang.CHAT_SIGN_ALREADY_USING_SLOT.getText(true).replace("%slot%", String.valueOf(slot)));
                sender.playSound(sender.getLocation(), Sound.ENTITY_VILLAGER_NO, 2f, 2f);
                return;

            }

            sign = billboard.getSign(block.getLocation());

            if (sign != null) {

                sender.sendMessage(Lang.CHAT_SIGN_ALREADY_USING_LOCATION.getText(true));
                sender.playSound(sender.getLocation(), Sound.ENTITY_VILLAGER_NO, 2f, 2f);
                return;

            }

            long time = getTimeFromString(rentDuration);

            if (time <= 0) {

                sender.sendMessage("§cExemple rent_duration : 60s 30m 12h 31j");
                sender.playSound(sender.getLocation(), Sound.ENTITY_VILLAGER_NO, 2f, 2f);
                return;

            }

            sign = new BillboardSign(block.getLocation(), price, time, slot);
            billboard.addSign(sign);

            sender.sendMessage(Lang.CHAT_SIGN_ADDED_TO_BILLBOARD.getText(true).replace("%billboard%", billboard.getName()).replace("%slot%", String.valueOf(sign.getSlot())));
            sender.playSound(sender.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2f, 2f);

            Billboards.saveSign(sign);
            Billboards.getInstance().getBillboardsConfiguration().save();

        });

    }

    @Subcommand("remove sign")
    @CommandPermission("billboard.sign")
    @CommandCompletion("@billboards @range:0-53")
    @Syntax("<billboard_name> <inventory_slot>")
    public static void onRemoveSign(Player sender, String billboardName, int slot) {

        Billboard billboard = Billboards.getBillboardManager().getBillboard(billboardName);

        if (billboard == null) {

            sender.sendMessage(Lang.CHAT_BILLBOARD_DOES_NOT_EXIST.getText(true));
            sender.playSound(sender.getLocation(), Sound.ENTITY_VILLAGER_NO, 2f, 2f);
            return;

        }

        BillboardSign sign = billboard.getSign(slot);

        if (sign == null) {

            sender.sendMessage(Lang.CHAT_BILLBOARD_DOES_NOT_CONTAIN_THIS_SLOT.getText(true).replace("%slot%", String.valueOf(slot)));
            sender.playSound(sender.getLocation(), Sound.ENTITY_VILLAGER_NO, 2f, 2f);
            return;

        }

        Billboards.removeSign(sign);
        billboard.removeSign(sign);
        Bukkit.getScheduler().runTask(Billboards.getInstance(), sign::resetLines);

        sender.sendMessage(Lang.CHAT_SIGN_DELETED_FROM_BILLBOARD.getText(true).replace("%billboard%", billboard.getName()));
        sender.playSound(sender.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2f, 2f);

    }

    @Subcommand("teleport billboard")
    @CommandPermission("billboard.teleport")
    @CommandCompletion("@billboards")
    @Syntax("<billboard_name>")
    public static void onTeleportToBillboard(Player sender, String billboardName) {

        Billboard billboard = Billboards.getBillboardManager().getBillboard(billboardName);

        if (billboard == null) {

            sender.sendMessage(Lang.CHAT_BILLBOARD_DOES_NOT_EXIST.getText(true));
            sender.playSound(sender.getLocation(), Sound.ENTITY_VILLAGER_NO, 2f, 2f);
            return;

        }

        sender.teleport(billboard.getLocation());
        sender.playSound(sender.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 2f, 2f);

    }

    @Subcommand("teleport sign")
    @CommandPermission("billboard.teleport")
    @CommandCompletion("@billboards @range:0-53")
    @Syntax("<billboard_name> <inventory_slot>")
    public static void onTeleportToBillboardSign(Player sender, String billboardName, int slot) {

        Billboard billboard = Billboards.getBillboardManager().getBillboard(billboardName);

        if (billboard == null) {

            sender.sendMessage(Lang.CHAT_BILLBOARD_DOES_NOT_EXIST.getText(true));
            sender.playSound(sender.getLocation(), Sound.ENTITY_VILLAGER_NO, 2f, 2f);
            return;

        }

        BillboardSign sign = billboard.getSign(slot);

        if (sign == null) {

            sender.sendMessage(Lang.CHAT_BILLBOARD_DOES_NOT_CONTAIN_THIS_SLOT.getText(true));
            sender.playSound(sender.getLocation(), Sound.ENTITY_VILLAGER_NO, 2f, 2f);
            return;

        }

        sender.teleport(sign.getLocation());
        sender.playSound(sender.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 2f, 2f);

    }

    @Subcommand("set name")
    @CommandPermission("billboard.use")
    @CommandCompletion("@billboards new_name")
    @Syntax("<billboard_name> <new_name>")
    public static void onSetName(CommandSender sender, String billboardName, String newName) {

        Player player = sender instanceof Player ? (Player) sender : null;
        Billboard billboard = Billboards.getBillboardManager().getBillboard(billboardName);

        if (billboard == null) {

            sender.sendMessage(Lang.CHAT_BILLBOARD_DOES_NOT_EXIST.getText(true));
            if (player != null)
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 2f, 2f);
            return;

        }

        billboard.setName(newName);
        Billboards.saveBillboard(billboard);
        sender.sendMessage(Lang.CHAT_NAME_HAS_BEEN_CHANGED.getText(true)
                .replace("%billboard%", billboardName)
                .replace("%new%", newName)
        );
        if (player != null)
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 2f);

    }

    @Subcommand("set price")
    @CommandPermission("billboard.use")
    @CommandCompletion("@billboards @range:0-53 new_price")
    @Syntax("<billboard_name> <inventory_slot> <new_price>")
    public static void onSetPrice(CommandSender sender, String billboardName, int slot, long newPrice) {

        Player player = sender instanceof Player ? (Player) sender : null;
        Billboard billboard = Billboards.getBillboardManager().getBillboard(billboardName);

        if (billboard == null) {

            sender.sendMessage(Lang.CHAT_BILLBOARD_DOES_NOT_EXIST.getText(true));
            if (player != null)
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 2f, 2f);
            return;

        }

        BillboardSign sign = billboard.getSign(slot);

        if (sign == null) {

            sender.sendMessage(Lang.CHAT_BILLBOARD_DOES_NOT_CONTAIN_THIS_SLOT.getText(true));
            if (player != null)
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 2f, 2f);
            return;

        }

        sign.setPrice(newPrice);
        Billboards.saveSign(sign);
        Billboards.getInstance().getBillboardsConfiguration().save();
        sender.sendMessage(Lang.CHAT_PRICE_HAS_BEEN_CHANGED.getText(true)
                .replace("%billboard%", billboardName)
                .replace("%price%", String.valueOf(newPrice))
        );
        if (player != null)
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 2f);

    }

    @Subcommand("set rent_duration")
    @CommandPermission("billboard.use")
    @CommandCompletion("@billboards @range:0-53 new_rent_duration")
    @Syntax("<billboard_name> <inventory_slot> <new_rent_duration>")
    public static void onSetRentDuration(CommandSender sender, String billboardName, int slot, String newRentDuration) {

        Player player = sender instanceof Player ? (Player) sender : null;
        Billboard billboard = Billboards.getBillboardManager().getBillboard(billboardName);

        if (billboard == null) {

            sender.sendMessage(Lang.CHAT_BILLBOARD_DOES_NOT_EXIST.getText(true));
            if (player != null)
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 2f, 2f);
            return;

        }

        BillboardSign sign = billboard.getSign(slot);

        if (sign == null) {

            sender.sendMessage(Lang.CHAT_BILLBOARD_DOES_NOT_CONTAIN_THIS_SLOT.getText(true));
            if (player != null)
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 2f, 2f);
            return;

        }

        long time = getTimeFromString(newRentDuration);

        if (time <= 0) {

            sender.sendMessage("§cExemple rent_duration : 60s 30m 12h 31j");
            if (player != null)
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 2f, 2f);
            return;

        }

        sign.setRentDuration(time);
        Billboards.saveSign(sign);
        Billboards.getInstance().getBillboardsConfiguration().save();
        sender.sendMessage(Lang.CHAT_RENT_DURATION_HAS_BEEN_CHANGED.getText(true)
                .replace("%billboard%", billboardName)
                .replace("%time%", Billboards.formatTime(time))
        );
        if (player != null)
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 2f);

    }

    @Subcommand("set slot")
    @CommandPermission("billboard.use")
    @CommandCompletion("@billboards @range:0-53 new_inventory_slot")
    @Syntax("<billboard_name> <inventory_slot> <new_inventory_slot>")
    public static void onSetSlot(CommandSender sender, String billboardName, int slot, int newSlot) {

        Player player = sender instanceof Player ? (Player) sender : null;
        Billboard billboard = Billboards.getBillboardManager().getBillboard(billboardName);

        if (billboard == null) {

            sender.sendMessage(Lang.CHAT_BILLBOARD_DOES_NOT_EXIST.getText(true));
            if (player != null)
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 2f, 2f);
            return;

        }

        BillboardSign sign1 = billboard.getSign(slot);

        if (sign1 == null) {

            sender.sendMessage(Lang.CHAT_BILLBOARD_DOES_NOT_CONTAIN_THIS_SLOT.getText(true)
                    .replace("%slot%", String.valueOf(slot))
            );
            if (player != null)
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 2f, 2f);
            return;

        }

        BillboardSign sign2 = billboard.getSign(newSlot);

        if (sign2 != null)
            sign2.setSlot(slot);

        sign1.setSlot(newSlot);
        Billboards.saveBillboard(billboard);
        sender.sendMessage(Lang.CHAT_SLOT_HAS_BEEN_CHANGED.getText(true)
                .replace("%billboard%", billboardName)
                .replace("%slot%", String.valueOf(newSlot))
        );
        if (player != null)
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 2f);

    }

    @Subcommand("movesign")
    @CommandPermission("billboard.movesign")
    public static void onMoveSign(Player sender) {
        Bukkit.getScheduler().runTask(Billboards.getInstance(), () -> {

            sender.sendMessage(Lang.CHAT_SELECT_SIGN.getText(true));
            Billboards.getSelectionManager().makeSelect(sender, block -> {
                if (!block.getType().name().toLowerCase(Locale.ROOT).contains("wall_sign")) {

                    sender.sendMessage(Lang.CHAT_THIS_BLOCK_IS_NOT_A_SIGN.getText(true));
                    return;

                }

                Sign sign1 = (Sign) block.getState();

                sender.sendMessage(Lang.CHAT_SELECT_SIGN.getText(true));
                Billboards.getSelectionManager().makeSelect(sender, block2 -> {

                    if (!block2.getType().name().toLowerCase(Locale.ROOT).contains("wall_sign")) {

                        sender.sendMessage(Lang.CHAT_THIS_BLOCK_IS_NOT_A_SIGN.getText(true));
                        return;

                    }

                    // swap sign 1 and sign 2 lines
                    Sign sign2 = (Sign) block2.getState();
                    String[] sign1Lines = sign1.getLines().clone();
                    SignEditor.setLines(sign1, sign2.getLines());
                    SignEditor.setLines(sign2, sign1Lines);

                    // swap sign 1 and sign 2 stored location in their billboard
                    BillboardSign billboardSign1 = Billboards.getBillboardManager().getSign(sign1.getLocation());
                    BillboardSign billboardSign2 = Billboards.getBillboardManager().getSign(sign2.getLocation());
                    Location sign1Location = sign1.getLocation().clone();

                    if (billboardSign1 != null)
                        billboardSign1.setLocation(billboardSign2.getLocation().clone());

                    if (billboardSign2 != null)
                        billboardSign2.setLocation(sign1Location);

                    sender.sendMessage(Lang.CHAT_SIGNS_HAVE_BEEN_MOVED.getText(true));
                    sender.playSound(sender.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 2f);

                });
            });

        });
    }

    @Subcommand("info")
    @CommandPermission("billboard.info")
    public static void onInfo(Player sender) {

        sender.sendMessage(Lang.CHAT_SELECT_SIGN.getText(true));
        Billboards.getSelectionManager().makeSelect(sender, block -> {
            if (!block.getType().name().toLowerCase(Locale.ROOT).contains("wall_sign")) {

                sender.sendMessage(Lang.CHAT_THIS_BLOCK_IS_NOT_A_SIGN.getText(true));
                sender.playSound(sender.getLocation(), Sound.ENTITY_VILLAGER_NO, 2f, 2f);
                return;

            }

            BillboardSign sign = Billboards.getBillboardManager().getSign(block.getLocation());

            if (sign == null) {

                sender.sendMessage(Lang.CHAT_THIS_IS_NOT_A_BILLBOARD_SIGN.getText(true));
                sender.playSound(sender.getLocation(), Sound.ENTITY_VILLAGER_NO, 2f, 2f);
                return;

            }

            String[] parts = (sign.isBought() ? Lang.CHAT_SIGN_INFO_IN_RENT : Lang.CHAT_SIGN_INFO_AVAILABLE).getText(true).split(Pattern.quote("||"));
            for (int i = 0; i < parts.length; i++) {
                OfflinePlayer offlinePlayer = sign.getOwner() != null ? Bukkit.getOfflinePlayer(sign.getOwner()) : null;
                sender.sendMessage(parts[i]
                        .replace("%billboard%", sign.getBillboard().getName())
                        .replace("%owner%", offlinePlayer != null ? offlinePlayer.getName() : "N/A")
                        .replace("%price%", String.valueOf(sign.getPrice()))
                        .replace("%time%", Billboards.formatTime(sign.isBought() ? sign.getRentEndAt() - System.currentTimeMillis() : sign.getRentDuration())));
            }

        });
    }

    /**
     * Check if a String is numeric
     * @param strNum String we want to check
     * @return true if the String is numeric or false if not
     */
    private static boolean isNumeric(String strNum) {
        if (strNum == null || strNum.isEmpty())
            return false;

        try {
            Double.parseDouble(strNum);
        } catch (NumberFormatException ignore) {
            return false;
        }

        return true;
    }

    /**
     * Get time from string
     * @param string String we want to extract the time
     * @return time in millis
     */
    private static long getTimeFromString(String string) {
        String[] parts = string.split("");
        StringBuilder timeBuilder = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++)
            timeBuilder.append(parts[i]);

        if (!isNumeric(timeBuilder.toString()))
            return -1L;


        long time = Long.parseLong(timeBuilder.toString());
        if (time <= 0)
            return -1L;

        String timeSymbol = parts[parts.length - 1];
        TimeUnit timeUnit = timeSymbol.equalsIgnoreCase("s") ? TimeUnit.SECONDS
                : timeSymbol.equalsIgnoreCase("m") ? TimeUnit.MINUTES
                : timeSymbol.equalsIgnoreCase("h") ? TimeUnit.HOURS
                : timeSymbol.equalsIgnoreCase("j") || timeSymbol.equalsIgnoreCase("d") ? TimeUnit.DAYS
                : TimeUnit.SECONDS;

        return timeUnit.toMillis(time);
    }

}
