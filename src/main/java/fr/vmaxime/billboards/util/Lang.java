package fr.vmaxime.billboards.util;

import org.bukkit.ChatColor;

public enum Lang {

    INVENTORY_SLOT_NOT_ASSIGNED("&8This slot is not assigned."),
    INVENTORY_SIGN_ALREADY_RENTED("&4This sign is in rent."),
    INVENTORY_SIGN_AVAILABLE("&aThis sign is available!"),
    INVENTORY_SIGN_OWNER("&7Owner: &c%player%"),
    INVENTORY_SIGN_RENT_END("&7Rental end: &e%time%"),
    INVENTORY_SIGN_RIGHT_CLICK_TO_EDIT("&8Right click to edit."),
    INVENTORY_SIGN_RIGHT_CLICK_TO_DELETE("&8Right click to &cdelete&8."),
    INVENTORY_SIGN_LORE("&7Price: &e%price%$||&7Rent duration: &b%time%|| ||&8Right click to rent"),
    INVENTORY_CONFIRM_I_CONFIRM("&aI confirm my purchase"),
    INVENTORY_CONFIRM_I_CONFIRM_LORE("&7Price: &e%price%$"),
    INVENTORY_CONFIRM_CANCEL("&cCancel my purchase"),
    INVENTORY_CONFIRM_SIGN("&8Renting a sign"),
    INVENTORY_CONFIRM_SIGN_LORE("&7Billboard: &b%billboard%||&7Duration: &b%time%|| ||&7&oYou can edit your signs||&7&oduring the rent."),

    CHAT_SELECT_BILLBOARD_BLOCK("&eSelect the block that will allow players to open the billboard gui."),
    CHAT_SELECT_SIGN("&eSelect a sign."),
    CHAT_THIS_IS_NOT_A_BILLBOARD_SIGN("&cThis is not a billboard sign."),
    CHAT_SIGN_INFO_IN_RENT(" ||&7Billboard: &b%billboard%||&7State: &4This sign is in rent.||&7Owner: &f%owner%||&7Rental end: &e%time%|| "),
    CHAT_SIGN_INFO_AVAILABLE(" ||&7Billboard: &b%billboard%||&7State: &aThis sign is available||&7Price: &e%price%$||&7Rent duration: &b%time%|| "),
    CHAT_NAME_ALREADY_IN_USE("&cThis name is already in use."),
    CHAT_NAME_DOES_NOT_EXIST("&cThis name doesn't exist."),
    CHAT_NAME_HAS_BEEN_CHANGED("&aThe billboard &7%billboard%&a sign name has been changed to &7%new%&a."),
    CHAT_PRICE_HAS_BEEN_CHANGED("&aThe billboard &7%billboard%&a sign price has been changed to &7%price%&a."),
    CHAT_RENT_DURATION_HAS_BEEN_CHANGED("&aThe billboard &7%billboard%&a sign rent duration has been changed to &7%time%&a."),
    CHAT_SLOT_HAS_BEEN_CHANGED("&aThe billboard &7%billboard%&a sign slot has been changed to &7%slot%&a."),
    CHAT_BILLBOARD_CREATED_SUCCESSFULLY("&aThe billboard &7%billboard%&a has been created!"),
    CHAT_BILLBOARD_DELETED_SUCCESSFULLY("&aThe billboard &7%billboard%&a has been deleted!"),
    CHAT_BILLBOARD_FULL("&cThis billboard is full!"),
    CHAT_BILLBOARD_DOES_NOT_EXIST("&cThis billboard doesn't exist."),
    CHAT_BILLBOARD_DOES_NOT_CONTAIN_THIS_SLOT("&cThis billboard doesn't contain sign on slot &7%slot%&c."),
    CHAT_SIGN_ALREADY_USING_SLOT("&cA sign is already using the slot &7%slot%&c."),
    CHAT_SIGN_ALREADY_USING_LOCATION("&cThis location is already in use by another sign."),
    CHAT_SIGN_ADDED_TO_BILLBOARD("&aThe sign has been added to the billboard &7%billboard%&a on slot &7%slot%&a."),
    CHAT_SIGN_DELETED_FROM_BILLBOARD("&aThe sign has been deleted from the billboard &7%billboard%&a."),
    CHAT_SIGN_EXPIRED("&cYour sign on the slot &7%slot%&c of the billboard &7%billboard%&c has expired.."),
    CHAT_RENT_DELETED("&aThe rent has been deleted!"),
    CHAT_NOT_ENOUGH_MONEY("&cYou do not have enough money."),
    CHAT_ERROR_WHILE_PURCHASING("&cAn error occured while purchasing.."),
    CHAT_THIS_BLOCK_IS_NOT_A_SIGN("&cThis block is not a sign."),
    CHAT_SIGNS_HAVE_BEEN_MOVED("&aThe signs have been moved!");

    private String text;

    Lang(String text) {
        this.text = text;
    }

    /**
     * Get the text
     * @param colored true if we want to translate alternate color codes
     * @return String
     */
    public String getText(boolean colored) {
        return colored ? ChatColor.translateAlternateColorCodes('&', text) : text;
    }

    /**
     * Set the text
     * @param text new text (use '&' for colors)
     */
    public void setText(String text) {
        this.text = text;
    }
}
