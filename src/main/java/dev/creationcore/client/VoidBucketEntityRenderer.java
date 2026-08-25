package dev.creationcore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.creationcore.entity.VoidBucketEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Type-safe wrapper around vanilla's ItemEntityRenderer for the temporary
 * void-return bucket entity.
 */
public final class VoidBucketEntityRenderer extends EntityRenderer<VoidBucketEntity> {
    private final ItemEntityRenderer delegate;

    public VoidBucketEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.delegate = new ItemEntityRenderer(context);
        this.shadowRadius = 0.15F;
    }

    @Override
    public void render(VoidBucketEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        delegate.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(VoidBucketEntity entity) {
        return delegate.getTextureLocation(entity);
    }
}
