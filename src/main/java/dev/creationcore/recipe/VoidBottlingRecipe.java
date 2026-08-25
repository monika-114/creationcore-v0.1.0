package dev.creationcore.recipe;

import dev.creationcore.registry.ModItems;
import dev.creationcore.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class VoidBottlingRecipe extends CustomRecipe {
    public VoidBottlingRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.width() != 3 || input.height() != 3) {
            return false;
        }
        for (int i = 0; i < 9; i++) {
            ItemStack stack = input.getItem(i);
            boolean glassSlot = i == 1 || i == 3 || i == 5 || i == 7;
            if (i == 4) {
                if (!stack.is(ModItems.VOID_BUCKET.get())) return false;
            } else if (glassSlot) {
                if (!stack.is(Items.GLASS)) return false;
            } else if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return new ItemStack(ModItems.BOTTLED_NOTHING.get(), 3);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        if (input.size() > 4 && input.getItem(4).is(ModItems.VOID_BUCKET.get())) {
            remaining.set(4, new ItemStack(Items.BUCKET));
        }
        return remaining;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.VOID_BOTTLING_SERIALIZER.get();
    }
}
