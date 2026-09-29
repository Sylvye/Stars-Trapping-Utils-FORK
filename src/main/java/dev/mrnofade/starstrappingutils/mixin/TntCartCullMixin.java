package dev.mrnofade.starstrappingutils.mixin;

import dev.mrnofade.starstrappingutils.StarTrappingUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Mixin(EntityRenderer.class)
public class TntCartCullMixin {

    private static final Set<BlockPos> renderedThisFrame = Collections.synchronizedSet(new HashSet<>());
    private static long lastFrameTick = -1;

    @Inject(
        method = "render(Lnet/minecraft/client/render/entity/state/EntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void stuCullDuplicateCarts(EntityRenderState renderState, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState, CallbackInfo ci) {
        if (!StarTrappingUtils.clumpTntCarts) return;
        if (renderState.entityType != EntityType.TNT_MINECART) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return;

        long tick = mc.world.getTime();
        if (tick != lastFrameTick) {
            renderedThisFrame.clear();
            lastFrameTick = tick;
        }

        BlockPos pos = BlockPos.ofFloored(renderState.x, renderState.y, renderState.z);
        if (!renderedThisFrame.add(pos)) {
            ci.cancel();
        }
    }
}
