package tk.meowmc.portalgun.misc;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class RayCast {

    MinecraftClient client;
    HitResult hit;

    public RayCast() {

        Entity entity = this.client.getCameraEntity();
        this.hit = entity.raycast(50.0D, 0.0F, false);

        switch (hit.getType()) {
            case MISS:
                break;
            case BLOCK:
                BlockHitResult blockHit = (BlockHitResult) hit;
                BlockPos blockPos = blockHit.getBlockPos();
                BlockState blockState = client.world.getBlockState(blockPos);
                Block block = blockState.getBlock();
                break;
            case ENTITY:
                EntityHitResult entityHit = (EntityHitResult) hit;
                entity = entityHit.getEntity();
                break;
        }
    }
}
