package dev.mrnofade.starstrappingutils.fakeplayer;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FakePlayerManager {

    private static final List<ServerPlayer> fakePlayers = new ArrayList<>();

    public static void spawnFakePlayer(MinecraftServer server, String name, Vec3 pos, boolean totemBothHands) throws Exception {
        ServerLevel world = server.overworld();
        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        CommonListenerCookie clientData = CommonListenerCookie.createInitial(profile, false);

        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        EmbeddedChannel channel = new EmbeddedChannel(connection);

        for (Field f : Connection.class.getDeclaredFields()) {
            if (f.getType() == SocketAddress.class) {
                f.setAccessible(true);
                f.set(connection, new InetSocketAddress("localhost", 0));
                break;
            }
        }

        ServerPlayer fakePlayer = new ServerPlayer(server, world, profile, clientData.clientInformation());
        fakePlayer.setPos(pos.x, pos.y, pos.z);
        fakePlayer.setGameMode(GameType.SURVIVAL);

        fakePlayer.setItemSlot(EquipmentSlot.HEAD,     new ItemStack(Items.NETHERITE_HELMET));
        fakePlayer.setItemSlot(EquipmentSlot.CHEST,    new ItemStack(Items.NETHERITE_CHESTPLATE));
        fakePlayer.setItemSlot(EquipmentSlot.LEGS,     new ItemStack(Items.NETHERITE_LEGGINGS));
        fakePlayer.setItemSlot(EquipmentSlot.FEET,     new ItemStack(Items.NETHERITE_BOOTS));
        fakePlayer.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TOTEM_OF_UNDYING));
        if (totemBothHands) {
            fakePlayer.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));
        }

        server.getPlayerList().placeNewPlayer(connection, fakePlayer, clientData);
        fakePlayers.add(fakePlayer);
    }

    public static void removeAll(MinecraftServer server) {
        for (ServerPlayer p : new ArrayList<>(fakePlayers)) {
            if (!p.isRemoved()) {
                server.getPlayerList().remove(p);
            }
        }
        fakePlayers.clear();
    }

    public static int count() {
        fakePlayers.removeIf(ServerPlayer::isRemoved);
        return fakePlayers.size();
    }
}
