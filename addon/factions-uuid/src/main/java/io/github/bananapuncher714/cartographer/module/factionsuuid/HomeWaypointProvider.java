package io.github.bananapuncher714.cartographer.module.factionsuuid;

import java.util.Collection;
import java.util.Collections;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor.Type;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.WorldCursor;
import io.github.bananapuncher714.cartographer.core.api.map.WorldCursorProvider;
import io.github.bananapuncher714.cartographer.core.map.MapViewer;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;

public class HomeWaypointProvider implements WorldCursorProvider {
	private Type type;
	
	public HomeWaypointProvider( Type type ) {
		this.type = type;
	}
	
	@Override
	public Collection< WorldCursor > getCursors( Player player, Minimap map, PlayerSetting setting ) {
		MapViewer viewer = Cartographer.getInstance().getPlayerManager().getViewerFor( player.getUniqueId() );
		if ( viewer.getSetting( FactionsUUIDModule.FACTION_HOME ) ) {
			Location location = setting.getLocation();
			FPlayer fplayer = FPlayers.fPlayers().get( player );
			if ( fplayer.hasFaction() ) {
				Faction faction = fplayer.faction();
				
				if ( faction.hasHome() ) {
					Location home = faction.home().clone();
					if ( home.getWorld() == location.getWorld() ) {
						home.setYaw( setting.isRotating() ? location.getYaw() : 180 );
						
						return Collections.singleton( new WorldCursor( null, home, type, true ) );
					}
				}
			}
		}
		return Collections.emptyList();
	}
}
