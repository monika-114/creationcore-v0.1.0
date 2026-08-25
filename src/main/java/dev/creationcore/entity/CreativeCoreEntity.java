package dev.creationcore.entity;

import dev.creationcore.registry.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class CreativeCoreEntity extends ItemEntity {
    public CreativeCoreEntity(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
        initialize();
    }

    public CreativeCoreEntity(Level level, double x, double y, double z) {
        this(dev.creationcore.registry.ModEntities.CREATIVE_CORE.get(), level);
        setPos(x, y, z);
        setItem(new ItemStack(ModItems.CREATIVE_CORE.get()));
        initialize();
    }

    private void initialize() {
        setNoGravity(true);
        setInvulnerable(true);
        setNeverPickUp();
        setUnlimitedLifetime();
        setDeltaMovement(0, 0, 0);
    }

    @Override
    public void tick() {
        super.tick();
        setNoGravity(true);
        setInvulnerable(true);
        setNeverPickUp();
        setUnlimitedLifetime();
        setDeltaMovement(0, 0, 0);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide) {
            ItemStack core = new ItemStack(ModItems.CREATIVE_CORE.get());
            if (!player.addItem(core)) {
                player.drop(core, false);
            }
            discard();
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }
}
