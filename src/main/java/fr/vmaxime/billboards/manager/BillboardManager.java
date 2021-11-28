package fr.vmaxime.billboards.manager;

import fr.vmaxime.billboards.Billboard;
import fr.vmaxime.billboards.BillboardSign;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BillboardManager {

    private final List<Billboard> billboards;

    public BillboardManager() {
        this.billboards = new ArrayList<>();

    }

    /**
     * Get all registered billboards
     * @return List of Billboard
     */
    public List<Billboard> getBillboards() {
        return billboards;
    }

    /**
     * Get all signs from registered billboards
     * @return List of BillboardSign
     */
    public List<BillboardSign> getSigns() {
        List<BillboardSign> signs = new ArrayList<>();
        billboards.forEach(billboard -> signs.addAll(billboard.getSigns()));
        return signs;
    }

    /**
     * Add billboard to the List of registered billboards
     * @param billboard Billboard we want to register
     */
    public void addBillboard(Billboard billboard) {
        billboards.add(billboard);
    }

    /**
     * Remove billboard from the List of registered billboards
     * @param billboard Billboard we want to unregister
     */
    public void removeBillboard(Billboard billboard) {
        billboards.remove(billboard);
        billboard.getSigns().forEach(BillboardSign::resetLines);
    }

    /**
     * Get Billboard from BillboardSign
     * @param sign BillboardSign we want to find his Billboard
     * @return Billboard if found or null
     */
    public Billboard getBillboard(BillboardSign sign) {
        return billboards.stream()
                .filter(billboard -> billboard.getSigns().contains(sign))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get Billboard from his name
     * @param name Billboard name
     * @return Billboard if found or null
     */
    public Billboard getBillboard(String name) {
        return billboards.stream()
                .filter(billboard -> billboard.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get Billboard from his location
     * @param location Billboard location
     * @return Billboard if found or null
     */
    public Billboard getBillboard(Location location) {
        return billboards.stream()
                .filter(billboard -> billboard.getLocation().equals(location))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get BillboardSign from his location
     * @param location BillboardSign location
     * @return BillboardSign if found or null
     */
    public BillboardSign getSign(Location location) {
        for (Billboard billboard : billboards) {
            Optional<BillboardSign> optional = billboard.getSigns().stream().filter(sign -> sign.getLocation().equals(location)).findFirst();
            if (optional.isPresent())
                return optional.get();
        }
        return null;
    }

}
