package io.github.bananapuncher714.cartographer.core.implementation.v26_1;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.legacy.CraftLegacy;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursor.Type;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.GeneralUtil;
import io.github.bananapuncher714.cartographer.core.api.PacketHandler;
import io.github.bananapuncher714.cartographer.core.internal.Util_1_17;
import io.github.bananapuncher714.cartographer.core.map.menu.MapInteraction;
import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;
import io.github.bananapuncher714.cartographer.core.util.CrossVersionMaterial;
import io.github.bananapuncher714.cartographer.core.util.MapUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class NMSHandler implements PacketHandler {
    private static final AtomicInteger HANDLER_INDEX = new AtomicInteger();

    private static Map< MapCursor.Type, Holder<MapDecorationType> > CURSOR_TYPES = new HashMap< MapCursor.Type, Holder<MapDecorationType> >();
    private static Field SIMPLECOMMANDMAP_COMMANDS;
    private static Method CRAFTSERVER_SYNCCOMMANDS;
    private static Method GET_TPS;
    private static Field NETWORK_MANAGER;

    static {
        try {
            SIMPLECOMMANDMAP_COMMANDS = SimpleCommandMap.class.getDeclaredField( "knownCommands" );
            SIMPLECOMMANDMAP_COMMANDS.setAccessible( true );

            CRAFTSERVER_SYNCCOMMANDS = CraftServer.class.getDeclaredMethod( "syncCommands" );
            CRAFTSERVER_SYNCCOMMANDS.setAccessible( true );

            NETWORK_MANAGER = ServerCommonPacketListenerImpl.class.getDeclaredField( "connection" );
            NETWORK_MANAGER.setAccessible( true );
        } catch ( Exception exception ) {
            exception.printStackTrace();
        }
            
        try {
            GET_TPS = Bukkit.class.getDeclaredMethod( "getTPS" );
        } catch ( Exception exception ) {
        }

        CURSOR_TYPES.put( MapCursor.Type.PLAYER, MapDecorationTypes.PLAYER );
        CURSOR_TYPES.put( MapCursor.Type.FRAME, MapDecorationTypes.FRAME );
        CURSOR_TYPES.put( MapCursor.Type.RED_MARKER, MapDecorationTypes.RED_MARKER );
        CURSOR_TYPES.put( MapCursor.Type.BLUE_MARKER, MapDecorationTypes.BLUE_MARKER );
        CURSOR_TYPES.put( MapCursor.Type.TARGET_X, MapDecorationTypes.TARGET_X );
        CURSOR_TYPES.put( MapCursor.Type.TARGET_POINT, MapDecorationTypes.TARGET_POINT );
        CURSOR_TYPES.put( MapCursor.Type.PLAYER_OFF_MAP, MapDecorationTypes.PLAYER_OFF_MAP );
        CURSOR_TYPES.put( MapCursor.Type.PLAYER_OFF_LIMITS, MapDecorationTypes.PLAYER_OFF_LIMITS );
        CURSOR_TYPES.put( MapCursor.Type.MANSION, MapDecorationTypes.WOODLAND_MANSION );
        CURSOR_TYPES.put( MapCursor.Type.MONUMENT, MapDecorationTypes.OCEAN_MONUMENT );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_WHITE, MapDecorationTypes.WHITE_BANNER );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_ORANGE, MapDecorationTypes.ORANGE_BANNER );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_MAGENTA, MapDecorationTypes.MAGENTA_BANNER );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_LIGHT_BLUE, MapDecorationTypes.LIGHT_BLUE_BANNER );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_YELLOW, MapDecorationTypes.YELLOW_BANNER );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_LIME, MapDecorationTypes.LIME_BANNER );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_PINK, MapDecorationTypes.PINK_BANNER );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_GRAY, MapDecorationTypes.GRAY_BANNER );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_LIGHT_GRAY, MapDecorationTypes.LIGHT_GRAY_BANNER );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_CYAN, MapDecorationTypes.CYAN_BANNER );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_PURPLE, MapDecorationTypes.PURPLE_BANNER );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_BLUE, MapDecorationTypes.BLUE_BANNER );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_BROWN, MapDecorationTypes.BROWN_BANNER );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_GREEN, MapDecorationTypes.GREEN_BANNER );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_RED, MapDecorationTypes.RED_BANNER );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_BLACK, MapDecorationTypes.BLACK_BANNER );
        CURSOR_TYPES.put( MapCursor.Type.RED_X, MapDecorationTypes.RED_X );
        CURSOR_TYPES.put( MapCursor.Type.VILLAGE_DESERT, MapDecorationTypes.DESERT_VILLAGE );
        CURSOR_TYPES.put( MapCursor.Type.VILLAGE_PLAINS, MapDecorationTypes.PLAINS_VILLAGE );
        CURSOR_TYPES.put( MapCursor.Type.VILLAGE_SAVANNA, MapDecorationTypes.SAVANNA_VILLAGE );
        CURSOR_TYPES.put( MapCursor.Type.VILLAGE_SNOWY, MapDecorationTypes.SNOWY_VILLAGE );
        CURSOR_TYPES.put( MapCursor.Type.VILLAGE_TAIGA, MapDecorationTypes.TAIGA_VILLAGE );
        CURSOR_TYPES.put( MapCursor.Type.JUNGLE_TEMPLE, MapDecorationTypes.JUNGLE_TEMPLE );
        CURSOR_TYPES.put( MapCursor.Type.SWAMP_HUT, MapDecorationTypes.SWAMP_HUT );
        CURSOR_TYPES.put( MapCursor.Type.TRIAL_CHAMBERS, MapDecorationTypes.TRIAL_CHAMBERS );
    }

    private final Map< UUID, Channel > channels = new ConcurrentHashMap< UUID, Channel >();
    private final Set< Integer > maps = new TreeSet< Integer >();
    private Util_1_17 util = new Util_1_17();
    private final String handler_name;

    private final Set< ClientboundMapItemDataPacket > whitelisted = Collections.synchronizedSet( Collections.newSetFromMap( new WeakHashMap< ClientboundMapItemDataPacket, Boolean >() ) );

    public NMSHandler() {
        handler_name = "cartographer2_handler_" + HANDLER_INDEX.getAndIncrement();
    }

    @Override
    public void inject( Player player ) {
        ServerGamePacketListenerImpl conn = ( ( CraftPlayer ) player ).getHandle().connection;
        Connection manager = null;
        try {
            manager = ( Connection ) NETWORK_MANAGER.get( conn );
        } catch ( IllegalArgumentException | IllegalAccessException e ) {
            e.printStackTrace();
        }
        Channel channel = manager.channel;

        if ( channel != null ) {
            channels.put( player.getUniqueId(), channel );
            if ( channel.pipeline().get( handler_name ) != null ) {
                channel.pipeline().remove( handler_name );
            }
            channel.pipeline().addBefore( "packet_handler", handler_name, new PacketInterceptor( player ) );
        }
    }

    @Override
    public void uninject( Player player ) {
        ServerGamePacketListenerImpl conn = ( ( CraftPlayer ) player ).getHandle().connection;
        Connection manager = null;
        try {
            manager = ( Connection ) NETWORK_MANAGER.get( conn );
        } catch ( IllegalArgumentException | IllegalAccessException e ) {
            e.printStackTrace();
        }
        Channel channel = manager.channel;
        channels.remove( player.getUniqueId() );

        if ( channel != null ) {
            if ( channel.pipeline().get( handler_name ) != null ) {
                channel.pipeline().remove( handler_name );
            }
        }
    }

    @Override
    public void sendDataTo( int id, byte[] data, @Nullable MapCursor[] cursors, UUID... uuids ) {
        List< MapDecoration > icons = null;
        if ( cursors != null ) {
            icons = new LinkedList< MapDecoration >();

            for ( int index = 0; index < cursors.length; index++ ) {
                MapCursor cursor = cursors[ index ];

                icons.add( new MapDecoration( CURSOR_TYPES.get( cursor.getType() ), cursor.getX(), cursor.getY(), cursor.getDirection(), Optional.ofNullable( cursor.getCaption() != null ? Component.translatable( cursor.getCaption() ) : null ) ) );
            }
        }

        ClientboundMapItemDataPacket packet = new ClientboundMapItemDataPacket( new MapId( id ), ( byte ) 0, false, icons, new MapItemSavedData.MapPatch( 0, 0, 128, 128, data ) );

        whitelisted.add( packet );

        for ( UUID uuid : uuids ) {
            if ( uuid != null ) {
                Channel channel = channels.get( uuid );
                if ( channel != null ) {
                    channel.pipeline().writeAndFlush( packet );
                }
            }
        }
    }

    private Object onPacketInterceptOut( Player viewer, Object packet ) {
        if ( packet instanceof ClientboundMapItemDataPacket && !whitelisted.contains( packet ) ) {
            try {
                int id = ( ( ClientboundMapItemDataPacket ) packet ).mapId().id();
                if ( maps.contains( id ) ) {
                    return null;
                }
            } catch ( IllegalArgumentException e ) {
                e.printStackTrace();
            }
        }
        return packet;
    }

    private Object onPacketInterceptIn( Player viewer, Object packet ) { 
        if ( viewer != null ) {
            if ( packet instanceof ServerboundPlayerActionPacket && Cartographer.getInstance().getSettings().isPreventDrop() && Cartographer.getInstance().getSettings().isUseDropPacket() ) {
                // Check for the drop packet
                ServerboundPlayerActionPacket digPacket = ( ServerboundPlayerActionPacket ) packet;

                Action type = digPacket.getAction();
                if ( type == Action.DROP_ITEM || type == Action.DROP_ALL_ITEMS ) {
                    ItemStack item = viewer.getEquipment().getItemInMainHand();
                    if ( Cartographer.getInstance().getMapManager().isMinimapItem( item ) ) {
                        // Update the player's hand
                        viewer.getEquipment().setItemInMainHand( item );

                        // Activate the drop
                        Cartographer.getInstance().getMapManager().activate( viewer, type == Action.DROP_ALL_ITEMS ? MapInteraction.CTRLQ : MapInteraction.Q );
                        return null;
                    }
                }
            } else if ( packet instanceof ServerboundClientInformationPacket ) {
                ServerboundClientInformationPacket settings = ( ServerboundClientInformationPacket ) packet;
                Cartographer.getInstance().getPlayerManager().setLocale( viewer.getUniqueId(), settings.information().language() );
            }
        }
        return packet;
    }

    @Override
    public boolean isMapRegistered( int id ) {
        return maps.contains( id );
    }

    @Override
    public void registerMap( int id ) {
        maps.add( id );
    }

    @Override
    public void unregisterMap( int id ) {
        maps.remove( id );
    }

    @Override
    public MapCursor constructMapCursor( int x, int y, double yaw, Type cursorType, String name ) {
        return new MapCursor( ( byte ) x, ( byte ) y, MapUtil.getDirection( yaw ), cursorType, true, name );
    }

    @Override
    public MinimapPalette getVanillaPalette() {
        MinimapPalette palette = new MinimapPalette();
        for ( Block block : BuiltInRegistries.BLOCK ) {
            CrossVersionMaterial material = new CrossVersionMaterial( CraftLegacy.fromLegacy( CraftMagicNumbers.getMaterial( block ) ) );
            boolean transparent = block.defaultBlockState().getRenderShape() == RenderShape.INVISIBLE;
            if ( transparent ) {
                palette.addTransparentMaterial( material );
            } else {
                int color = block.defaultMapColor().col;
                if ( color == 0 ) {
                    palette.addTransparentMaterial( material );
                } else {
                    palette.setColor( material, color );
                }
            }
        }
        palette.getTransparentBlocks().remove( new CrossVersionMaterial( org.bukkit.Material.WATER ) );
        palette.getTransparentBlocks().remove( new CrossVersionMaterial( org.bukkit.Material.LAVA ) );
        palette.setColor( new CrossVersionMaterial( org.bukkit.Material.WATER ), new Color( 64, 64, 255 ) );
        palette.setColor( new CrossVersionMaterial( org.bukkit.Material.LAVA ), new Color( 255, 0, 0 ) );

        return palette;
    }

    @Override
    public double getTPS() {
        if ( GET_TPS != null ) {
            try {
                return ( ( double[] ) GET_TPS.invoke( null ) )[ 0 ];
            } catch ( Exception e ) {
                e.printStackTrace();
            }
            return 20;
        } else {
            // Spigot
            return MinecraftServer.getServer().recentTps[ 0 ];
        }
    }

    @Override
    public boolean registerCommand( PluginCommand command ) {
        Validate.notNull( command );
        return registerCommand( command.getPlugin().getName(), command );
    }

    @Override
    public boolean registerCommand( String fallbackPrefix, PluginCommand command ) {
        Validate.notNull( fallbackPrefix );
        Validate.notNull( command );
        boolean registered = ( ( CraftServer ) Bukkit.getServer() ).getCommandMap().register( fallbackPrefix, command );

        try {
            // Pretty dumb, but apparently you need to re-sync the commands after you do your business or else it won't tab complete properly for players
            CRAFTSERVER_SYNCCOMMANDS.invoke( Bukkit.getServer() );
        } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
            e.printStackTrace();
        }

        return registered;
    }

    @Override
    public void unregisterCommand( PluginCommand command ) {
        Validate.notNull( command );
        try {
            SimpleCommandMap map = ( ( CraftServer ) Bukkit.getServer() ).getCommandMap();
            Map< String, Command > commands = ( Map< String, Command > ) SIMPLECOMMANDMAP_COMMANDS.get( map );
            for ( Iterator< Entry< String, Command > > iterator = commands.entrySet().iterator(); iterator.hasNext(); ) {
                Entry< String, Command > entry = iterator.next();
                if ( entry.getValue() == command ) {
                    iterator.remove();
                }
            }

            try {
                CRAFTSERVER_SYNCCOMMANDS.invoke( Bukkit.getServer() );
            } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
                e.printStackTrace();
            }
        } catch ( IllegalArgumentException | IllegalAccessException e ) {
            e.printStackTrace();
        }
    }

    @Override
    public GeneralUtil getUtil() {
        return util;
    }

    private class PacketInterceptor extends ChannelDuplexHandler {
        public volatile Player player;

        private PacketInterceptor( Player player ) {
            this.player = player;
        }

        @Override
        public void channelRead( ChannelHandlerContext ctx, Object msg ) throws Exception {
            try {
                msg = onPacketInterceptIn( player, msg );
            } catch ( Exception e ) {
                Cartographer.getPlugin( Cartographer.class ).getLogger().log( Level.SEVERE, "Error in onPacketInAsync().", e );
            }

            if ( msg != null ) {
                super.channelRead( ctx, msg );
            }
        }

        @Override
        public void write( ChannelHandlerContext ctx, Object msg, ChannelPromise promise ) throws Exception {
            try {
                msg = onPacketInterceptOut( player, msg );
            } catch ( Exception e ) {
                Cartographer.getPlugin( Cartographer.class ).getLogger().log( Level.SEVERE, "Error in onPacketOutAsync().", e );
            }

            if ( msg != null ) {
                super.write( ctx, msg, promise );
            }
        }
    }
}
