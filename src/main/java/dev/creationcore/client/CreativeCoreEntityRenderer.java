package dev.creationcore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.creationcore.entity.CreativeCoreEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public final class CreativeCoreEntityRenderer extends EntityRenderer<CreativeCoreEntity> {
    private final ItemEntityRenderer delegate;

    public CreativeCoreEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.delegate = new ItemEntityRenderer(context);
        this.shadowRadius = 0.15F;
    }

    @Override
    public void render(CreativeCoreEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(1.35F, 1.35F, 1.35F);
        delegate.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(CreativeCoreEntity entity) {
        return delegate.getTextureLocation(entity);
    }
}
