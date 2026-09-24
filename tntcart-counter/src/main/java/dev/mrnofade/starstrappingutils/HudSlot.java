package dev.mrnofade.starstrappingutils;

import net.minecraft.world.item.ItemStack;

public record HudSlot(ItemStack stack, String label, String topLabel, int topColor) {}
