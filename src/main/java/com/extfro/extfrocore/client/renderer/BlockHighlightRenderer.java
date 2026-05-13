package com.extfro.extfrocore.client.renderer;

import com.extfro.extfrocore.api.blockentity.PipeBlockEntity;
import com.extfro.extfrocore.api.capability.GTCapabilityHelper;
import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.texture.CroppedTexture;
import com.extfro.extfrocore.api.item.PipeBlockItem;
import com.extfro.extfrocore.api.item.tool.GTToolType;
import com.extfro.extfrocore.api.item.tool.IToolGridHighlight;
import com.extfro.extfrocore.api.item.tool.ToolHelper;
import com.extfro.extfrocore.api.pattern.util.RelativeDirection;
import com.extfro.extfrocore.api.pipenet.IPipeType;
import com.extfro.extfrocore.client.util.RenderUtil;
import com.extfro.extfrocore.common.data.item.GTItemAbilities;
import com.extfro.extfrocore.common.item.behavior.CoverPlaceBehavior;
import com.extfro.extfrocore.common.item.tool.rotation.CustomBlockRotations;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Set;
import java.util.function.Function;

import static com.extfro.extfrocore.utils.GTMatrixUtils.*;

@OnlyIn(Dist.CLIENT)
public class BlockHighlightRenderer {

    public static void renderBlockHighlight(PoseStack poseStack, Camera camera, BlockHitResult target,
                                            MultiBufferSource multiBufferSource, float partialTick) {
        var mc = Minecraft.getInstance();
        var level = mc.level;
        var player = mc.player;
        if (level != null && player != null) {
            ItemStack held = player.getMainHandItem();
            BlockPos blockPos = target.getBlockPos();
            Vector3fc blockCenter = blockPos.getCenter().toVector3f();

            Set<GTToolType> toolType = ToolHelper.getToolTypes(held);
            BlockEntity blockEntity = level.getBlockEntity(blockPos);

            Vec3 cameraPos = camera.getPosition();
            // draw tool grid highlight
            if ((!toolType.isEmpty()) || (held.isEmpty() && player.isShiftKeyDown())) {
                IToolGridHighlight gridHighlight = null;
                if (blockEntity instanceof IToolGridHighlight highLight) {
                    gridHighlight = highLight;
                } else if (level.getBlockState(blockPos).getBlock() instanceof IToolGridHighlight highLight) {
                    gridHighlight = highLight;
                } else
                    if (toolType.contains(GTToolType.WRENCH) || held.canPerformAction(GTItemAbilities.WRENCH_ROTATE)) {
                        var behavior = CustomBlockRotations.getCustomRotation(level.getBlockState(blockPos).getBlock());
                        if (behavior != null && behavior.showGrid()) {
                            gridHighlight = new IToolGridHighlight() {

                                @Override
                                public @Nullable IGuiTexture sideTips(Player player, BlockPos pos, BlockState state,
                                                                      Set<GTToolType> toolTypes, ItemStack held,
                                                                      Direction side) {
                                    return behavior.showSideTip(state, side) ? GuiTextures.TOOL_FRONT_FACING_ROTATION :
                                            null;
                                }
                            };
                        }
                    }
                if (gridHighlight == null) {
                    return;
                }
                BlockState state = level.getBlockState(blockPos);
                poseStack.pushPose();
                if (gridHighlight.shouldRenderGrid(player, blockPos, state, held, toolType)) {
                    final IToolGridHighlight finalGridHighlight = gridHighlight;
                    drawGridOverlays(poseStack, multiBufferSource, cameraPos, target,
                            side -> finalGridHighlight.sideTips(player, blockPos, state, toolType, held, side));
                } else {
                    var facing = target.getDirection();
                    var texture = gridHighlight.sideTips(player, blockPos, state, toolType, held, facing);
                    if (texture != null) {
                        RenderSystem.disableDepthTest();
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();

                        poseStack.translate(facing.getStepX() * 0.01f, facing.getStepY() * 0.01f,
                                facing.getStepZ() * 0.01f);
                        poseStack.translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());

                        RenderUtil.moveToFace(poseStack, blockCenter, facing);
                        if (facing.getAxis() == Direction.Axis.Y) {
                            RenderUtil.rotateToFace(poseStack, facing, Direction.SOUTH);
                        } else {
                            RenderUtil.rotateToFace(poseStack, facing, Direction.NORTH);
                        }
                        poseStack.scale(1f / 16, 1f / 16, 0);
                        poseStack.translate(-8, -8, 0);

                        drawResourceTexture(poseStack, multiBufferSource, texture, 0xffffffff,
                                4, 4, 8, 8);

                        RenderSystem.disableBlend();
                        RenderSystem.enableDepthTest();
                    }
                }
                poseStack.popPose();
                return;
            }

            // draw cover grid highlight
            ICoverable coverable = GTCapabilityHelper.getCoverable(level, blockPos, target.getDirection());
            if (coverable != null && CoverPlaceBehavior.isCoverBehaviorItem(held, coverable::hasAnyCover,
                    coverDef -> ICoverable.canPlaceCover(coverDef, coverable))) {
                poseStack.pushPose();

                drawGridOverlays(poseStack, multiBufferSource, cameraPos, target,
                        side -> coverable.hasCover(side) ? null : GuiTextures.TOOL_ATTACH_COVER);

                poseStack.popPose();
            }

            // draw pipe connection grid highlight
            var pipeType = held.getItem() instanceof PipeBlockItem pipeBlockItem ? pipeBlockItem.getBlock().pipeType :
                    null;
            if (pipeType instanceof IPipeType<?> type && blockEntity instanceof PipeBlockEntity<?, ?> pipeBlockEntity &&
                    pipeBlockEntity.getPipeType().type().equals(type.type())) {
                poseStack.pushPose();

                drawGridOverlays(poseStack, multiBufferSource, cameraPos, target,
                        side -> level.isEmptyBlock(blockPos.relative(side)) ?
                                pipeBlockEntity.getPipeTexture(true) : null);

                poseStack.popPose();
            }
        }
    }

    private static float rColour;
    private static float gColour;
    private static float bColour;

    private static void drawGridOverlays(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 cameraPos,
                                         BlockHitResult blockHitResult, Function<Direction, IGuiTexture> texture) {
        rColour = gColour = 0.2F + (float) Math.sin((System.currentTimeMillis() % (Mth.PI * 800)) / 800) / 2;
        bColour = 1f;
        BlockPos blockPos = blockHitResult.getBlockPos();
        float minX = blockPos.getX();
        float maxX = blockPos.getX() + 1;
        float minY = blockPos.getY();
        float maxY = blockPos.getY() + 1;
        float maxZ = blockPos.getZ() + 1.01f;
        Direction attachSide = ICoverable.traceCoverSide(blockHitResult);
        Vector3f topRight = new Vector3f(maxX, maxY, maxZ);
        Vector3f bottomRight = new Vector3f(maxX, minY, maxZ);
        Vector3f bottomLeft = new Vector3f(minX, minY, maxZ);
        Vector3f topLeft = new Vector3f(minX, maxY, maxZ);
        Vector3f shiftX = new Vector3f(0.25f, 0, 0);
        Vector3f shiftY = new Vector3f(0, 0.25f, 0);

        Vector3f cubeCenter = blockPos.getCenter().toVector3f();

        topRight.sub(cubeCenter);
        bottomRight.sub(cubeCenter);
        bottomLeft.sub(cubeCenter);
        topLeft.sub(cubeCenter);

        Direction front = blockHitResult.getDirection();
        Direction back = front.getOpposite();
        Direction left = RelativeDirection.LEFT.getActualDirection(front);
        Direction right = RelativeDirection.RIGHT.getActualDirection(front);
        Direction top = RelativeDirection.UP.getActualDirection(front);
        Direction bottom = RelativeDirection.DOWN.getActualDirection(front);

        Quaternionfc rotation = getRotation(Direction.SOUTH, front);
        topRight.rotate(rotation);
        bottomRight.rotate(rotation);
        bottomLeft.rotate(rotation);
        topLeft.rotate(rotation);
        shiftX.rotate(rotation);
        shiftY.rotate(rotation);

        IGuiTexture leftBlocked = texture.apply(left);
        IGuiTexture rightBlocked = texture.apply(right);
        IGuiTexture topBlocked = texture.apply(top);
        IGuiTexture bottomBlocked = texture.apply(bottom);
        IGuiTexture frontBlocked = texture.apply(front);
        IGuiTexture backBlocked = texture.apply(back);

        topRight.add(cubeCenter);
        bottomRight.add(cubeCenter);
        bottomLeft.add(cubeCenter);
        topLeft.add(cubeCenter);

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.lines());
        RenderSystem.lineWidth(3);
        PoseStack.Pose pose = poseStack.last();
        // straight top bottom lines
        drawLine(pose, buffer, new Vector3f(topRight).sub(shiftX), new Vector3f(bottomRight).sub(shiftX));
        drawLine(pose, buffer, new Vector3f(bottomLeft).add(shiftX), new Vector3f(topLeft).add(shiftX));
        // straight side to side lines
        drawLine(pose, buffer, new Vector3f(topLeft).sub(shiftY), new Vector3f(topRight).sub(shiftY));
        drawLine(pose, buffer, new Vector3f(bottomLeft).add(shiftY), new Vector3f(bottomRight).add(shiftY));

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        poseStack.pushPose();
        poseStack.translate(front.getStepX() * 0.01f, front.getStepY() * 0.01f, front.getStepZ() * 0.01f);

        RenderUtil.moveToFace(poseStack, cubeCenter, front);
        RenderUtil.rotateToFace(poseStack, front, Direction.SOUTH);
        poseStack.scale(1f / 16, 1f / 16, 0);
        poseStack.translate(-8, -8, 0);
        poseStack.scale(0.9f, 0.9f, 1);

        if (leftBlocked != null) {
            int color = attachSide == left ? 0xffffffff : 0x44ffffff;
            drawResourceTexture(poseStack, bufferSource, leftBlocked, color, 0, 6, 4, 4);
        }
        if (topBlocked != null) {
            int color = attachSide == top ? 0xffffffff : 0x44ffffff;
            drawResourceTexture(poseStack, bufferSource, topBlocked, color, 6, 12, 4, 4);
        }
        if (rightBlocked != null) {
            int color = attachSide == right ? 0xffffffff : 0x44ffffff;
            drawResourceTexture(poseStack, bufferSource, rightBlocked, color, 12, 6, 4, 4);
        }
        if (bottomBlocked != null) {
            int color = attachSide == bottom ? 0xffffffff : 0x44ffffff;
            drawResourceTexture(poseStack, bufferSource, bottomBlocked, color, 6, 0, 4, 4);
        }
        if (frontBlocked != null) {
            int color = attachSide == front ? 0xffffffff : 0x44ffffff;
            drawResourceTexture(poseStack, bufferSource, frontBlocked, color, 6, 6, 4, 4);
        }
        if (backBlocked != null) {
            int color = attachSide == back ? 0xffffffff : 0x44ffffff;
            drawResourceTexture(poseStack, bufferSource, backBlocked, color, 0, 0, 4, 4);
            drawResourceTexture(poseStack, bufferSource, backBlocked, color, 12, 0, 4, 4);
            drawResourceTexture(poseStack, bufferSource, backBlocked, color, 0, 12, 4, 4);
            drawResourceTexture(poseStack, bufferSource, backBlocked, color, 12, 12, 4, 4);
        }
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();

        poseStack.popPose();
        poseStack.popPose();
    }

    private static void drawLine(PoseStack.Pose pose, VertexConsumer buffer, Vector3fc from, Vector3fc to) {
        Vector3f normal = from.sub(to, new Vector3f());

        buffer.addVertex(pose.pose(), from.x(), from.y(), from.z())
                .setColor(rColour, gColour, bColour, 1f)
                .setNormal(normal.x(), normal.y(), normal.z());
        buffer.addVertex(pose.pose(), to.x(), to.y(), to.z())
                .setColor(rColour, gColour, bColour, 1f)
                .setNormal(normal.x(), normal.y(), normal.z());
    }

    private static void drawResourceTexture(PoseStack poseStack, MultiBufferSource bufferSource,
                                            IGuiTexture texture, int color,
                                            float x, float y, float w, float h) {
        TextureUV uv = getTextureUV(texture);
        if (uv == null) {
            return;
        }
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.text(uv.location));
        var pose = poseStack.last().pose();
        float u0 = uv.u;
        float v0 = uv.v;
        float u1 = uv.width;
        float v1 = uv.height;
        // spotless:off
        consumer.addVertex(pose, x, y + h, 0).setColor(color).setUv(u0, v0 + v1).setLight(LightTexture.FULL_BRIGHT);
        consumer.addVertex(pose, x + w, y + h, 0).setColor(color).setUv(u0 + u1, v0 + v1).setLight(LightTexture.FULL_BRIGHT);
        consumer.addVertex(pose, x + w, y, 0).setColor(color).setUv(u0 + u1, v0).setLight(LightTexture.FULL_BRIGHT);
        consumer.addVertex(pose, x, y, 0).setColor(color).setUv(u0, v0).setLight(LightTexture.FULL_BRIGHT);
        // spotless:on
    }

    private static @Nullable TextureUV getTextureUV(IGuiTexture texture) {
        if (texture instanceof CroppedTexture croppedTexture) {
            return new TextureUV(croppedTexture.location(), croppedTexture.u(), croppedTexture.v(),
                    croppedTexture.width(), croppedTexture.height());
        }
        if (texture instanceof SpriteTexture spriteTexture) {
            var imageSize = spriteTexture.getImageSize();
            var spriteSize = spriteTexture.spriteSize;
            var spritePosition = spriteTexture.spritePosition;
            int textureWidth = imageSize.getWidth();
            int textureHeight = imageSize.getHeight();
            int spriteWidth = spriteSize.getWidth() > 0 ? spriteSize.getWidth() : textureWidth;
            int spriteHeight = spriteSize.getHeight() > 0 ? spriteSize.getHeight() : textureHeight;
            return new TextureUV(spriteTexture.getImageLocation(),
                    (float) spritePosition.getX() / textureWidth,
                    (float) spritePosition.getY() / textureHeight,
                    (float) spriteWidth / textureWidth,
                    (float) spriteHeight / textureHeight);
        }
        return null;
    }

    private record TextureUV(net.minecraft.resources.ResourceLocation location, float u, float v, float width,
                             float height) {}
}
