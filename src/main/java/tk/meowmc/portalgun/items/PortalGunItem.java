package tk.meowmc.portalgun.items;

import com.qouteall.immersive_portals.McHelper;
import com.qouteall.immersive_portals.ModMain;
import com.qouteall.immersive_portals.portal.Portal;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
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
import net.minecraft.world.World;
import org.ejml.data.FixedMatrix3x3_64F;
import tk.meowmc.portalgun.Portalgun;
import tk.meowmc.portalgun.misc.PortalMethods;
import tk.meowmc.portalgun.misc.PortalPersistentState;
import tk.meowmc.portalgun.misc.TaskList;

import static net.minecraft.util.hit.HitResult.Type.BLOCK;

public class PortalGunItem extends Item {
    public static final String KEY = Portalgun.MODID + ":portalgun_portals";
    public static HitResult hit;
    public static Portal newPortal1;
    public static Portal newPortal2;
    public static boolean waitPortal = false;
    public MinecraftClient client = MinecraftClient.getInstance();
    FixedMatrix3x3_64F planeMatrix;
    FixedMatrix3x3_64F planeMatrixInverse;
    Direction direction;
    Vec3d positionCorrectionVec;
    Portal portal1;
    Portal portal2;

    public PortalGunItem(Settings settings) {
        super(settings);
    }

    public static void removeOldPortal1(LivingEntity user, PortalPersistentState persistentState, World world) {
        String key = user.getUuidAsString() + "-portalGunPortal0";
        Portal portal = PortalPersistentState.getPortals().get(key);
        if (portal != null) {
            Entity portalEntity = McHelper.getServerWorld(portal.world.getRegistryKey()).getEntity(portal.getUuid());
            if (portalEntity != null) {
                portalEntity.kill();
            }
            PortalPersistentState.getPortals().replace(key, newPortal1);
            persistentState.markDirty();
        }
    }

    public static void removeOldPortal2(LivingEntity user, PortalPersistentState persistentState, World world) {
        String key = user.getUuidAsString() + "-portalGunPortal1";
        Portal portal = PortalPersistentState.getPortals().get(key);
        if (portal != null) {
            Entity portalEntity = McHelper.getServerWorld(portal.world.getRegistryKey()).getEntity(portal.getUuid());
            if (portalEntity != null) {
                portalEntity.kill();
            }
            PortalPersistentState.getPortals().replace(key, newPortal2);
            persistentState.markDirty();
        }
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return false;
    }

    public void savePerstistentState(PortalPersistentState persistentState) {
        persistentState.markDirty();
        McHelper.getServerWorld(client.world.getRegistryKey()).getPersistentStateManager().set(persistentState);
    }

    public void portal1Spawn(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        user.getItemCooldownManager().set(this, 4);
        Entity entity = this.client.getCameraEntity();
        hit = entity.raycast(50.0D, 0.0F, false);

        if (hit.getType() == BLOCK) {
            Direction direction = ((BlockHitResult) hit).getSide();

            PortalPersistentState portalPersistentState = McHelper.getServerWorld(user.world.getRegistryKey()).getPersistentStateManager().getOrCreate(() -> new PortalPersistentState(KEY), KEY);

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

            BlockState space1BlockState = user.world.getBlockState(space1BlockPos);
            BlockState space2BlockState = user.world.getBlockState(space2BlockPos);
            BlockState space3BlockState = user.world.getBlockState(space3BlockPos);

            double distanceX = blockPos.getX() - user.getX();
            double distanceY = blockPos.getY() - (user.getY() + user.getEyeHeight(user.getPose()));
            double distanceZ = blockPos.getZ() - user.getZ();

            Vec3d distanceCombined = new Vec3d(distanceX, distanceY, distanceZ);

            double distance = distanceCombined.length();

            int delay = (int) (0.5 * distance);

            ModMain.serverTaskList.addTask(TaskList.withDelay(delay, TaskList.oneShotTask(() -> {
                waitPortal = false;
            })));

            if (!world.isClient && !waitPortal && !space2BlockState.isAir() && space1BlockState.isAir() && space3BlockState.isAir()) {
                world.playSound(null,
                        user.getX(),
                        user.getY(),
                        user.getZ(),
                        Portalgun.PORTAL1_SHOOT_EVENT,
                        SoundCategory.NEUTRAL,
                        1.0F,
                        1F);

                waitPortal = true;

                ModMain.serverTaskList.addTask(TaskList.withDelay(delay, TaskList.oneShotTask(() -> {

                    PortalMethods.portal1Methods(user, hit);

                    newPortal1.setDestinationDimension(newPortal2.world.getRegistryKey());

                    if (McHelper.getServer().getThread() == Thread.currentThread()) {
                        portal1 = PortalPersistentState.getPortals().get(user.getUuidAsString() + "-portalGunPortal0");
                        portal2 = PortalPersistentState.getPortals().get(user.getUuidAsString() + "-portalGunPortal1");
                        if (portal1 != null && portal2 != null) {
                            removeOldPortal1(user, portalPersistentState, user.world);
                            removeOldPortal2(user, portalPersistentState, user.world);
                            world.playSound(null,
                                    newPortal1.getX(),
                                    newPortal1.getY(),
                                    newPortal1.getZ(),
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

            savePerstistentState(portalPersistentState);
        }
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        user.getItemCooldownManager().set(this, 4);
        Entity entity = this.client.getCameraEntity();
        hit = entity.raycast(50.0D, 0.0F, false);

        if (hit.getType() == BLOCK) {
            Direction direction = ((BlockHitResult) hit).getSide();

            PortalPersistentState portalPersistentState = McHelper.getServerWorld(world.getRegistryKey()).getPersistentStateManager().getOrCreate(() -> new PortalPersistentState(KEY), KEY);

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

            BlockState space1BlockState = user.world.getBlockState(space1BlockPos);
            BlockState space2BlockState = user.world.getBlockState(space2BlockPos);
            BlockState space3BlockState = user.world.getBlockState(space3BlockPos);
            double distanceX = blockPos.getX() - user.getX();
            double distanceY = blockPos.getY() - (user.getY() + user.getEyeHeight(user.getPose()));
            double distanceZ = blockPos.getZ() - user.getZ();

            Vec3d distanceCombined = new Vec3d(distanceX, distanceY, distanceZ);

            double distance = distanceCombined.length();

            int delay = (int) (0.5 * distance);


            if (!world.isClient && !waitPortal && !space2BlockState.isAir() && space1BlockState.isAir() && space3BlockState.isAir()) {

                world.playSound(null,
                        user.getX(),
                        user.getY(),
                        user.getZ(),
                        Portalgun.PORTAL2_SHOOT_EVENT,
                        SoundCategory.NEUTRAL,
                        1.0F,
                        1F);

                waitPortal = true;

                ModMain.serverTaskList.addTask(TaskList.withDelay(delay, TaskList.oneShotTask(() -> {

                    PortalMethods.portal2Mtehods(user, hit);

                    if (McHelper.getServer().getThread() == Thread.currentThread()) {
                        portal1 = PortalPersistentState.getPortals().get(user.getUuidAsString() + "-portalGunPortal0");
                        portal2 = PortalPersistentState.getPortals().get(user.getUuidAsString() + "-portalGunPortal1");
                        if (portal1 != null && portal2 != null) {
                            removeOldPortal1(user, portalPersistentState, world);
                            removeOldPortal2(user, portalPersistentState, world);
                            world.playSound(null,
                                    newPortal2.getX(),
                                    newPortal2.getY(),
                                    newPortal2.getZ(),
                                    Portalgun.PORTAL_OPEN_EVENT,
                                    SoundCategory.NEUTRAL,
                                    1.0F,
                                    1F);
                            McHelper.spawnServerEntity(newPortal2);
                            McHelper.spawnServerEntity(newPortal1);
                        }
                        waitPortal = false;
                    }
                    PortalPersistentState.getPortals().put(user.getUuidAsString() + "-portalGunPortal0", newPortal1);
                    PortalPersistentState.getPortals().put(user.getUuidAsString() + "-portalGunPortal1", newPortal2);
                    waitPortal = false;
                })));


            }
            user.incrementStat(Stats.USED.getOrCreateStat(this));

            savePerstistentState(portalPersistentState);

        }

        return TypedActionResult.success(itemStack, world.isClient());
    }

}
