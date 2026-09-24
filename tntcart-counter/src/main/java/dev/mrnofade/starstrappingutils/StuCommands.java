package dev.mrnofade.starstrappingutils;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

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
                                    .then(argument("count", IntegerArgumentType.integer(1, 100))
                                            .then(argument("randomness", DoubleArgumentType.doubleArg(0.0))
                                                    .executes(ctx -> spawnArrows(ctx.getSource(),
                                                            IntegerArgumentType.getInteger(ctx, "count"),
                                                            DoubleArgumentType.getDouble(ctx, "randomness"), false)))))
                            .then(literal("point")
                                    .then(argument("count", IntegerArgumentType.integer(1, 100))
                                            .then(argument("randomness", DoubleArgumentType.doubleArg(0.0))
                                                    .executes(ctx -> spawnArrows(ctx.getSource(),
                                                            IntegerArgumentType.getInteger(ctx, "count"),
                                                            DoubleArgumentType.getDouble(ctx, "randomness"), true))))))
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

    private static Vec3 blockSurfaceCenter(BlockPos pos) {
        return new Vec3(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
    }

    private static Vec3 getCrosshairPos(Minecraft mc, LocalPlayer player) {
        HitResult target = mc.hitResult;
        if (target != null && target.getType() == HitResult.Type.BLOCK) {
            return blockSurfaceCenter(((BlockHitResult) target).getBlockPos());
        }
        if (target != null && target.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult) target).getEntity().position();
        }
        Vec3 look = player.getViewVector(1.0f);
        return player.position().add(look.x * 3, look.y * 3, look.z * 3);
    }

    private static int spawnBot(FabricClientCommandSource source, boolean bothHands) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return 0;

        Vec3 pos = getCrosshairPos(mc, player);

        player.connection.sendCommand(String.format("summon minecraft:mannequin %.4f %.4f %.4f", pos.x, pos.y, pos.z));
        player.connection.sendCommand("item replace entity @e[type=minecraft:mannequin,limit=1,sort=nearest] armor.head with minecraft:netherite_helmet[enchantments={\"protection\":4}]");
        player.connection.sendCommand("item replace entity @e[type=minecraft:mannequin,limit=1,sort=nearest] armor.chest with minecraft:netherite_chestplate[enchantments={\"protection\":4}]");
        player.connection.sendCommand("item replace entity @e[type=minecraft:mannequin,limit=1,sort=nearest] armor.legs with minecraft:netherite_leggings[enchantments={\"protection\":4}]");
        player.connection.sendCommand("item replace entity @e[type=minecraft:mannequin,limit=1,sort=nearest] armor.feet with minecraft:netherite_boots[enchantments={\"protection\":4}]");
        player.connection.sendCommand("item replace entity @e[type=minecraft:mannequin,limit=1,sort=nearest] weapon.mainhand with minecraft:totem_of_undying");
        if (bothHands) {
            player.connection.sendCommand("item replace entity @e[type=minecraft:mannequin,limit=1,sort=nearest] weapon.offhand with minecraft:totem_of_undying");
        }

        source.sendFeedback(Component.literal("[STU] Spawned mannequin with " + (bothHands ? "2 totems" : "1 totem") + " + prot 4 netherite"));
        return 1;
    }

    private static int clearBots(FabricClientCommandSource source) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return 0;
        player.connection.sendCommand("kill @e[type=minecraft:mannequin,distance=..64]");
        source.sendFeedback(Component.literal("[STU] Cleared nearby mannequins"));
        return 1;
    }

    private static int clumpCarts(FabricClientCommandSource source) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return 0;

        Vec3 center = getCrosshairPos(mc, player);
        player.connection.sendCommand(String.format(
                "tp @e[type=minecraft:tnt_minecart,distance=..32] %.4f %.4f %.4f",
                center.x, center.y, center.z));

        source.sendFeedback(Component.literal("[STU] Clumped nearby TNT carts"));
        return 1;
    }

    private static Vec3 randomOffset(double radius) {
        if (radius == 0) return Vec3.ZERO;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double angle = random.nextDouble() * Math.PI * 2;
        double distance = Math.sqrt(random.nextDouble()) * radius;
        return new Vec3(Math.cos(angle) * distance, 0, Math.sin(angle) * distance);
    }

    private static int spawnArrows(FabricClientCommandSource source, int count, double randomness, boolean point) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return 0;

        Vec3 center;
        if (point) {
            HitResult target = mc.hitResult;
            if (target == null || target.getType() != HitResult.Type.BLOCK) {
                source.sendError(Component.literal("[STU] Aim at a block for /stu arrow point"));
                return 0;
            }
            center = blockSurfaceCenter(((BlockHitResult) target).getBlockPos());
        } else {
            center = getCrosshairPos(mc, player);
        }
        double yawRad = Math.toRadians(player.getYRot());
        double perpX = Math.cos(yawRad);
        double perpZ = Math.sin(yawRad);
        double totalSpread = 0.6;
        double stepSize = count > 1 ? totalSpread / (count - 1) : 0;
        double startOffset = -(totalSpread / 2.0);
        String ownerNbt = uuidToNbt(player.getUUID());

        for (int i = 0; i < count; i++) {
            double offset = point ? 0 : startOffset + i * stepSize;
            Vec3 base = center.add(perpX * offset, 0, perpZ * offset);
            for (double damage : ARROW_DAMAGES) {
                Vec3 pos = base.add(randomOffset(randomness));
                player.connection.sendCommand(String.format(Locale.ROOT,
                        "summon minecraft:arrow %.6f %.6f %.6f {damage:%.1fd,Owner:%s}",
                        pos.x, pos.y, pos.z, damage, ownerNbt));
            }
        }

        source.sendFeedback(Component.literal("[STU] Spawned " + count + " arrow trios in a "
                + (point ? "point" : "line") + " (P1+P3+P5)"));
        return 1;
    }

    private static int spawnEntities(FabricClientCommandSource source, String entityId, int count) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return 0;

        Vec3 pos = getCrosshairPos(mc, player);
        for (int i = 0; i < count; i++) {
            player.connection.sendCommand(
                    String.format("summon %s %.4f %.4f %.4f", entityId, pos.x, pos.y, pos.z));
        }

        source.sendFeedback(Component.literal("[STU] Spawned " + count + "x " +
                entityId.replace("minecraft:", "") +
                " at " + String.format("%.1f %.1f %.1f", pos.x, pos.y, pos.z)));
        return count;
    }
}
