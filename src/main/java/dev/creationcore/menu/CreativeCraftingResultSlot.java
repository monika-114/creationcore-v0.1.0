package dev.creationcore.menu;

import dev.creationcore.recipe.CreativeCraftingRecipe;
import dev.creationcore.registry.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Optional;

/**
 * Result slot used by the Creative Crafting Table.
 *
 * Vanilla ResultSlot always asks RecipeType.CRAFTING for recipe remainders. That is correct for
 * ordinary crafting recipes but not for our exclusive creationcore:creative_crafting recipes.
 * This subclass only handles the exclusive-recipe branch itself and delegates ordinary recipes
 * back to vanilla unchanged.
 */
public final class CreativeCraftingResultSlot extends ResultSlot {
    private final TransientCraftingContainer craftSlots;

    public CreativeCraftingResultSlot(Player player, TransientCraftingContainer craftSlots,
                                      Container resultContainer, int slot, int x, int y) {
        super(player, craftSlots, resultContainer, slot, x, y);
        this.craftSlots = craftSlots;
    }

    @Override
    public void onTake(Player player, ItemStack craftedStack) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            super.onTake(player, craftedStack);
            return;
        }

        CraftingInput input = craftSlots.asCraftInput();
        Optional<RecipeHolder<CreativeCraftingRecipe>> creativeRecipe = serverLevel.getRecipeManager()
                .getRecipeFor(ModRecipes.CREATIVE_CRAFTING_TYPE.get(), input, serverLevel);

        if (creativeRecipe.isEmpty()) {
            // Preserve vanilla behavior (including modded normal crafting recipe remainders).
            super.onTake(player, craftedStack);
            return;
        }

        checkTakeAchievements(craftedStack);
        NonNullList<ItemStack> remaining = creativeRecipe.get().value().getRemainingItems(input);

        for (int slot = 0; slot < craftSlots.getContainerSize(); slot++) {
            ItemStack ingredient = craftSlots.getItem(slot);
            ItemStack remainder = remaining.get(slot);

            if (!ingredient.isEmpty()) {
                craftSlots.removeItem(slot, 1);
                ingredient = craftSlots.getItem(slot);
            }

            if (remainder.isEmpty()) continue;

            if (ingredient.isEmpty()) {
                craftSlots.setItem(slot, remainder);
            } else if (ItemStack.isSameItemSameComponents(ingredient, remainder)) {
                remainder.grow(ingredient.getCount());
                craftSlots.setItem(slot, remainder);
            } else if (!player.getInventory().add(remainder)) {
                player.drop(remainder, false);
            }
        }
    }
}
