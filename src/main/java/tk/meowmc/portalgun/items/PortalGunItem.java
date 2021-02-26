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
import static net.minecraft.util.hit.HitResult.Type.MISS;

public class PortalGunItem extends Item {
    public PortalGunItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return false;
    }

    public static final String KEY = Portalgun.MODID + ":portalgun_portals";
    public MinecraftClient client = MinecraftClient.getInstance();
    public static HitResult hit;
    FixedMatrix3x3_64F planeMatrix;
    FixedMatrix3x3_64F planeMatrixInverse;
    Direction direction;
    Vec3d positionCorrectionVec;
    public static Portal newPortal1;
    public static Portal newPortal2;
    Portal portal1;
    Portal portal2;
    public static boolean waitPortal1 = false;
    public static boolean waitPortal2 = false;

    public static void removeOldPortal1(LivingEntity user, PortalPersistentState persistentState, World world) {
            String key = user.getUuidAsString() + "-portalGunPortal0";
            Portal portal = persistentState.getPortals().get(key);
            if (portal != null) {
                Entity portalEntity = McHelper.getServerWorld(portal.world.getRegistryKey()).getEntity(portal.getUuid());
                if (portalEntity != null) {
                    portalEntity.kill();
                }
                persistentState.getPortals().remove(key);
                persistentState.markDirty();
            }
    }

    public static void removeOldPortal2(LivingEntity user, PortalPersistentState persistentState, World world) {
            String key = user.getUuidAsString() + "-portalGunPortal1";
            Portal portal = persistentState.getPortals().get(key);
            if (portal != null){
                Entity portalEntity = McHelper.getServerWorld(portal.world.getRegistryKey()).getEntity(portal.getUuid());
                if (portalEntity != null){
                    portalEntity.kill();
                }
                persistentState.getPortals().remove(key);
                persistentState.markDirty();
            }
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

            double distanceX = blockPos.getX() - user.getX();
            double distanceY = blockPos.getY() - (user.getY() + user.getEyeHeight(user.getPose()));
            double distanceZ = blockPos.getZ() - user.getZ();

            Vec3d distanceCombined = new Vec3d(distanceX, distanceY, distanceZ);

            double distance = distanceCombined.length();

            int delay = (int) (0.5 * distance);

            ModMain.serverTaskList.addTask(TaskList.withDelay(delay, TaskList.oneShotTask(() -> {
                waitPortal1 = false;
            })));

            if (!world.isClient && !waitPortal1) {
                world.playSound(null,
                        user.getX(),
                        user.getY(),
                        user.getZ(),
                        Portalgun.PORTAL1_SHOOT_EVENT,
                        SoundCategory.NEUTRAL,
                        1.0F,
                        1F);

                waitPortal1 = true;

                ModMain.serverTaskList.addTask(TaskList.withDelay(delay, TaskList.oneShotTask(() -> {

                    PortalMethods.portal1Methods(user, hit);

                    newPortal1.setDestinationDimension(newPortal2.world.getRegistryKey());

                    if (McHelper.getServer().getThread() == Thread.currentThread()) {
                        portal1 = portalPersistentState.getPortals().get(user.getUuidAsString() + "-portalGunPortal0");
                        portal2 = portalPersistentState.getPortals().get(user.getUuidAsString() + "-portalGunPortal1");
                        if (portal1 != null && portal2 != null && !waitPortal2) {
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
                        waitPortal1 = false;
                        waitPortal2 = false;
                    }

                })));
            }
            user.incrementStat(Stats.USED.getOrCreateStat(this));

            portalPersistentState.getPortals().put(user.getUuidAsString() + "-portalGunPortal0", newPortal1);
            portalPersistentState.getPortals().put(user.getUuidAsString() + "-portalGunPortal1", newPortal2);

            savePerstistentState(portalPersistentState);
        }
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        user.getItemCooldownManager().set(this, 4);
        Entity entity = this.client.getCameraEntity();
        hit = entity.raycast(50.0D, 0.0F, false);

        if (hit.getType() == MISS) {
            return TypedActionResult.fail(itemStack);
        } else if (hit.getType() == BLOCK){
            Direction direction = ((BlockHitResult) hit).getSide();

            PortalPersistentState portalPersistentState = McHelper.getServerWorld(user.world.getRegistryKey()).getPersistentStateManager().getOrCreate(() -> new PortalPersistentState(KEY), KEY);

            BlockHitResult blockHit = (BlockHitResult) hit;
            BlockPos blockPos = blockHit.getBlockPos();

            double distanceX = blockPos.getX()-user.getX();
            double distanceY = blockPos.getY()-(user.getY()+user.getEyeHeight(user.getPose()));
            double distanceZ = blockPos.getZ()-user.getZ();

            Vec3d distanceCombined = new Vec3d(distanceX, distanceY, distanceZ);

            double distance = distanceCombined.length();

            int delay = (int) (0.5*distance);

            ModMain.serverTaskList.addTask(TaskList.withDelay(delay, TaskList.oneShotTask(() -> {
                waitPortal2 = false;
            })));

            if (!world.isClient && !waitPortal2) {
                world.playSound(null,
                            user.getX(),
                            user.getY(),
                            user.getZ(),
                            Portalgun.PORTAL2_SHOOT_EVENT,
                            SoundCategory.NEUTRAL,
                            1.0F,
                            1F);

                waitPortal2 = true;

                    ModMain.serverTaskList.addTask(TaskList.withDelay(delay, TaskList.oneShotTask(() -> {

                        PortalMethods.portal2Mtehods(user, hit);

                        if (McHelper.getServer().getThread() == Thread.currentThread()) {
                            portal1 = portalPersistentState.getPortals().get(user.getUuidAsString() + "-portalGunPortal0");
                            portal2 = portalPersistentState.getPortals().get(user.getUuidAsString() + "-portalGunPortal1");
                            if (portal1 != null && portal2 != null && !waitPortal1) {
                                removeOldPortal1(user, portalPersistentState, user.world);
                                removeOldPortal2(user, portalPersistentState, user.world);
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
                            waitPortal2 = false;
                            waitPortal1 = false;
                        }
                    })));



                }
                user.incrementStat(Stats.USED.getOrCreateStat(this));

                portalPersistentState.getPortals().put(user.getUuidAsString() + "-portalGunPortal0", newPortal1);
                portalPersistentState.getPortals().put(user.getUuidAsString() + "-portalGunPortal1", newPortal2);

                savePerstistentState(portalPersistentState);

            }

        return TypedActionResult.success(itemStack, world.isClient());
    }

}
