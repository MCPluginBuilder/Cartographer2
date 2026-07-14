package io.github.bananapuncher714.cartographer.module.worldguard;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.github.bananapuncher714.cartographer.core.api.events.minimap.MinimapLoadEvent;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingState;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingStateBoolean;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.palette.PaletteManager;
import io.github.bananapuncher714.cartographer.core.module.Module;
import io.github.bananapuncher714.cartographer.core.util.FileUtil;
import io.github.bananapuncher714.cartographer.module.worldguard.api.WorldGuardWrapper;

public class WorldGuardModule extends Module implements Listener {
	public static final SettingStateBoolean WORLDGUARD_REGIONS = SettingStateBoolean.of( "worldguard_show_regions", false, true );
	
	protected List< ColorRule > colorRules;
	protected RegionColors defaultColors;
	
	protected WorldGuardWrapper wrapper;
	
	@Override
	public void onEnable() {
		registerSettings();

		FileUtil.saveToFile( getResource( "config.yml" ), new File( getDataFolder(), "/config.yml" ), false );
		FileUtil.saveToFile( getResource( "README.md" ), new File( getDataFolder(), "/README.md" ), false );

		wrapper = getWrapperImpl();
		
		if ( wrapper == null ) {
			getLogger().severe( "No compatible WorldGuard version found! Disabling..." );
			getCartographer().getModuleManager().disableModule( this );
			return;
		}
		
		colorRules = new ArrayList<>();
		loadConfig();

		for ( Minimap minimap : getCartographer().getMapManager().getMinimaps().values() ) {
			init( minimap );
		}

		registerListener( this );
	}
	
	@Override
	public SettingState< ? >[] getSettingStates() {
		SettingState< ? >[] states = new SettingState< ? >[] {
			WORLDGUARD_REGIONS
		};
		return states;
	}
	
	private WorldGuardWrapper getWrapperImpl() {
		try {
            Class.forName( "com.sk89q.worldguard.WorldGuard" );
            return new io.github.bananapuncher714.cartographer.module.worldguard.implementation.v7.WorldGuardWrapperImpl();
        } catch (ClassNotFoundException e) {
            try {
                Class.forName( "com.sk89q.worldguard.protection.flags.registry.FlagRegistry" );
                return new io.github.bananapuncher714.cartographer.module.worldguard.implementation.v7.WorldGuardWrapperImpl();
            } catch (ClassNotFoundException e1) {
                getLogger().severe( "WorldGuard v6 and v7 not found!" );
            }
        }
		return null;
	}
	
	@EventHandler
	private void onEvent( MinimapLoadEvent event ) {
		init( event.getMinimap() );
	}

	private void init( Minimap minimap ) {
		minimap.register( new RegionBorderShader( this ) );
	}
	
	private void loadConfig() {
		colorRules.clear();
		
		FileConfiguration config = YamlConfiguration.loadConfiguration( new File( getDataFolder(), "config.yml" ) );
		ConfigurationSection section = config.getConfigurationSection( "colors" );
		
		if ( section == null ) {
			getLogger().warning( "No 'colors' section found!" );
		} else {
			defaultColors = loadFrom( section.getConfigurationSection( "default" ) );
			
			if ( section.contains( "regions" ) ) {
				ConfigurationSection regionSection = section.getConfigurationSection( "regions" );
				
				for ( String key : regionSection.getKeys( false ) ) {
					
					ConfigurationSection region = regionSection.getConfigurationSection( key );
				
					if ( region == null ) {
						continue;
					}
					
					// If the "pattern" setting is defined, use this value for extended pattern definition.
					if ( region.contains( "pattern" ) ) {
						try {
							String regex = "^" + region.getString( "pattern" ) + "$";
							colorRules.add( new ColorRule( Pattern.compile( regex ), loadFrom( regionSection.getConfigurationSection( key ) ) ) );
						} catch ( PatternSyntaxException ex ) {
							getLogger().warning( "Invalid region regex '" + key + "': " + ex.getMessage() );
						}
						
					} else {
						// Alternative: Use the config key for region name comparison.
						colorRules.add( new ColorRule( key, loadFrom( regionSection.getConfigurationSection( key ) ) ) );
					}
					
				}
			}
		}
	}
	
	private RegionColors loadFrom( ConfigurationSection section ) {
		Color nonmember = PaletteManager.fromString( section.getString( "nonmember" ) ).get();
		Color member = PaletteManager.fromString( section.getString( "member" ) ).get();
		Color owner = PaletteManager.fromString( section.getString( "owner" ) ).get();
		return new RegionColors( owner, member, nonmember );
	}
	
	RegionColors getFor( String targetRegionName ) {
		// Go through the configuration entries from bottom to top:
		for ( int i = colorRules.size() - 1; i >= 0; i-- ) {
			
			ColorRule rule = colorRules.get( i );
			
			if ( rule.isMatch( targetRegionName ) ) {
				return rule.getColor();
			}
		}
		return defaultColors;
	}
	
	protected WorldGuardWrapper getWrapper() {
		return wrapper;
	}
}
