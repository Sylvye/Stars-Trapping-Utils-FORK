package dev.mrnofade.starstrappingutils.fakeplayer;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FakePlayerManager {

    private static final List<ServerPlayerEntity> fakePlayers = new ArrayList<>();

    public static void spawnFakePlayer(MinecraftServer server, String name, Vec3d pos, boolean totemBothHands) throws Exception {
        ServerWorld world = server.getOverworld();
        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        ConnectedClientData clientData = ConnectedClientData.createDefault(profile, false);

        ClientConnection connection = new ClientConnection(NetworkSide.SERVERBOUND);
        EmbeddedChannel channel = new EmbeddedChannel(connection);

        for (Field f : ClientConnection.class.getDeclaredFields()) {
            if (f.getType() == SocketAddress.class) {
                f.setAccessible(true);
                f.set(connection, new InetSocketAddress("localhost", 0));
                break;
            }
        }

        ServerPlayerEntity fakePlayer = new ServerPlayerEntity(server, world, profile, clientData.syncedOptions());
        fakePlayer.setPosition(pos.x, pos.y, pos.z);
        fakePlayer.changeGameMode(GameMode.SURVIVAL);

        fakePlayer.equipStack(EquipmentSlot.HEAD,     new ItemStack(Items.NETHERITE_HELMET));
        fakePlayer.equipStack(EquipmentSlot.CHEST,    new ItemStack(Items.NETHERITE_CHESTPLATE));
        fakePlayer.equipStack(EquipmentSlot.LEGS,     new ItemStack(Items.NETHERITE_LEGGINGS));
        fakePlayer.equipStack(EquipmentSlot.FEET,     new ItemStack(Items.NETHERITE_BOOTS));
        fakePlayer.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.TOTEM_OF_UNDYING));
        if (totemBothHands) {
            fakePlayer.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));
        }

        server.getPlayerManager().onPlayerConnect(connection, fakePlayer, clientData);
        fakePlayers.add(fakePlayer);
    }

    public static void removeAll(MinecraftServer server) {
        for (ServerPlayerEntity p : new ArrayList<>(fakePlayers)) {
            if (!p.isRemoved()) {
                server.getPlayerManager().remove(p);
            }
        }
        fakePlayers.clear();
    }

    public static int count() {
        fakePlayers.removeIf(ServerPlayerEntity::isRemoved);
        return fakePlayers.size();
    }
}
