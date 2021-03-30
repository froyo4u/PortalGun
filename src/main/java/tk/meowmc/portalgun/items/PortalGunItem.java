package tk.meowmc.portalgun.items;

import com.qouteall.immersive_portals.McHelper;
import com.qouteall.immersive_portals.ModMain;
import com.qouteall.immersive_portals.portal.Portal;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import tk.meowmc.portalgun.Portalgun;
import tk.meowmc.portalgun.client.PortalgunClient;
import tk.meowmc.portalgun.misc.PortalMethods;
import tk.meowmc.portalgun.misc.PortalPersistentState;
import tk.meowmc.portalgun.misc.TaskList;

import static net.minecraft.util.hit.HitResult.Type.BLOCK;
import static net.minecraft.util.hit.HitResult.Type.MISS;
import static tk.meowmc.portalgun.misc.PortalMethods.*;

public class PortalGunItem extends Item {
    public static final String KEY = Portalgun.MODID + ":portalgun_portals";
    public static HitResult hit;
    public static BlockHitResult blockHit;
    public static BlockPos blockPos;
    public static Portal newPortal1;
    public static Portal newPortal2;
    public static boolean waitPortal = false;
    public static BlockState space1BlockState;
    public static BlockState space2BlockState;
    public static BlockState space3BlockState;
    BlockPos space1BlockPos;
    BlockPos space2BlockPos;
    BlockPos space3BlockPos;

    Direction direction;
    Vec3d positionCorrectionVec;
    Portal portal1;
    Portal portal2;

    public PortalGunItem(Settings settings) {
        super(settings);
    }

    public static void removeOldPortals(LivingEntity user, PortalPersistentState persistentState, World world) {
        String key1 = user.getUuidAsString() + "-portalGunPortal0";
        String key2 = user.getUuidAsString() + "-portalGunPortal1";
        Portal portal1 = PortalPersistentState.getPortals().get(key1);
        Portal portal2 = PortalPersistentState.getPortals().get(key2);
        if (portal1 != null && portal2 != null) {
            Entity portal1Entity = McHelper.getServerWorld(world.getRegistryKey()).getEntity(portal1.getUuid());
            Entity portal2Entity = McHelper.getServerWorld(world.getRegistryKey()).getEntity(portal2.getUuid());
            if (portal1Entity != null && portal2Entity != null) {
                portal1Entity.kill();
                portal2Entity.kill();
            }
            PortalPersistentState.getPortals().replace(key1, newPortal1);
            PortalPersistentState.getPortals().replace(key2, newPortal2);
            persistentState.markDirty();
            newPortal1.removed = false;
            newPortal2.removed = false;
            savePerstistentState(persistentState, user);
        }
    }

    public static void savePerstistentState(PortalPersistentState persistentState, LivingEntity user) {
        persistentState.markDirty();
        McHelper.getServerWorld(user.world.getRegistryKey()).getPersistentStateManager().set(persistentState);
    }

    public static PortalPersistentState getOrCreatePortalPersistentState(LivingEntity user) {
        return McHelper.getServerWorld(user.world.getRegistryKey()).getPersistentStateManager().getOrCreate(() -> new PortalPersistentState(KEY), KEY);
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return false;
    }

    public void portal1Spawn(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        Entity entity = client.getCameraEntity();
        hit = entity.raycast(50.0D, 0.0F, false);
        blockHit = (BlockHitResult) hit;
        blockPos = blockHit.getBlockPos();

        if (hit.getType() == BLOCK && PortalgunClient.delay) {
            direction = blockHit.getSide();

            PortalPersistentState portalPersistentState = getOrCreatePortalPersistentState(user);

            space1BlockPos = blockPos.add(-1, 0, 0);
            space2BlockPos = blockPos.add(0, -1, 0);
            space3BlockPos = blockPos.add(0, -1, -1);


            switch (direction) {
                case UP:
                    space1BlockPos = blockPos.add(0, 1, 0);
                    space2BlockPos = blockPos.add(0, 0, -1);
                    space3BlockPos = blockPos.add(0, 1, -1);
                    break;
                case DOWN:
                    space1BlockPos = blockPos.add(0, -1, 0);
                    space2BlockPos = blockPos.add(0, 0, -1);
                    space3BlockPos = blockPos.add(0, -1, -1);
                    break;
                case NORTH:
                    space1BlockPos = blockPos.add(0, 0, -1);
                    space3BlockPos = blockPos.add(0, -1, -1);
                    break;
                case SOUTH:
                    space1BlockPos = blockPos.add(0, 0, 1);
                    space3BlockPos = blockPos.add(0, -1, 1);
                    break;
                case EAST:
                    space1BlockPos = blockPos.add(1, 0, 0);
                    space3BlockPos = blockPos.add(1, -1, 0);
                    break;
                case WEST:
                    space3BlockPos = blockPos.add(-1, -1, 0);
                    break;
            }

            dirOut1 = blockHit.getSide().getOpposite().getVector();
            if (dirOut1.getY() == 0) {
                dirUp1 = new Vec3i(0, 1, 0);
            } else {
                dirUp1 = user.getHorizontalFacing().getVector();
            }
            dirRight1 = dirUp1.crossProduct(dirOut1);

            dirRight1 = new Vec3i(-dirRight1.getX(), -dirRight1.getY(), -dirRight1.getZ());

            if (dirUp1.getZ() == -1) {
                space2BlockPos = blockPos.add(0, 0, 1);
                space3BlockPos = blockPos.add(0, 1, 1);
            }

            space1BlockState = world.getBlockState(space1BlockPos);
            space2BlockState = world.getBlockState(space2BlockPos);
            space3BlockState = world.getBlockState(space3BlockPos);

            double distanceX = blockPos.getX() - user.getX();
            double distanceY = blockPos.getY() - (user.getY() + user.getEyeHeight(user.getPose()));
            double distanceZ = blockPos.getZ() - user.getZ();

            Vec3d distanceVec = new Vec3d(distanceX, distanceY, distanceZ);

            double distance = distanceVec.length();
            int delay = (int) (0.5 * distance);

            ModMain.serverTaskList.addTask(TaskList.withDelay(delay, TaskList.oneShotTask(() -> {
                waitPortal = false;
            })));

            if (!user.getItemCooldownManager().isCoolingDown(this))
                user.getItemCooldownManager().set(this, 4);

            if (!world.isClient && !waitPortal && space2BlockState.isOpaque() && !space1BlockState.isOpaque() && !space3BlockState.isOpaque() || space2BlockState.getBlock().is(Blocks.SNOW)) {
                world.playSound(null,
                        user.getX(),
                        user.getY(),
                        user.getZ(),
                        Portalgun.PORTAL1_SHOOT_EVENT,
                        SoundCategory.NEUTRAL,
                        1.0F,
                        1F);

                waitPortal = true;

                PortalMethods.portal1Methods(user, hit);

                if (PortalGunItem.space2BlockState.getBlock().is(Blocks.SNOW) && direction == Direction.UP) {
                    newPortal1.updatePosition(newPortal1.getX(), newPortal1.getY() - 0.875, newPortal1.getZ());
                }

                newPortal1.setDestinationDimension(newPortal2.world.getRegistryKey());

                ModMain.serverTaskList.addTask(TaskList.withDelay(delay, TaskList.oneShotTask(() -> {
                    if (McHelper.getServer().getThread() == Thread.currentThread()) {
                        portal1 = PortalPersistentState.getPortals().get(user.getUuidAsString() + "-portalGunPortal0");
                        portal2 = PortalPersistentState.getPortals().get(user.getUuidAsString() + "-portalGunPortal1");
                        if (portal1 != null && portal2 != null) {
                            world.playSound(null,
                                    newPortal1.getX(),
                                    newPortal1.getY(),
                                    newPortal1.getZ(),
                                    Portalgun.PORTAL_OPEN_EVENT,
                                    SoundCategory.NEUTRAL,
                                    1.0F,
                                    1F);
                            removeOldPortals(user, portalPersistentState, newPortal1.world);
                            removeOldPortals(user, portalPersistentState, newPortal2.world);
                            McHelper.spawnServerEntity(newPortal1);
                            McHelper.spawnServerEntity(newPortal2);
                        }
                        waitPortal = false;
                    }
                    PortalPersistentState.getPortals().put(user.getUuidAsString() + "-portalGunPortal0", newPortal1);
                    PortalPersistentState.getPortals().put(user.getUuidAsString() + "-portalGunPortal1", newPortal2);
                })));
            }
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            savePerstistentState(portalPersistentState, user);
        }
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        user.getItemCooldownManager().set(this, 4);
        Entity entity = client.getCameraEntity();
        hit = entity.raycast(50.0D, 0.0F, false);

        if (hit.getType() == MISS)
            return TypedActionResult.fail(itemStack);
        else if (hit.getType() == BLOCK) {
            Direction direction = ((BlockHitResult) hit).getSide();

            PortalPersistentState portalPersistentState = getOrCreatePortalPersistentState(user);

            BlockHitResult blockHit = (BlockHitResult) hit;
            BlockPos blockPos = blockHit.getBlockPos();
            BlockPos space1BlockPos = blockPos.add(-1, 0, 0);
            BlockPos space2BlockPos = blockPos.add(0, -1, 0);
            BlockPos space3BlockPos = blockPos.add(0, -1, -1);

            switch (direction) {
                case UP:
                    space1BlockPos = blockPos.add(0, 1, 0);
                    space2BlockPos = blockPos.add(0, 0, -1);
                    space3BlockPos = blockPos.add(0, 1, -1);
                    break;
                case DOWN:
                    space1BlockPos = blockPos.add(0, -1, 0);
                    space2BlockPos = blockPos.add(0, 0, -1);
                    space3BlockPos = blockPos.add(0, -1, -1);
                    break;
                case NORTH:
                    space1BlockPos = blockPos.add(0, 0, -1);
                    space3BlockPos = blockPos.add(0, -1, -1);
                    break;
                case SOUTH:
                    space1BlockPos = blockPos.add(0, 0, 1);
                    space3BlockPos = blockPos.add(0, -1, 1);
                    break;
                case EAST:
                    space1BlockPos = blockPos.add(1, 0, 0);
                    space3BlockPos = blockPos.add(1, -1, 0);
                    break;
                case WEST:
                    space3BlockPos = blockPos.add(-1, -1, 0);
                    break;
            }

            dirOut2 = ((BlockHitResult) hit).getSide().getOpposite().getVector();
            if (dirOut2.getY() == 0) {
                dirUp2 = new Vec3i(0, 1, 0);
            } else {
                dirUp2 = client.player.getHorizontalFacing().getVector();
            }
            dirRight2 = dirUp2.crossProduct(dirOut2);

            dirRight2 = new Vec3i(-dirRight2.getX(), -dirRight2.getY(), -dirRight2.getZ());

            if (dirUp2.getZ() == -1) {
                space2BlockPos = blockPos.add(0, 0, 1);
                space3BlockPos = blockPos.add(0, 1, 1);
            }

            space1BlockState = world.getBlockState(space1BlockPos);
            space2BlockState = world.getBlockState(space2BlockPos);
            space3BlockState = world.getBlockState(space3BlockPos);

            double distanceX = blockPos.getX() - user.getX();
            double distanceY = blockPos.getY() - (user.getY() + user.getEyeHeight(user.getPose()));
            double distanceZ = blockPos.getZ() - user.getZ();

            Vec3d distanceVec = new Vec3d(distanceX, distanceY, distanceZ);

            double distance = distanceVec.length();

            int delay = (int) (0.5 * distance);

            client.attackCooldown = 10;
            client.gameRenderer.firstPersonRenderer.resetEquipProgress(user.getActiveHand());

            if (!world.isClient && !waitPortal && space2BlockState.isOpaque() && !space1BlockState.isOpaque() && !space3BlockState.isOpaque() || space2BlockState.getBlock().is(Blocks.SNOW)) {

                world.playSound(null,
                        user.getX(),
                        user.getY(),
                        user.getZ(),
                        Portalgun.PORTAL2_SHOOT_EVENT,
                        SoundCategory.NEUTRAL,
                        1.0F,
                        1F);

                waitPortal = true;


                PortalMethods.portal2Methods(user, hit);

                ModMain.serverTaskList.addTask(TaskList.withDelay(delay, TaskList.oneShotTask(() -> {
                    if (McHelper.getServer().getThread() == Thread.currentThread()) {
                        portal1 = PortalPersistentState.getPortals().get(user.getUuidAsString() + "-portalGunPortal0");
                        portal2 = PortalPersistentState.getPortals().get(user.getUuidAsString() + "-portalGunPortal1");
                        if (portal1 != null && portal2 != null) {
                            removeOldPortals(user, portalPersistentState, newPortal1.world);
                            removeOldPortals(user, portalPersistentState, newPortal2.world);
                            world.playSound(null,
                                    newPortal2.getX(),
                                    newPortal2.getY(),
                                    newPortal2.getZ(),
                                    Portalgun.PORTAL_OPEN_EVENT,
                                    SoundCategory.NEUTRAL,
                                    1.0F,
                                    1F);
                            McHelper.spawnServerEntity(newPortal1);
                            McHelper.spawnServerEntity(newPortal2);
                        }
                        waitPortal = false;
                    }
                    PortalPersistentState.getPortals().put(user.getUuidAsString() + "-portalGunPortal0", newPortal1);
                    PortalPersistentState.getPortals().put(user.getUuidAsString() + "-portalGunPortal1", newPortal2);
                })));
            }
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            savePerstistentState(portalPersistentState, user);
        }

        return TypedActionResult.pass(itemStack);
    }

}
