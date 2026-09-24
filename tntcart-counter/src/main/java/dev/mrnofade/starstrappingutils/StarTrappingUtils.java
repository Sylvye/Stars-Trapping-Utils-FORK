package dev.mrnofade.starstrappingutils;

import com.mojang.blaze3d.platform.InputConstants;
import dev.mrnofade.starstrappingutils.screen.StuSettingsScreen;
import dev.mrnofade.starstrappingutils.screen.StuWaypointsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.entity.vehicle.minecart.MinecartCommandBlock;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.entity.vehicle.minecart.MinecartHopper;
import net.minecraft.world.entity.vehicle.minecart.MinecartSpawner;
import net.minecraft.world.entity.vehicle.minecart.MinecartTNT;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StarTrappingUtils implements ClientModInitializer {

    public static boolean showTnt = true;
    public static boolean showHopper = true;
    public static boolean showChest = true;
    public static boolean showFurnace = true;
    public static boolean showCommandBlock = true;
    public static boolean showSpawner = true;
    public static boolean showEmpty = true;
    public static boolean showSnowGolem = true;
    public static boolean showArmorStand = true;
    public static boolean showArrows = true;
    public static boolean showWindCharges = true;
    public static boolean showRailPower = false;
    public static boolean showArrowDespawn = false;
    public static boolean noSignGui = false;
    public static boolean invisWarning = false;
    public static boolean bowBlocksTrapdoors = false;
    public static HudPosition hudPosition = HudPosition.BOTTOM_LEFT;

    public static double arrowVolume = 1.0;
    public static double minecartVolume = 1.0;

    private static KeyMapping openSettings;
    private static KeyMapping setWaypoint;
    private static KeyMapping openWaypoints;

    private static final KeyMapping.Category STU_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("starstrappingutils", "keybinds"));

    @Override
    public void onInitializeClient() {
        openSettings = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.starstrappingutils.open_settings",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, STU_CATEGORY
        ));
        setWaypoint = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.starstrappingutils.set_waypoint",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, STU_CATEGORY
        ));
        openWaypoints = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.starstrappingutils.open_waypoints",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, STU_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.gui.screen() == null) {
                if (openSettings.consumeClick()) client.gui.setScreen(new StuSettingsScreen(null));
                if (openWaypoints.consumeClick()) client.gui.setScreen(new StuWaypointsScreen(null));
                if (setWaypoint.consumeClick() && client.player != null && client.level != null) {
                    BlockPos pos = client.player.blockPosition();
                    String dim = client.level.dimension().identifier().toString();
                    String name = "WP" + (WaypointManager.getWaypoints().size() + 1);
                    WaypointManager.addWaypoint(name, pos, dim);
                    client.player.sendSystemMessage(
                            net.minecraft.network.chat.Component.literal("[STU] Waypoint set: " + name + " at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()));
                }
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> StuDiscordRPC.update());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> StuDiscordRPC.update());
        Runtime.getRuntime().addShutdownHook(new Thread(StuDiscordRPC::shutdown));

        StuDiscordRPC.init();
        WaypointManager.init();
        StuCommands.register();
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("starstrappingutils", "hud"), this::render);
    }

    private void render(GuiGraphicsExtractor graphics, DeltaTracker tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.gui.hud.isHidden()) return;

        InvisWarningRenderer.render(graphics);
        WaypointManager.renderHud(graphics, tickDelta);

        HitResult target = mc.hitResult;
        if (target == null) return;

        List<HudSlot> slots = new ArrayList<>();

        if (target.getType() == HitResult.Type.BLOCK) {
            BlockPos hit = ((BlockHitResult) target).getBlockPos();
            BlockState state = mc.level.getBlockState(hit);

            if (state.getBlock() instanceof BaseRailBlock) {
                LinkedHashMap<ItemStack, Integer> counts = new LinkedHashMap<>();
                for (Entity entity : mc.level.getEntitiesOfClass(Entity.class, new AABB(hit), Entity::isAlive)) {
                    if (!shouldShowEntity(entity)) continue;
                    ItemStack stack = entity.getPickResult();
                    if (stack == null || stack.isEmpty()) continue;
                    boolean found = false;
                    for (ItemStack key : counts.keySet()) {
                        if (ItemStack.isSameItemSameComponents(key, stack)) {
                            counts.put(key, counts.get(key) + 1);
                            found = true;
                            break;
                        }
                    }
                    if (!found) counts.put(stack, 1);
                }
                for (Map.Entry<ItemStack, Integer> e : counts.entrySet()) {
                    slots.add(new HudSlot(e.getKey(), "x " + e.getValue(), null, 0));
                }

                if (showRailPower && state.getBlock() instanceof PoweredRailBlock && state.hasProperty(BlockStateProperties.POWERED)) {
                    boolean powered = state.getValue(BlockStateProperties.POWERED);
                    slots.add(new HudSlot(
                            powered ? new ItemStack(Items.POWERED_RAIL) : new ItemStack(Items.RAIL),
                            powered ? "ON" : "OFF", null,
                            powered ? 0xFF00FF00 : 0xFFFF4444));
                }
            }

            if (showArrows) {
                List<AbstractArrow> arrows = mc.level.getEntitiesOfClass(
                        AbstractArrow.class, new AABB(hit).inflate(0.5), Entity::isAlive);
                if (!arrows.isEmpty()) {
                    String top = null;
                    int topColor = 0xFFFFFFFF;
                    if (showArrowDespawn) {
                        int seconds = Math.max(0, 1200 - ((ArrowLifeAccessor) arrows.get(0)).stu_getLife()) / 20;
                        top = seconds + "s";
                        topColor = seconds > 20 ? 0xFFFFFFFF : 0xFFFF4444;
                    }
                    slots.add(new HudSlot(new ItemStack(Items.ARROW), "x " + arrows.size(), top, topColor));
                }
            }

            if (showWindCharges) {
                List<AbstractWindCharge> wc = mc.level.getEntitiesOfClass(
                        AbstractWindCharge.class, new AABB(hit).inflate(1.0), Entity::isAlive);
                if (!wc.isEmpty()) {
                    slots.add(new HudSlot(new ItemStack(Items.WIND_CHARGE), "x " + wc.size(), null, 0));
                }
            }

        } else if (target.getType() == HitResult.Type.ENTITY) {
            Entity hit = ((EntityHitResult) target).getEntity();

            if (hit instanceof AbstractMinecart || hit instanceof SnowGolem || hit instanceof ArmorStand) {
                LinkedHashMap<ItemStack, Integer> counts = new LinkedHashMap<>();
                for (Entity entity : mc.level.getEntitiesOfClass(Entity.class, new AABB(hit.blockPosition()), Entity::isAlive)) {
                    if (!shouldShowEntity(entity)) continue;
                    ItemStack stack = entity.getPickResult();
                    if (stack == null || stack.isEmpty()) continue;
                    boolean found = false;
                    for (ItemStack key : counts.keySet()) {
                        if (ItemStack.isSameItemSameComponents(key, stack)) {
                            counts.put(key, counts.get(key) + 1);
                            found = true;
                            break;
                        }
                    }
                    if (!found) counts.put(stack, 1);
                }
                for (Map.Entry<ItemStack, Integer> e : counts.entrySet()) {
                    slots.add(new HudSlot(e.getKey(), "x " + e.getValue(), null, 0));
                }
            }

            if (showArrows && hit instanceof AbstractArrow arrow) {
                List<AbstractArrow> arrows = mc.level.getEntitiesOfClass(
                        AbstractArrow.class, new AABB(hit.blockPosition()).inflate(0.5), Entity::isAlive);
                String top = null;
                int topColor = 0xFFFFFFFF;
                if (showArrowDespawn) {
                    int seconds = Math.max(0, 1200 - ((ArrowLifeAccessor) arrow).stu_getLife()) / 20;
                    top = seconds + "s";
                    topColor = seconds > 20 ? 0xFFFFFFFF : 0xFFFF4444;
                }
                slots.add(new HudSlot(new ItemStack(Items.ARROW), "x " + arrows.size(), top, topColor));
            }

            if (showWindCharges && hit instanceof AbstractWindCharge) {
                List<AbstractWindCharge> wc = mc.level.getEntitiesOfClass(
                        AbstractWindCharge.class, new AABB(hit.blockPosition()).inflate(1.0), Entity::isAlive);
                slots.add(new HudSlot(new ItemStack(Items.WIND_CHARGE), "x " + wc.size(), null, 0));
            }
        }

        if (slots.isEmpty()) return;

        int slotWidth = 45;
        int contentWidth = slots.size() * slotWidth;
        int y = hudPosition.getY(mc);
        boolean rightAnchored = hudPosition == HudPosition.TOP_RIGHT || hudPosition == HudPosition.BOTTOM_RIGHT;
        int startX = hudPosition.getX(mc, contentWidth);

        for (int i = 0; i < slots.size(); i++) {
            HudSlot slot = slots.get(i);
            int x = rightAnchored
                    ? mc.getWindow().getGuiScaledWidth() - 10 - (i + 1) * slotWidth
                    : startX + i * slotWidth;
            graphics.item(slot.stack(), x, y);
            int labelColor = slot.topColor() != 0 ? slot.topColor() : 0xFFFFFFFF;
            graphics.text(mc.font, slot.label(), x + 20, y + 5, labelColor, true);
            if (slot.topLabel() != null) {
                graphics.text(mc.font, slot.topLabel(), x, y - 10, slot.topColor(), true);
            }
        }
    }

    private boolean shouldShowEntity(Entity entity) {
        if (entity instanceof MinecartTNT) return showTnt;
        if (entity instanceof MinecartHopper) return showHopper;
        if (entity instanceof MinecartChest) return showChest;
        if (entity instanceof MinecartFurnace) return showFurnace;
        if (entity instanceof MinecartCommandBlock) return showCommandBlock;
        if (entity instanceof MinecartSpawner) return showSpawner;
        if (entity instanceof AbstractMinecart) return showEmpty;
        if (entity instanceof SnowGolem) return showSnowGolem;
        if (entity instanceof ArmorStand) return showArmorStand;
        return false;
    }
}
