package net.nightlyside.hideplayerinregion.events;

import net.nightlyside.hideplayerinregion.MovementWay;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class RegionEnteredEvent extends RegionEvent
{
    /**
     * creates a new RegionEnteredEvent
     * @param region the region the player entered
     * @param player the player who triggered the event
     * @param movement the type of movement how the player entered the region
     * @param from Location the player moved from
     */
    public RegionEnteredEvent(ProtectedRegion region, Player player, MovementWay movement, Location from)
    {
        super(region, player, movement, from);
    }
}
