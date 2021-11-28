package fr.vmaxime.billboards.manager;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class SelectionManager {

    private final Map<UUID, Consumer<Block>> selections;

    public SelectionManager() {
        this.selections = new HashMap<>();
    }

    /**
     * Get Consumer that has to be accepted when player select a block
     * @param uuid Player's uuid in selection mod
     * @return Consumer with Block as argument if the player is in selection or null
     */
    public Consumer<Block> getConsumer(UUID uuid) {
        return selections.get(uuid);
    }

    /**
     * Put player in selection mod
     * @param player Player we want to put in selection mod
     * @param onBlockSelected Consumer with Block as argument that will be accepted when the player select a block
     */
    public void makeSelect(Player player, Consumer<Block> onBlockSelected) {
        selections.put(player.getUniqueId(), onBlockSelected);
    }

    /**
     * Remove player from selection mod if he is in
     * @param player Player we want to remove from selection mod
     */
    public void cancelSelection(Player player) {
        selections.remove(player.getUniqueId());
    }

    /**
     * Make player selected a block
     * @param player Player that selected a block
     * @param block Selected block
     * @return true if the player was in selection mod or false if he was not in
     */
    public boolean onSelected(Player player, Block block) {
        Consumer<Block> consumer = getConsumer(player.getUniqueId());
        if (consumer == null)
            return false;

        cancelSelection(player);
        consumer.accept(block);
        return true;
    }


}
