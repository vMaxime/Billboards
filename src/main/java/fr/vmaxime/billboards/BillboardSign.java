package fr.vmaxime.billboards;

import fr.vmaxime.billboards.util.SignEditor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.util.Locale;
import java.util.UUID;

public class BillboardSign {

    private Location location;
    private UUID owner;
    private long boughtAt;
    private String[] lines;
    private long price;
    private long rentDuration;
    private int slot;

    public BillboardSign(Location location, UUID owner, long boughtAt, String[] lines, long price, long rentDuration, int slot) {
        this.location = location;
        this.owner = owner;
        this.boughtAt = boughtAt;
        this.lines = lines;
        this.price = price;
        this.rentDuration = rentDuration;
        this.slot = slot;
    }

    public BillboardSign(Location location, long price, long rentDuration, int slot) {
        this(location, null, -1L, null, price, rentDuration, slot);
    }

    /**
     * Get the sign location
     * @return Location of the sign
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Set the sign location
     * @param location new location
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Get the current owner of the sign
     * @return Owner's uuid or null if the sign is not in rent
     */
    public UUID getOwner() {
        return owner;
    }

    /**
     * Put the sign in rent
     * @param owner Player's uuid who rents the sign
     * @param boughtAt timestamp when the rent started
     */
    public void setOwner(UUID owner, long boughtAt) {
        this.owner = owner;
        this.boughtAt = boughtAt;
    }

    /**
     * Get timestamp when the rent started
     * @return timestamp when the rent started or -1 if it didn't start
     */
    public long getBoughtAt() {
        return boughtAt;
    }

    /**
     * Check if the sign is in rent
     * @return true if the sign is in rent or false if he is free
     */
    public boolean isBought() {
        return getBoughtAt() != -1L;
    }

    /**
     * Get timestamp when the rent ends
     * @return timestamp when the rent ends ({@link #getBoughtAt()} + {@link #getRentDuration()}
     */
    public long getRentEndAt() {
        return boughtAt + rentDuration;
    }

    /**
     * Get sign lines
     * @return Sign's lines or null if he doesn't have lines
     */
    public String[] getLines() {
        return lines;
    }

    /**
     * Check if the sign has lines
     * @return true if the sign has lines or false if he doesn't ({@link #getLines()} != null)
     */
    public boolean hasLines() {
        return getLines() != null;
    }

    /**
     * Set the lines of the sign. It takes only the 4 first lines.
     * @param lines Array of String
     */
    public void setLines(String... lines) {
        this.lines = lines;
        Block block = location.getBlock();
        if (block.getType().toString().toLowerCase(Locale.ROOT).contains("wall_sign")) {
            Bukkit.getScheduler().runTask(Billboards.getInstance(), () -> {
                Sign sign = (Sign) block.getState();
                SignEditor.setLines(sign, lines);
            });
        }
    }

    /**
     * Get the sign rent price
     * @return price
     */
    public long getPrice() {
        return price;
    }

    /**
     * Set the sign rent price
     * @param price new price
     */
    public void setPrice(long price) {
        this.price = price;
    }

    /**
     * Get the sign rent duration
     * @return rent duration in millis
     */
    public long getRentDuration() {
        return rentDuration;
    }

    /**
     * Set the sign rent duration
     * @param rentDuration new rent duration in millis
     */
    public void setRentDuration(long rentDuration) {
        this.rentDuration = rentDuration;
    }

    /**
     * Get the sign slot in the billboard gui
     * @return slot in the inventory
     */
    public int getSlot() {
        return slot;
    }

    /**
     * Set the sign slot in the billboard gui
     * @param slot new slot
     */
    public void setSlot(int slot) {
        this.slot = slot;
    }

    /**
     * Get the assigned Billboard
     * @return Billboard
     */
    public Billboard getBillboard() {
        return Billboards.getBillboardManager().getBillboard(this);
    }

    /**
     * Reset lines of the sign
     */
    public void resetLines() {
        setLines("", "", "", "");
        this.lines = null;
    }
}
