package dev.mrnofade.starstrappingutils;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class StuCommands {

    private static final double[] ARROW_DAMAGES = {4.5, 7.5, 11.0};

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(literal("stu")
                    .then(literal("armorstand")
                            .then(argument("count", IntegerArgumentType.integer(1, 500))
                                    .executes(ctx -> spawnEntities(ctx.getSource(), "minecraft:armor_stand", IntegerArgumentType.getInteger(ctx, "count")))))
                    .then(literal("snowgolem")
                            .then(argument("count", IntegerArgumentType.integer(1, 500))
                                    .executes(ctx -> spawnEntities(ctx.getSource(), "minecraft:snow_golem", IntegerArgumentType.getInteger(ctx, "count")))))
                    .then(literal("tntcart")
                            .then(argument("count", IntegerArgumentType.integer(1, 500))
                                    .executes(ctx -> spawnEntities(ctx.getSource(), "minecraft:tnt_minecart", IntegerArgumentType.getInteger(ctx, "count")))))
                    .then(literal("chest")
                            .then(argument("count", IntegerArgumentType.integer(1, 500))
                                    .executes(ctx -> spawnEntities(ctx.getSource(), "minecraft:chest_minecart", IntegerArgumentType.getInteger(ctx, "count")))))
                    .then(literal("hopper")
                            .then(argument("count", IntegerArgumentType.integer(1, 500))
                                    .executes(ctx -> spawnEntities(ctx.getSource(), "minecraft:hopper_minecart", IntegerArgumentType.getInteger(ctx, "count")))))
                    .then(literal("furnace")
                            .then(argument("count", IntegerArgumentType.integer(1, 500))
                                    .executes(ctx -> spawnEntities(ctx.getSource(), "minecraft:furnace_minecart", IntegerArgumentType.getInteger(ctx, "count")))))
                    .then(literal("cart")
                            .then(argument("count", IntegerArgumentType.integer(1, 500))
                                    .executes(ctx -> spawnEntities(ctx.getSource(), "minecraft:minecart", IntegerArgumentType.getInteger(ctx, "count")))))
                    .then(literal("windcharge")
                            .then(argument("count", IntegerArgumentType.integer(1, 500))
                                    .executes(ctx -> spawnEntities(ctx.getSource(), "minecraft:wind_charge", IntegerArgumentType.getInteger(ctx, "count")))))
                    .then(literal("arrow")
                            .then(literal("line")
                                    .then(argument("count", IntegerArgumentType.integer(64, 100))
                                            .executes(ctx -> spawnArrowLine(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "count")))))
                            .then(literal("point")
                                    .then(argument("randomness", com.mojang.brigadier.arguments.FloatArgumentType.floatArg(0f, 2f))
                                            .executes(ctx -> spawnArrowPoint(ctx.getSource(), com.mojang.brigadier.arguments.FloatArgumentType.getFloat(ctx, "randomness"))))
                                    .executes(ctx -> spawnArrowPoint(ctx.getSource(), 0f))))
                    .then(literal("bot")
                            .then(literal("totem")
                                    .then(literal("both")
                                            .executes(ctx -> spawnBot(ctx.getSource(), true)))
                                    .then(literal("main")
                                            .executes(ctx -> spawnBot(ctx.getSource(), false))))
                            .then(literal("clear")
                                    .executes(ctx -> clearBots(ctx.getSource()))))
            )
        );
    }

    private static String uuidToNbt(UUID uuid) {
        long most = uuid.getMostSignificantBits();
        long least = uuid.getLeastSignificantBits();
        return String.format("[I;%d,%d,%d,%d]",
                (int) (most >> 32), (int) most,
                (int) (least >> 32), (int) least);
    }

    private static Vec3d getCrosshairPos(MinecraftClient mc, ClientPlayerEntity player) {
        HitResult target = mc.crosshairTarget;
        if (target != null && target.getType() == HitResult.Type.BLOCK) {
            BlockPos bp = ((BlockHitResult) target).getBlockPos();
            return new Vec3d(bp.getX() + 0.5, bp.getY() + 1, bp.getZ() + 0.5);
        }
        if (target != null && target.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult) target).getEntity().getEntityPos();
        }
        Vec3d look = player.getRotationVec(1.0f);
        return player.getEntityPos().add(look.x * 3, look.y * 3, look.z * 3);
    }

    private static int spawnBot(FabricClientCommandSource source, boolean bothHands) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null) return 0;

        Vec3d pos = getCrosshairPos(mc, player);
        int bx = (int) Math.floor(pos.x);
        int by = (int) Math.floor(pos.y);
        int bz = (int) Math.floor(pos.z);

        player.networkHandler.sendChatCommand(String.format("summon minecraft:mannequin %d %d %d", bx, by, bz));
        player.networkHandler.sendChatCommand("item replace entity @e[type=minecraft:mannequin,limit=1,sort=nearest] armor.head with minecraft:netherite_helmet[enchantments={\"protection\":4}]");
        player.networkHandler.sendChatCommand("item replace entity @e[type=minecraft:mannequin,limit=1,sort=nearest] armor.chest with minecraft:netherite_chestplate[enchantments={\"protection\":4}]");
        player.networkHandler.sendChatCommand("item replace entity @e[type=minecraft:mannequin,limit=1,sort=nearest] armor.legs with minecraft:netherite_leggings[enchantments={\"protection\":4}]");
        player.networkHandler.sendChatCommand("item replace entity @e[type=minecraft:mannequin,limit=1,sort=nearest] armor.feet with minecraft:netherite_boots[enchantments={\"protection\":4}]");
        player.networkHandler.sendChatCommand("item replace entity @e[type=minecraft:mannequin,limit=1,sort=nearest] weapon.mainhand with minecraft:totem_of_undying");
        if (bothHands) {
            player.networkHandler.sendChatCommand("item replace entity @e[type=minecraft:mannequin,limit=1,sort=nearest] weapon.offhand with minecraft:totem_of_undying");
        }
        return 1;
    }

    private static int clearBots(FabricClientCommandSource source) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null) return 0;
        player.networkHandler.sendChatCommand("kill @e[type=minecraft:mannequin,distance=..64]");
        return 1;
    }

    private static int clumpCarts(FabricClientCommandSource source) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null) return 0;

        Vec3d center = getCrosshairPos(mc, player);
        player.networkHandler.sendChatCommand(String.format(
                "tp @e[type=minecraft:tnt_minecart,distance=..32] %.2f %.2f %.2f",
                center.x, center.y, center.z));
        return 1;
    }

    private static int spawnArrowPoint(FabricClientCommandSource source, float randomness) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null) return 0;

        Vec3d center = getCrosshairPos(mc, player);
        String ownerNbt = uuidToNbt(player.getUuid());
        java.util.Random rand = new java.util.Random();

        for (double damage : ARROW_DAMAGES) {
            double rx = (rand.nextDouble() - 0.5) * randomness;
            double rz = (rand.nextDouble() - 0.5) * randomness;
            player.networkHandler.sendChatCommand(String.format(
                    "summon minecraft:arrow %.2f %.2f %.2f {damage:%.1fd,Owner:%s}",
                    center.x + rx, center.y, center.z + rz, damage, ownerNbt));
        }
        return 1;
    }

    private static int spawnArrowLine(FabricClientCommandSource source, int count) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null) return 0;

        Vec3d center = getCrosshairPos(mc, player);
        double yawRad = Math.toRadians(player.getYaw());
        double perpX = Math.cos(yawRad);
        double perpZ = Math.sin(yawRad);
        double totalSpread = 0.6;
        double stepSize = count > 1 ? totalSpread / (count - 1) : 0;
        double startOffset = -(totalSpread / 2.0);
        String ownerNbt = uuidToNbt(player.getUuid());

        for (int i = 0; i < count; i++) {
            double offset = startOffset + i * stepSize;
            double x = center.x + perpX * offset;
            double z = center.z + perpZ * offset;
            for (double damage : ARROW_DAMAGES) {
                player.networkHandler.sendChatCommand(String.format(
                        "summon minecraft:arrow %.2f %.2f %.2f {damage:%.1fd,Owner:%s}",
                        x, center.y, z, damage, ownerNbt));
            }
        }
        return 1;
    }

    private static int spawnEntities(FabricClientCommandSource source, String entityId, int count) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null) return 0;

        Vec3d pos = getCrosshairPos(mc, player);
        int x = (int) Math.floor(pos.x);
        int y = (int) Math.floor(pos.y);
        int z = (int) Math.floor(pos.z);

        for (int i = 0; i < count; i++) {
            player.networkHandler.sendChatCommand(
                    String.format("summon %s %d %d %d", entityId, x, y, z));
        }
        return count;
    }
}
