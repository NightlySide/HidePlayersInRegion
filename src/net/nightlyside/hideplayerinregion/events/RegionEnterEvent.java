package net.nightlyside.hideplayerinregion.events;

import net.nightlyside.hideplayerinregion.MovementWay;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class RegionEnterEvent extends RegionEvent implements Cancellable {
    private boolean cancelled, cancellable;
    /**
     * creates a new RegionEnterEvent
     * @param region the region the player is entering
     * @param player the player who triggered the event
     * @param movement the type of movement how the player enters the region
     * @param from Location the player moved from
     */
    public RegionEnterEvent(ProtectedRegion region, Player player, MovementWay movement, Location from)
    {
        super(region, player, movement, from);
        cancelled = false;
        cancellable = true;
        
        if (movement == MovementWay.SPAWN
            || movement == MovementWay.DISCONNECT)
        {
            cancellable = false;
        }
    }
    
    @Override
    public void setCancelled(boolean cancelled)
    {
        if (!this.cancellable)
        {
            return;
        }
        
        this.cancelled = cancelled;
    }
    
    @Override
    public boolean isCancelled()
    {
        return this.cancelled;
    }
    
    
    public boolean isCancellable()
    {
        return this.cancellable;
    }
    
    protected void setCancellable(boolean cancellable)
    {
        this.cancellable = cancellable;
        
        if (!this.cancellable)
        {
            this.cancelled = false;
        }
    }
}