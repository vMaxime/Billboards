package fr.vmaxime.billboards;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class Billboard {

    private String name;
    private Location location;
    private final List<BillboardSign> signs;

    public Billboard(String name, Location location, List<BillboardSign> signs) {
        this.name = name;
        this.location = location;
        this.signs = signs;
    }

    public Billboard(String name, Location location) {
        this(name, location, new ArrayList<>());
    }

    /**
     * Get the Billboard name
     * @return current name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the Billboard name
     * @param name new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the Billboard location
     * @return current location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Set the Billboard location
     * @param location new location
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Get the Billboard signs
     * @return List of BillboardSign
     */
    public List<BillboardSign> getSigns() {
        return signs;
    }

    /**
     * Add BillboardSign to the billboard
     * @param sign BillboardSign we want to register
     */
    public void addSign(BillboardSign sign) {
        signs.add(sign);
    }

    /**
     * Remove BillboardSign from the billboard
     * @param sign BillboardSign we want to unregister
     */
    public void removeSign(BillboardSign sign) {
        signs.remove(sign);
    }

    /**
     * Get BillboardSign on specific slot in the billboard gui
     * @param slot slot in the billboard gui
     * @return BillboardSign on the slot or null
     */
    public BillboardSign getSign(int slot) {
        return signs.stream().filter(sign -> sign.getSlot() == slot).findFirst().orElse(null);
    }

    /**
     * Get BillboardSign from his location
     * @param location Location we want to extract the BillboardSign
     * @return BillboardSign on the location or null
     */
    public BillboardSign getSign(Location location) {
        return signs.stream().filter(sign -> sign.getLocation().equals(location)).findFirst().orElse(null);
    }

    /**
     * Check if the billboard is full (max=54)
     * @return true if the billboard is full or false if not
     */
    public boolean isFull() {
        return signs.size() >= 54;
    }
}
