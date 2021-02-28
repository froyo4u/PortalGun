package tk.meowmc.portalgun.misc;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import tk.meowmc.portalgun.Portalgun;

import static tk.meowmc.portalgun.Portalgun.PORTALGUN;

public class AnimationMethods {

    static MinecraftClient client = MinecraftClient.getInstance();

    public static void doBlockBreaking(boolean bl) {
        if (!bl) {
            client.attackCooldown = 0;
        }

        if (client.attackCooldown <= 0 && !client.player.isUsingItem() && !client.player.isHolding(PORTALGUN)) {
            if (bl && client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) client.crosshairTarget;
                BlockPos blockPos = blockHitResult.getBlockPos();
                if (!client.world.getBlockState(blockPos).isAir()) {
                    Direction direction = blockHitResult.getSide();
                    if (client.interactionManager.updateBlockBreakingProgress(blockPos, direction)) {
                        client.particleManager.addBlockBreakingParticles(blockPos, direction);
                        client.player.swingHand(Hand.MAIN_HAND);
                    }
                }

            } else {
                client.interactionManager.cancelBlockBreaking();
            }
        } else if (!client.player.isUsingItem() && client.player.isHolding(PORTALGUN))
            client.attackCooldown = 10;
    }

    public static void doAttack() {
        if (client.attackCooldown <= 0) {
            if (client.crosshairTarget == null) {
                Portalgun.LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");
                if (client.interactionManager.hasLimitedAttackSpeed()) {
                    client.attackCooldown = 10;
                }

            } else if (!client.player.isRiding()) {
                switch (client.crosshairTarget.getType()) {
                    case ENTITY:
                        client.interactionManager.attackEntity(client.player, ((EntityHitResult) client.crosshairTarget).getEntity());
                        break;
                    case BLOCK:
                        BlockHitResult blockHitResult = (BlockHitResult) client.crosshairTarget;
                        BlockPos blockPos = blockHitResult.getBlockPos();
                        if (!client.world.getBlockState(blockPos).isAir() && !client.player.isHolding(PORTALGUN)) {
                            client.interactionManager.attackBlock(blockPos, blockHitResult.getSide());
                            break;
                        } else if (!client.world.getBlockState(blockPos).isAir() && client.player.isHolding(PORTALGUN))
                            client.attackCooldown = 10;
                    case MISS:
                        if (client.interactionManager.hasLimitedAttackSpeed() || client.player.isHolding(PORTALGUN)) {
                            client.attackCooldown = 10;
                        }

                        client.player.resetLastAttackedTicks();
                }
                if (!client.player.isHolding(PORTALGUN))
                    client.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }

    public static void doItemUse() {
        if (!client.interactionManager.isBreakingBlock()) {
            client.itemUseCooldown = 4;
            if (!client.player.isRiding()) {
                if (client.crosshairTarget == null) {
                    Portalgun.LOGGER.warn("Null returned as 'hitResult', client shouldn't happen!");
                }

                Hand[] var1 = Hand.values();
                int var2 = var1.length;

                for (Hand hand : var1) {
                    ItemStack itemStack = client.player.getStackInHand(hand);
                    if (client.crosshairTarget != null) {
                        switch (client.crosshairTarget.getType()) {
                            case ENTITY:
                                EntityHitResult entityHitResult = (EntityHitResult) client.crosshairTarget;
                                Entity entity = entityHitResult.getEntity();
                                ActionResult actionResult = client.interactionManager.interactEntityAtLocation(client.player, entity, entityHitResult, hand);
                                if (!actionResult.isAccepted()) {
                                    actionResult = client.interactionManager.interactEntity(client.player, entity, hand);
                                }

                                if (actionResult.isAccepted()) {
                                    if (actionResult.shouldSwingHand() && !client.player.isHolding(PORTALGUN)) {
                                        client.player.swingHand(hand);
                                    }

                                    return;
                                }
                                break;
                            case BLOCK:
                                BlockHitResult blockHitResult = (BlockHitResult) client.crosshairTarget;
                                int i = itemStack.getCount();
                                ActionResult actionResult2 = client.interactionManager.interactBlock(client.player, client.world, hand, blockHitResult);
                                if (actionResult2.isAccepted()) {
                                    if (actionResult2.shouldSwingHand()) {
                                        if (!client.player.isHolding(PORTALGUN)) {
                                            client.player.swingHand(hand);
                                        }
                                        if (!itemStack.isEmpty() && (itemStack.getCount() != i || client.interactionManager.hasCreativeInventory())) {
                                            client.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                                        }
                                    } else if (client.player.isHolding(PORTALGUN))
                                        client.attackCooldown = 10;

                                    return;
                                }

                                if (actionResult2 == ActionResult.FAIL) {
                                    return;
                                }
                        }
                    }

                    if (!itemStack.isEmpty()) {
                        ActionResult actionResult3 = client.interactionManager.interactItem(client.player, client.world, hand);
                        if (actionResult3.isAccepted()) {
                            if (actionResult3.shouldSwingHand() && !client.player.isHolding(PORTALGUN)) {
                                client.player.swingHand(hand);
                            } /*else if (client.player.isHolding(PORTALGUN)) {
                                client.attackCooldown = 10;
                            } */

                            client.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                            return;
                        }
                    }
                }

            }
        }
    }

}
