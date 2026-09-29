package dev.mrnofade.starstrappingutils;

import dev.mrnofade.starstrappingutils.screen.StuSettingsScreen;
import dev.mrnofade.starstrappingutils.screen.StuWaypointsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.projectile.AbstractWindChargeEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
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

    private static KeyBinding openSettings;
    private static KeyBinding setWaypoint;
    private static KeyBinding openWaypoints;
    private static KeyBinding removeWaypoint;

    private static final KeyBinding.Category STU_CATEGORY =
            KeyBinding.Category.create(Identifier.of("starstrappingutils", "keybinds"));

    @Override
    public void onInitializeClient() {
        openSettings = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.starstrappingutils.open_settings",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, STU_CATEGORY
        ));
        setWaypoint = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.starstrappingutils.set_waypoint",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, STU_CATEGORY
        ));
        openWaypoints = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.starstrappingutils.open_waypoints",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, STU_CATEGORY
        ));
        removeWaypoint = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.starstrappingutils.remove_waypoint",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, STU_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.currentScreen == null) {
                if (openSettings.wasPressed()) client.setScreen(new StuSettingsScreen(null));
                if (openWaypoints.wasPressed()) client.setScreen(new StuWaypointsScreen(null));
                if (setWaypoint.wasPressed() && client.player != null && client.world != null) {
                    BlockPos pos = client.player.getBlockPos();
                    String dim = client.world.getRegistryKey().getValue().toString().replace("minecraft:", "");
                    String name = "WP" + (WaypointManager.getWaypoints().size() + 1);
                    WaypointManager.addWaypoint(name, pos, dim);
                    client.player.sendMessage(
                            net.minecraft.text.Text.literal("[STU] Waypoint set: " + name + " at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()),
                            false);
                }
                if (removeWaypoint.wasPressed() && client.player != null && client.world != null) {
                    String dim = client.world.getRegistryKey().getValue().toString().replace("minecraft:", "");
                    WaypointManager.removeNearest(client.player.getEntityPos(), dim);
                    client.player.sendMessage(
                            net.minecraft.text.Text.literal("[STU] Nearest waypoint removed"), false);
                }
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> StuDiscordRPC.update());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            StuDiscordRPC.update();
            StuConfig.save();
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            StuConfig.save();
            StuDiscordRPC.shutdown();
        }));

        StuDiscordRPC.init();
        StuConfig.init();
        WaypointManager.init();
        WaypointWorldRenderer.register();
        StuCommands.register();
        HudRenderCallback.EVENT.register(this::render);
    }

    private void render(DrawContext graphics, RenderTickCounter tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null || mc.options.hudHidden) return;

        InvisWarningRenderer.render(graphics);

        HitResult target = mc.crosshairTarget;
        if (target == null) return;

        List<HudSlot> slots = new ArrayList<>();

        if (target.getType() == HitResult.Type.BLOCK) {
            BlockPos hit = ((BlockHitResult) target).getBlockPos();
            BlockState state = mc.world.getBlockState(hit);

            if (state.getBlock() instanceof AbstractRailBlock) {
                LinkedHashMap<ItemStack, Integer> counts = new LinkedHashMap<>();
                for (Entity entity : mc.world.getEntitiesByClass(Entity.class, new Box(hit), Entity::isAlive)) {
                    if (!shouldShowEntity(entity)) continue;
                    ItemStack stack = entity.getPickBlockStack();
                    if (stack == null || stack.isEmpty()) continue;
                    boolean found = false;
                    for (ItemStack key : counts.keySet()) {
                        if (ItemStack.areItemsAndComponentsEqual(key, stack)) {
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

                if (showRailPower && state.getBlock() instanceof PoweredRailBlock && state.contains(Properties.POWERED)) {
                    boolean powered = state.get(Properties.POWERED);
                    slots.add(new HudSlot(
                            powered ? new ItemStack(Items.POWERED_RAIL) : new ItemStack(Items.RAIL),
                            powered ? "ON" : "OFF", null,
                            powered ? 0xFF00FF00 : 0xFFFF4444));
                }
            }

            if (showArrows) {
                List<PersistentProjectileEntity> arrows = mc.world.getEntitiesByClass(
                        PersistentProjectileEntity.class, new Box(hit).expand(0.5), Entity::isAlive);
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
                List<AbstractWindChargeEntity> wc = mc.world.getEntitiesByClass(
                        AbstractWindChargeEntity.class, new Box(hit).expand(1.0), Entity::isAlive);
                if (!wc.isEmpty()) {
                    slots.add(new HudSlot(new ItemStack(Items.WIND_CHARGE), "x " + wc.size(), null, 0));
                }
            }

        } else if (target.getType() == HitResult.Type.ENTITY) {
            Entity hit = ((EntityHitResult) target).getEntity();

            if (hit instanceof AbstractMinecartEntity || hit instanceof SnowGolemEntity || hit instanceof ArmorStandEntity) {
                LinkedHashMap<ItemStack, Integer> counts = new LinkedHashMap<>();
                for (Entity entity : mc.world.getEntitiesByClass(Entity.class, new Box(hit.getBlockPos()), Entity::isAlive)) {
                    if (!shouldShowEntity(entity)) continue;
                    ItemStack stack = entity.getPickBlockStack();
                    if (stack == null || stack.isEmpty()) continue;
                    boolean found = false;
                    for (ItemStack key : counts.keySet()) {
                        if (ItemStack.areItemsAndComponentsEqual(key, stack)) {
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

            if (showArrows && hit instanceof PersistentProjectileEntity arrow) {
                List<PersistentProjectileEntity> arrows = mc.world.getEntitiesByClass(
                        PersistentProjectileEntity.class, new Box(hit.getBlockPos()).expand(0.5), Entity::isAlive);
                String top = null;
                int topColor = 0xFFFFFFFF;
                if (showArrowDespawn) {
                    int seconds = Math.max(0, 1200 - ((ArrowLifeAccessor) arrow).stu_getLife()) / 20;
                    top = seconds + "s";
                    topColor = seconds > 20 ? 0xFFFFFFFF : 0xFFFF4444;
                }
                slots.add(new HudSlot(new ItemStack(Items.ARROW), "x " + arrows.size(), top, topColor));
            }

            if (showWindCharges && hit instanceof AbstractWindChargeEntity) {
                List<AbstractWindChargeEntity> wc = mc.world.getEntitiesByClass(
                        AbstractWindChargeEntity.class, new Box(hit.getBlockPos()).expand(1.0), Entity::isAlive);
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
                    ? mc.getWindow().getScaledWidth() - 10 - (i + 1) * slotWidth
                    : startX + i * slotWidth;
            graphics.drawItem(slot.stack(), x, y);
            int labelColor = slot.topColor() != 0 ? slot.topColor() : 0xFFFFFFFF;
            graphics.drawText(mc.textRenderer, slot.label(), x + 20, y + 5, labelColor, true);
            if (slot.topLabel() != null) {
                graphics.drawText(mc.textRenderer, slot.topLabel(), x, y - 10, slot.topColor(), true);
            }
        }
    }

    private boolean shouldShowEntity(Entity entity) {
        if (entity instanceof TntMinecartEntity) return showTnt;
        if (entity instanceof HopperMinecartEntity) return showHopper;
        if (entity instanceof ChestMinecartEntity) return showChest;
        if (entity instanceof FurnaceMinecartEntity) return showFurnace;
        if (entity instanceof AbstractMinecartEntity) return showEmpty;
        if (entity instanceof SnowGolemEntity) return showSnowGolem;
        if (entity instanceof ArmorStandEntity) return showArmorStand;
        return false;
    }
}
