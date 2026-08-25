package dev.creationcore.menu;

import dev.creationcore.recipe.CreativeCraftingRecipe;
import dev.creationcore.registry.ModBlocks;
import dev.creationcore.registry.ModMenus;
import dev.creationcore.registry.ModRecipes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Optional;

public final class CreativeCraftingMenu extends AbstractContainerMenu {
    private static final int RESULT_SLOT = 0;
    private static final int CRAFT_START = 1;
    private static final int CRAFT_END = 10;
    private static final int INV_START = 10;
    private static final int INV_END = 37;
    private static final int HOTBAR_START = 37;
    private static final int HOTBAR_END = 46;

    private final TransientCraftingContainer craftSlots = new TransientCraftingContainer(this, 3, 3);
    private final ResultContainer resultSlots = new ResultContainer();
    private final ContainerLevelAccess access;
    private final Player player;

    public CreativeCraftingMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, ContainerLevelAccess.NULL);
    }

    public CreativeCraftingMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(ModMenus.CREATIVE_CRAFTING.get(), containerId);
        this.access = access;
        this.player = inventory.player;

        addSlot(new CreativeCraftingResultSlot(this.player, this.craftSlots, this.resultSlots, 0, 124, 35));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new Slot(this.craftSlots, col + row * 3, 30 + col * 18, 17 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public void slotsChanged(Container container) {
        if (player.level() instanceof ServerLevel serverLevel) {
            CraftingInput input = craftSlots.asCraftInput();
            Optional<RecipeHolder<CreativeCraftingRecipe>> creative = serverLevel.getRecipeManager()
                    .getRecipeFor(ModRecipes.CREATIVE_CRAFTING_TYPE.get(), input, serverLevel);

            if (creative.isPresent()) {
                RecipeHolder<CreativeCraftingRecipe> holder = creative.get();
                resultSlots.setRecipeUsed(holder);
                resultSlots.setItem(0, holder.value().assemble(input, serverLevel.registryAccess()));
            } else {
                Optional<RecipeHolder<CraftingRecipe>> normal = serverLevel.getRecipeManager()
                        .getRecipeFor(RecipeType.CRAFTING, input, serverLevel);
                if (normal.isPresent()) {
                    RecipeHolder<CraftingRecipe> holder = normal.get();
                    resultSlots.setRecipeUsed(holder);
                    resultSlots.setItem(0, holder.value().assemble(input, serverLevel.registryAccess()));
                } else {
                    resultSlots.setRecipeUsed(null);
                    resultSlots.setItem(0, ItemStack.EMPTY);
                }
            }
            broadcastChanges();
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        access.execute((level, pos) -> clearContainer(player, craftSlots));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.CREATIVE_CRAFTING_TABLE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index == RESULT_SLOT) {
                if (!moveItemStackTo(stack, INV_START, HOTBAR_END, true)) return ItemStack.EMPTY;
                slot.onQuickCraft(stack, result);
            } else if (index >= INV_START && index < HOTBAR_END) {
                if (!moveItemStackTo(stack, CRAFT_START, CRAFT_END, false)) {
                    if (index < INV_END) {
                        if (!moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) return ItemStack.EMPTY;
                    } else if (!moveItemStackTo(stack, INV_START, INV_END, false)) return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, INV_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
            if (stack.getCount() == result.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, stack);
        }
        return result;
    }
}
