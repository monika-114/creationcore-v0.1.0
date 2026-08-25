package dev.creationcore.registry;

import dev.creationcore.CreativeCoreMod;
import dev.creationcore.recipe.CreativeCraftingRecipe;
import dev.creationcore.recipe.RecipeSerializerWrapper;
import dev.creationcore.recipe.VoidBottlingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, CreativeCoreMod.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, CreativeCoreMod.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<CreativeCraftingRecipe>> CREATIVE_CRAFTING_TYPE =
            TYPES.register("creative_crafting", RecipeType::simple);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CreativeCraftingRecipe>> CREATIVE_CRAFTING_SERIALIZER =
            SERIALIZERS.register("creative_crafting", () -> new RecipeSerializerWrapper<>(
                    ShapedRecipe.Serializer.CODEC.xmap(CreativeCraftingRecipe::new, CreativeCraftingRecipe::delegate),
                    ShapedRecipe.Serializer.STREAM_CODEC.map(CreativeCraftingRecipe::new, CreativeCraftingRecipe::delegate)
            ));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<VoidBottlingRecipe>> VOID_BOTTLING_SERIALIZER =
            SERIALIZERS.register("void_bottling", () -> new CustomRecipe.Serializer<>(VoidBottlingRecipe::new));

    private ModRecipes() {}

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        SERIALIZERS.register(bus);
    }
}
