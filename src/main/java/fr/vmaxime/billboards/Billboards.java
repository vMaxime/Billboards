package fr.vmaxime.billboards;

import co.aikar.commands.PaperCommandManager;
import fr.vmaxime.billboards.command.BillboardCommand;
import fr.vmaxime.billboards.listener.PlayerListener;
import fr.vmaxime.billboards.listener.SelectionListener;
import fr.vmaxime.billboards.manager.BillboardManager;
import fr.vmaxime.billboards.manager.SelectionManager;
import fr.vmaxime.billboards.util.Configuration;
import fr.vmaxime.billboards.util.Lang;
import fr.vmaxime.billboards.util.SignEditor;
import fr.minuskube.inv.InventoryManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Billboards extends JavaPlugin {

    private static Billboards INSTANCE;
    public static String INVENTORY_TITLE;
    public static String INVENTORY_CONFIRM_TITLE;

    // CONFIGS
    private Configuration langConfiguration;
    private Configuration billboardsConfiguration;

    // ECONOMY
    private static Economy economy;

    // MANAGERS
    private static PaperCommandManager commandManager;
    private static InventoryManager inventoryManager;
    private static BillboardManager billboardManager;
    private static SelectionManager selectionManager;

    private static SignEditor signEditor;

    private int taskId;

    @Override
    public void onEnable() {

        langConfiguration = new Configuration(this, "lang");
        langConfiguration.saveDefaultConfig();
        billboardsConfiguration = new Configuration(this, "billboards");
        billboardsConfiguration.saveDefaultConfig();

        INSTANCE = this;
        INVENTORY_TITLE = ChatColor.translateAlternateColorCodes('&', langConfiguration.getConfig().getString("inventory_title", "&7Rent a billboard"));
        INVENTORY_CONFIRM_TITLE = ChatColor.translateAlternateColorCodes('&', langConfiguration.getConfig().getString("inventory_confirm_title", "&8Renting confirm"));

        // lang texts
        for (Lang lang : Lang.values()) {
            String name = lang.name().toLowerCase(Locale.ROOT);
            if (!langConfiguration.getConfig().contains(name))
                langConfiguration.getConfig().set(name, lang.getText(false));
            lang.setText(langConfiguration.getConfig().getString(name));
        }

        // economy
        if (!setupEconomy()) {
            getServer().getLogger().severe("Missing Vault on this server !");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // managers
        commandManager = new PaperCommandManager(this);
        commandManager.enableUnstableAPI("help");

        inventoryManager = new InventoryManager(this);
        inventoryManager.init();
        billboardManager = new BillboardManager();

        selectionManager = new SelectionManager();

        // commands
        commandManager.getCommandCompletions().registerAsyncCompletion("billboards", c -> {
            List<String> billboards = new ArrayList<>();
            billboardManager.getBillboards().forEach(billboard -> billboards.add(billboard.getName()));
            return billboards;
        });
        commandManager.registerCommand(new BillboardCommand());

        // listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new SelectionListener(), this);

        // loading billboards from config
        loadBillboards();

        signEditor = new SignEditor();

        // end of sign rents
        taskId = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {

            for (BillboardSign sign : billboardManager.getSigns())
                if (sign.isBought() && sign.getRentEndAt() <= System.currentTimeMillis()) {

                    UUID ownerUid = sign.getOwner();
                    Player owner = ownerUid != null ? getServer().getPlayer(ownerUid) : null;

                    sign.setOwner(null, -1L);
                    Bukkit.getScheduler().runTask(this, sign::resetLines);
                    saveSign(sign);
                    billboardsConfiguration.save();

                    if (owner != null)
                        owner.sendMessage(Lang.CHAT_SIGN_EXPIRED.getText(true)
                                .replace("%slot%", String.valueOf(sign.getSlot()))
                                .replace("%billboard%", sign.getBillboard().getName())
                        );

                }

        }, 0L, 20L).getTaskId();

    }

    @Override
    public void onDisable() {

        getServer().getScheduler().cancelTask(taskId);

    }

    public static Billboards getInstance() {
        return INSTANCE;
    }

    public Configuration getLangConfiguration() {
        return langConfiguration;
    }

    public Configuration getBillboardsConfiguration() {
        return billboardsConfiguration;
    }

    public static Economy getEconomy() {
        return economy;
    }

    public static PaperCommandManager getCommandManager() {
        return commandManager;
    }

    public static InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public static BillboardManager getBillboardManager() {
        return billboardManager;
    }

    public static SelectionManager getSelectionManager() {
        return selectionManager;
    }

    public static SignEditor getSignEditor() {
        return signEditor;
    }

    /**
     * Setup economy for the plugin
     * @return true if the economy has been setup or false if not
     */
    private static boolean setupEconomy() {

        if (Bukkit.getPluginManager().getPlugin("Vault") == null)
            return false;

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            return false;

        economy = rsp.getProvider();
        return true;
    }

    /**
     * Load all billboards from billboards.yml
     */
    public static void loadBillboards() {

        FileConfiguration config = getInstance().getBillboardsConfiguration().getConfig();

        ConfigurationSection cs = config.getConfigurationSection("");
        if (cs == null)
            return;

        cs.getKeys(false).forEach(billboardName -> {

            Location billboardLocation = config.getLocation(billboardName + ".location");

            List<BillboardSign> signs = new ArrayList<>();
            ConfigurationSection signsCs = config.getConfigurationSection(billboardName + ".signs");

            if (signsCs != null) {

                signsCs.getKeys(false).forEach(signSlot -> {

                    try {

                        int slot = Integer.parseInt(signSlot);
                        Location signLocation = config.getLocation(billboardName + ".signs." + signSlot + ".location");
                        long price = config.getLong(billboardName + ".signs." + signSlot + ".price");
                        long rentDuration = config.getLong(billboardName + ".signs." + signSlot + ".rent_duration");

                        Block block = signLocation.getBlock();
                        if (!block.getType().toString().toLowerCase(Locale.ROOT).contains("sign"))
                            block.setType(Material.OAK_WALL_SIGN);
                        BillboardSign sign = new BillboardSign(signLocation, price, rentDuration, slot);
                        signs.add(sign);

                        String ownerUid = config.getString(billboardName + ".signs." + signSlot + ".owner");
                        long boughtAt = config.getLong(billboardName + ".signs." + signSlot + ".bought_at", -1L);
                        List<String> lines = new ArrayList<>();
                        if (config.contains(billboardName + ".signs." + signSlot + ".lines"))
                            lines = config.getStringList(billboardName + ".signs." + signSlot + ".lines");


                        if (ownerUid != null && boughtAt != -1L && !lines.isEmpty()) {

                            sign.setOwner(UUID.fromString(ownerUid), boughtAt);
                            sign.setLines(lines.toArray(new String[0]));

                        }

                    } catch (NumberFormatException ex) {

                        getInstance().getLogger().severe("La pancarte " + signSlot + " est invalide.");

                    }

                });

            } else {

                getInstance().getLogger().info(billboardName + " does not contain sign.");

            }

            Billboard billboard = new Billboard(billboardName, billboardLocation, signs);
            billboardManager.addBillboard(billboard);

        });

    }

    /**
     * Save a billboard in billboards.yml
     * @param billboard Billboard we want to save
     */
    public static void saveBillboard(Billboard billboard) {

        FileConfiguration config = getInstance().getBillboardsConfiguration().getConfig();
        String key = billboard.getName();

        config.set(key + ".location", billboard.getLocation());
        config.set(key + ".signs", null);
        billboard.getSigns().stream()
                .sorted(Comparator.comparingInt(BillboardSign::getSlot))
                .forEach(Billboards::saveSign);
        getInstance().getBillboardsConfiguration().save();

    }

    /**
     * Remove a billboard from billboards.yml
     * @param billboard Billboard we want to remove
     */
    public static void removeBillboard(Billboard billboard) {

        FileConfiguration config = getInstance().getBillboardsConfiguration().getConfig();
        String key = billboard.getName();

        config.set(key, null);
        getInstance().getBillboardsConfiguration().save();
    }

    /**
     * Save BillboardSign in billboards.yml
     * @param sign BillboardSign we want to save
     */
    public static void saveSign(BillboardSign sign) {

        Billboard billboard = sign.getBillboard();
        FileConfiguration config = getInstance().getBillboardsConfiguration().getConfig();
        String key = billboard.getName() + ".signs." + sign.getSlot();

        config.set(key + ".location", sign.getLocation());
        config.set(key + ".price", sign.getPrice());
        config.set(key + ".rent_duration", sign.getRentDuration());
        config.set(key + ".owner", sign.getOwner() != null ? sign.getOwner().toString() : null);
        config.set(key + ".bought_at", sign.isBought() ? sign.getBoughtAt() : null);
        config.set(key + ".lines", sign.hasLines() ? Arrays.asList(sign.getLines()) : null);

    }

    /**
     * Remove a billboard sign from billboards.yml
     * @param sign BillboardSign we want to remove
     */
    public static void removeSign(BillboardSign sign) {

        Billboard billboard = sign.getBillboard();
        FileConfiguration config = getInstance().getBillboardsConfiguration().getConfig();
        String key = billboard.getName() + ".signs." + sign.getSlot();

        config.set(key, null);
        getInstance().getBillboardsConfiguration().save();
    }

    /**
     * Make player rent a BillboardSign. It sets: the player owner of the BillboardSign; the lines of the sign and save it to the billboards.yml
     * @param sign BillboardSign we want to rent to a player
     * @param customer
     * @param lines
     */
    public static void buySign(BillboardSign sign, Player customer, String... lines) {

        customer.playSound(customer.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);
        sign.setOwner(customer.getUniqueId(), System.currentTimeMillis());
        sign.setLines(lines);
        saveSign(sign);
        getInstance().getBillboardsConfiguration().save();

    }

    /**
     * Make player edit fake sign with packets and put his lines on the real sign
     * @param sign BillboardSign we want to put the new lines
     * @param player Player who edits the BillboardSign
     */
    public static void editSign(BillboardSign sign, Player player) {

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        signEditor.open(player, sign, sign::setLines);

    }

    /**
     * Format millis time to readable String
     * @param time millis time we want to format
     * @return formatted time or "0s" if the time was invalid
     */
    public static String formatTime(long time) {

        StringBuilder str = new StringBuilder();

        int days    = (int) ((time / (1000*60*60*24)) % 24);
        if (days > 0)
            str.append(days).append("d ");

        int hours   = (int) ((time / (1000*60*60)) % 24);
        if (hours > 0)
            str.append(hours).append("h ");

        int minutes = (int) ((time / (1000*60)) % 60);
        if (minutes > 0)
            str.append(minutes).append("m ");

        int seconds = (int) (time / 1000) % 60 ;
        if (seconds > 0)
            str.append(seconds).append("s ");

        return str.toString();

    }

}
