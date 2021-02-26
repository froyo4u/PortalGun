package tk.meowmc.portalgun.items;

import com.qouteall.immersive_portals.McHelper;
import com.qouteall.immersive_portals.ModMain;
import com.qouteall.immersive_portals.my_util.DQuaternion;
import com.qouteall.immersive_portals.portal.GeometryPortalShape;
import com.qouteall.immersive_portals.portal.Portal;
import com.qouteall.immersive_portals.portal.PortalExtension;
import com.qouteall.immersive_portals.portal.PortalManipulation;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.Vector3f;
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
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.ejml.data.FixedMatrix3x3_64F;
import tk.meowmc.portalgun.Portalgun;
import tk.meowmc.portalgun.misc.PortalPersistentState;
import tk.meowmc.portalgun.misc.TaskList;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    public static MinecraftClient clientStatic = MinecraftClient.getInstance();
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


    public static Vec3d getDirectionVec(Direction direction) {
        switch (direction) {
            case UP:
                return new Vec3d(0, -1, 0);
            case DOWN:
                return new Vec3d(0, 1, 0);
            case EAST:
                return new Vec3d(-1, 0, 0);
            case WEST:
                return new Vec3d(1, 0, 0);
            case NORTH:
                return new Vec3d(0, 0, -1);
            case SOUTH:
                return new Vec3d(0, 0, 1);
        }
        return null;
    }

    public void setPlaneInformation(HitResult hit){
        planeMatrix = new FixedMatrix3x3_64F();
        planeMatrixInverse = new FixedMatrix3x3_64F();
        planeMatrix.a11 = 0;
        planeMatrix.a22 = 0;
        planeMatrix.a33 = 0;
        planeMatrixInverse.a11 = 0;
        planeMatrixInverse.a22 = 0;
        planeMatrixInverse.a33 = 0;
        direction = ((BlockHitResult) hit).getSide();
        switch(direction){
            case UP:
            case DOWN:
                planeMatrix.a11 = 1;
                planeMatrix.a23 = 1;
                planeMatrixInverse.a11 = 1;
                planeMatrixInverse.a32 = 1;
                break;
            case EAST:
            case WEST:
                planeMatrix.a13 = 1;
                planeMatrix.a22 = 1;
                planeMatrixInverse.a31 = 1;
                planeMatrixInverse.a22 = 1;
                break;
            case NORTH:
            case SOUTH:
                planeMatrix.a11= 1 ;
                planeMatrix.a22= 1 ;
                planeMatrixInverse.a11 = 1;
                planeMatrixInverse.a22 = 1;
                break;
        }
        switch(direction){
            case UP:
                positionCorrectionVec = new Vec3d(0,0.001,0);
                break;
            case DOWN:
                positionCorrectionVec = new Vec3d(0,-0.001,0);
                break;
            case EAST:
                positionCorrectionVec = new Vec3d(0.001,0,0);
                break;
            case WEST:
                positionCorrectionVec = new Vec3d(-0.001,0,0);
                break;
            case NORTH:
                positionCorrectionVec = new Vec3d(0,0,-0.001);
                break;
            case SOUTH:
                positionCorrectionVec = new Vec3d(0,0,0.001);
                break;
        }
    }

    public static Vec3d multiply(FixedMatrix3x3_64F mat, Vec3d vec){
        double x = mat.a11 * vec.x + mat.a12 * vec.y + mat.a13 * vec.z;
        double y = mat.a21 * vec.x + mat.a22 * vec.y + mat.a23 * vec.z;
        double z = mat.a31 * vec.x + mat.a32 * vec.y + mat.a33 * vec.z;
        return  new Vec3d(x, y, z);
    }

    public Portal Settings1(Direction direction, BlockPos blockPos) {

        Portal portal = Portal.entityType.create(McHelper.getServer().getWorld(client.world.getRegistryKey()));
        Vec3d portalPosition = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        Vec3d destPos = new Vec3d(blockPos.getX(), blockPos.getY()+2, blockPos.getZ());

        portal.dimensionTo = client.world.getRegistryKey();
        portal.setDestination(destPos);
        updatePortalRotation(portal, direction);

        double width = 1;
        double height = 2;

        switch (direction){
            case SOUTH:
                portal.updatePosition(portalPosition.x+0.5, portalPosition.y, portalPosition.z+1.001);
                break;
            case NORTH:
                portal.updatePosition(portalPosition.x+0.5, portalPosition.y, portalPosition.z-0.001);
                break;
            case WEST:
                portal.updatePosition(portalPosition.x-0.001, portalPosition.y, portalPosition.z+0.5);
                break;
            case EAST:
                portal.updatePosition(portalPosition.x+1.001, portalPosition.y, portalPosition.z+0.5);
                break;
            case UP:
                portal.updatePosition(portalPosition.x+0.5, portalPosition.y+1.001, portalPosition.z);
                break;
            case DOWN:
                portal.updatePosition(portalPosition.x+0.5, portalPosition.y-0.001, portalPosition.z);
                break;
        }


        Vec3d directionVec = getDirectionVec(direction);
        double scalar = directionVec.x + directionVec.y + directionVec.z;
        Vec3d rightVec = multiply(planeMatrixInverse, new Vec3d(scalar*1,0,0));

        Vec3d axisH = multiply(planeMatrixInverse, new Vec3d(0,1,0));

        portal.axisW = rightVec;
        portal.axisH = axisH;
        portal.width = width;
        portal.height = height;
        makeRoundPortal(portal);
        portal.portalTag = "portalgun_portal1";
        PortalExtension portalExtension = PortalExtension.get(portal);
        portalExtension.adjustPositionAfterTeleport = false;
        return portal;
    }

    public Portal Settings2(Direction direction, BlockPos blockPos) {

        Portal portal = Portal.entityType.create(McHelper.getServer().getWorld(client.world.getRegistryKey()));
        Vec3d portalPosition = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        Vec3d destpos = newPortal1.getPos();

        portal.dimensionTo = newPortal1.world.getRegistryKey();
        portal.setDestination(newPortal1.getPos());
        portal.updatePosition(portalPosition.x, portalPosition.y, portalPosition.z);
        updatePortalRotation(portal, direction);

        double width = 1;
        double height = 2;

        switch (direction){
            case SOUTH:
                portal.updatePosition(portalPosition.x+0.5, portalPosition.y, portalPosition.z+1.001);
                break;
            case NORTH:
                portal.updatePosition(portalPosition.x+0.5, portalPosition.y, portalPosition.z-0.001);
                break;
            case WEST:
                portal.updatePosition(portalPosition.x-0.001, portalPosition.y, portalPosition.z+0.5);
                break;
            case EAST:
                portal.updatePosition(portalPosition.x+1.001, portalPosition.y, portalPosition.z+0.5);
                break;
            case UP:
                portal.updatePosition(portalPosition.x+0.5, portalPosition.y+1.001, portalPosition.z);
                break;
            case DOWN:
                portal.updatePosition(portalPosition.x+0.5, portalPosition.y-0.001, portalPosition.z);
                break;
        }


        Vec3d directionVec = getDirectionVec(direction);
        double scalar = directionVec.x + directionVec.y + directionVec.z;
        Vec3d rightVec = multiply(planeMatrixInverse, new Vec3d(scalar*1,0,0));

        Vec3d axisH = multiply(planeMatrixInverse, new Vec3d(0,1,0));

        portal.axisW = rightVec;
        portal.axisH = axisH;
        portal.width = width;
        portal.height = height;
        makeRoundPortal(portal);
        portal.portalTag = "portalgun_portal2";
        PortalExtension portalExtension = PortalExtension.get(portal);
        portalExtension.adjustPositionAfterTeleport = false;
        return portal;
    }


    public static void updatePortalRotation(Portal portal, Direction direction) {
        switch (direction){
            case WEST:
                portal.rotation = new Quaternion(new Vector3f(0, 1, 0), 270, true);
                break;
            case EAST:
            case NORTH:
            case SOUTH:
                portal.rotation = new Quaternion(new Vector3f(0, 1, 0), 180, true);
                break;
        }
    }

    public static void removeOldPortal1(LivingEntity user, PortalPersistentState persistentState, World world) {
            String key = user.getUuidAsString() + "-portalGunPortal0";
            Portal portal = persistentState.getPortals().get(key);
            if (portal != null) {
                Entity portalEntity = McHelper.getServerWorld(portal.world.getRegistryKey()).getEntity(portal.getUuid());
                if (portalEntity != null) {
                    portalEntity.kill();
                    Portalgun.log(Level.INFO, String.format("Removed %s", portal));
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
                    Portalgun.log(Level.INFO, String.format("Removed %s", portalEntity));
                }
                persistentState.getPortals().remove(key);
                persistentState.markDirty();
            }
    }

    public void savePerstistentState(PortalPersistentState persistentState) {
        persistentState.markDirty();
        McHelper.getServerWorld(client.world.getRegistryKey()).getPersistentStateManager().set(persistentState);
    }

    public static void makeRoundPortal(Portal portal) {
        GeometryPortalShape shape = new GeometryPortalShape();
        final int triangleNum = 30;
        double twoPi = Math.PI * 2;
        shape.triangles = IntStream.range(0, triangleNum)
                .mapToObj(i -> new GeometryPortalShape.TriangleInPlane(
                        0, 0,
                        portal.width * 0.5 * Math.cos(twoPi * ((double) i) / triangleNum),
                        portal.height * 0.5 * Math.sin(twoPi * ((double) i) / triangleNum),
                        portal.width * 0.5 * Math.cos(twoPi * ((double) i + 1) / triangleNum),
                        portal.height * 0.5 * Math.sin(twoPi * ((double) i + 1) / triangleNum)
                )).collect(Collectors.toList());
        portal.specialShape = shape;
        portal.cullableXStart = 0;
        portal.cullableXEnd = 0;
        portal.cullableYStart = 0;
        portal.cullableYEnd = 0;
    }

    public void portal1Spawn(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        user.getItemCooldownManager().set(this, 4);
        Entity entity = this.client.getCameraEntity();
        hit = entity.raycast(50.0D, 0.0F, false);

        if (hit.getType() == BLOCK) {
            Direction direction = ((BlockHitResult) hit).getSide();

            PortalPersistentState portalPersistentState = McHelper.getServerWorld(user.world.getRegistryKey()).getPersistentStateManager().getOrCreate(() -> new PortalPersistentState(KEY), KEY);

            setPlaneInformation(hit);
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
                    newPortal1 = Settings1(direction, blockPos);
                    newPortal1.setDestination(newPortal2.getPos());

                    Vec3d portal2AxisW = newPortal2.axisW;
                    Vec3d portal2AxisH = newPortal2.axisH;


                    newPortal2 = Settings2(direction, blockPos);
                    newPortal2.updatePosition(newPortal1.getDestPos().getX(), newPortal1.getDestPos().getY(), newPortal1.getDestPos().getZ());
                    newPortal2.setDestination(newPortal1.getPos());
                    newPortal2.axisW = portal2AxisW;
                    newPortal2.axisH = portal2AxisH;

                    PortalExtension portal1Extension = PortalExtension.get(newPortal1);
                    PortalExtension portal2Extension = PortalExtension.get(newPortal2);

                    DQuaternion portal2OorentationInverse = PortalManipulation.getPortalOrientationQuaternion(newPortal2.axisW, newPortal2.axisH).getConjugated();
                    DQuaternion orientationCombined1 = PortalManipulation.getPortalOrientationQuaternion(newPortal1.axisW, newPortal1.axisH).hamiltonProduct(portal2OorentationInverse);

                    DQuaternion portal1OorentationInverse = PortalManipulation.getPortalOrientationQuaternion(newPortal1.axisW, newPortal1.axisH).getConjugated();
                    DQuaternion orientationCombined2 = PortalManipulation.getPortalOrientationQuaternion(newPortal2.axisW, newPortal2.axisH).hamiltonProduct(portal1OorentationInverse);

                    Quaternion orientationPortal1 = orientationCombined1.toMcQuaternion();
                    Quaternion orientationPortal2 = orientationCombined2.toMcQuaternion();

                    newPortal1.rotation = orientationPortal1;
                    newPortal2.rotation = orientationPortal2;

                    if (newPortal1.rotation.getW() == 1 && newPortal2.rotation.getW() == 1 || newPortal1.rotation.getW() == 1.8746996965264928E-33d && newPortal2.rotation.getW() == 1.8746996965264928E-33d) {
                        newPortal1.rotation = new Quaternion(orientationPortal1.getX(), 1, orientationPortal1.getZ(), 0);
                        newPortal2.rotation = new Quaternion(orientationPortal2.getX(), 1, orientationPortal2.getZ(), 0);
                    }
                    if (newPortal1.rotation.getW() == 3.0616171314629196E-17d && newPortal2.rotation.getW() == 3.0616171314629196E-17d) {
                        newPortal1.rotation = new Quaternion(orientationPortal1.getX(), 0.7071067690849304f, orientationPortal1.getZ(), 0.7071067690849304f);
                        newPortal2.rotation = new Quaternion(orientationPortal2.getX(), 0.7071067690849304f, orientationPortal2.getZ(), -0.7071067690849304f);
                    }
                    if (newPortal1.rotation.getW() == 0.7071067690849304d && newPortal2.rotation.getW() == -0.7071067690849304d) {
                        newPortal1.rotation = new Quaternion(orientationPortal1.getX(), 0.7071067690849304f, orientationPortal1.getZ(), -0.7071067690849304f);
                        newPortal2.rotation = new Quaternion(orientationPortal2.getX(), 0.7071067690849304f, orientationPortal2.getZ(), 0.7071067690849304f);
                    } else if (newPortal1.rotation.getW() == -0.7071067690849304d && newPortal2.rotation.getW() == 0.7071067690849304d) {
                        newPortal1.rotation = new Quaternion(orientationPortal1.getX(), 0.7071067690849304f, orientationPortal1.getZ(), 0.7071067690849304f);
                        newPortal2.rotation = new Quaternion(orientationPortal2.getX(), 0.7071067690849304f, orientationPortal2.getZ(), -0.7071067690849304f);
                    }
                    if (newPortal1.rotation.getW() == 4.329780301713277E-17d && newPortal2.rotation.getW() == 4.329780301713277E-17d || newPortal1.rotation.getW() == 2.220446049250313E-16d && newPortal2.rotation.getW() == 2.220446049250313E-16d || newPortal1.axisH.z == 1 && newPortal1.axisW.x == 1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1) {
                        newPortal1.rotation = null;
                        newPortal2.rotation = null;
                        newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x, newPortal2.getDestPos().y + 1, newPortal2.getDestPos().z - 1));
                    }
                    if (newPortal2.axisH.y == 1 && newPortal2.axisW.z == 1 && newPortal1.axisH.y == 1 && newPortal1.axisW.x == -1 || newPortal1.axisH.y == 1 && newPortal1.axisW.z == -1 && newPortal2.axisH.y == 1 && newPortal2.axisW.x == -1) {
                        newPortal1.rotation = new Quaternion(orientationPortal1.getX(), 0.7071067690849304f, orientationPortal1.getZ(), -0.7071067690849304f);
                        newPortal2.rotation = new Quaternion(orientationPortal2.getX(), 0.7071067690849304f, orientationPortal2.getZ(), 0.7071067690849304f);
                    } else if (newPortal1.axisH.y == 1 && newPortal1.axisW.z == 1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1) {
                        newPortal1.rotation = new Quaternion(orientationPortal1.getX(), orientationPortal1.getY(), 0.7071067690849304f, 0.7071067690849304f);
                    }

                    if (newPortal1.axisH.y == 1 && newPortal1.axisW.z == 1 && newPortal2.axisH.y == 1 && newPortal2.axisW.x == -1) {
                        newPortal1.rotation = new Quaternion(orientationPortal1.getX(), 0.7071067690849304f, orientationPortal1.getZ(), 0.7071067690849304f);
                        newPortal2.rotation = new Quaternion(orientationPortal2.getX(), 0.7071067690849304f, orientationPortal2.getZ(), -0.7071067690849304f);
                    }
                    if (newPortal2.axisH.y == 1 && newPortal2.axisW.z == -1 && newPortal1.axisH.y == 1 && newPortal1.axisW.x == -1) {
                        newPortal1.rotation = new Quaternion(orientationPortal1.getX(), 0.7071067690849304f, orientationPortal1.getZ(), 0.7071067690849304f);
                        newPortal2.rotation = new Quaternion(orientationPortal2.getX(), 0.7071067690849304f, orientationPortal2.getZ(), -0.7071067690849304f);
                    }


                    switch (direction) {
                        case WEST:
                            newPortal2.setDestination(new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ() + 0.5));
                            break;
                        case EAST:
                            newPortal2.setDestination(new Vec3d(blockPos.getX() + 1, blockPos.getY(), blockPos.getZ() + 0.5));
                            break;
                        case SOUTH:
                        case UP:
                            newPortal2.setDestination(new Vec3d(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 1));
                            break;
                        case DOWN:
                        case NORTH:
                            newPortal2.setDestination(new Vec3d(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ()));
                            break;
                    }
                    if (newPortal1.axisH.z == 1 && newPortal1.axisW.x == -1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == 1) {
                        newPortal1.rotation = null;
                        newPortal2.rotation = null;
                        newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x, newPortal2.getDestPos().y + 1, newPortal2.getDestPos().z - 1));
                    }
                    if (newPortal1.axisH.y == 1 && newPortal1.axisW.z == 1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1) {
                        newPortal1.rotation = new Quaternion(0.5f, 0.5f, -0.5f, -0.5f);
                        newPortal2.rotation = new Quaternion(0.5f, 0.5f, -0.5f, 0.5f);
                        portal1Extension.adjustPositionAfterTeleport = true;
                        portal2Extension.adjustPositionAfterTeleport = true;
                    }
                    if (newPortal2.axisH.y == 1 && newPortal2.axisW.z == 1 && newPortal1.axisH.z == 1 && newPortal1.axisW.x == -1) {
                        newPortal2.rotation = new Quaternion(0.5f, 0.5f, -0.5f, -0.5f);
                        newPortal1.rotation = new Quaternion(0.5f, 0.5f, -0.5f, 0.5f);
                        portal1Extension.adjustPositionAfterTeleport = true;
                        portal2Extension.adjustPositionAfterTeleport = true;
                        newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x, newPortal2.getDestPos().y + 1.001, newPortal2.getDestPos().z - 0.999));
                    }
                    if (newPortal1.axisH.z == 1 && newPortal1.axisW.x == -1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1) {
                        newPortal1.rotation = new Quaternion(0, 0, 1, 0);
                        newPortal2.rotation = new Quaternion(0, 0, 1, 0);
                        newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x, newPortal2.getDestPos().y + 1, newPortal2.getDestPos().z - 1));
                    }
                    if (newPortal1.axisH.z == 1 && newPortal1.axisW.x == 1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == 1) {
                        newPortal1.rotation = new Quaternion(0, 0, 1, 0);
                        newPortal2.rotation = new Quaternion(0, 0, 1, 0);
                    }
                    if (newPortal1.axisH.z == 1 && newPortal1.axisW.x == -1 && newPortal2.axisH.y == 1 && newPortal2.axisW.x == 1) {
                        newPortal1.rotation = new Quaternion(-0.7071067690849304f, 0, 0, 0.7071067690849304f);
                        newPortal2.rotation = new Quaternion(0, 0.7071067094802856f, -0.7071067094802856f, 0);
                        newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x, newPortal2.getDestPos().y + 1.001, newPortal2.getDestPos().z - 0.999));
                        portal1Extension.adjustPositionAfterTeleport = true;
                        portal2Extension.adjustPositionAfterTeleport = true;
                    }
                    if (newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1 && newPortal1.axisH.y == 1 && newPortal1.axisW.x == 1) {
                        newPortal2.rotation = new Quaternion(-0.7071067690849304f, 0, 0, 0.7071067690849304f);
                        newPortal1.rotation = new Quaternion(0, 0.7071067094802856f, -0.7071067094802856f, 0);
                        newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x, newPortal2.getDestPos().y, newPortal2.getDestPos().z + 0.001));
                        portal1Extension.adjustPositionAfterTeleport = true;
                        portal2Extension.adjustPositionAfterTeleport = true;
                    }

                    if (newPortal1.axisH.z == 1 && newPortal1.axisW.x == -1 && newPortal2.axisH.y == 1 && newPortal2.axisW.x == -1) {
                        newPortal1.rotation = new Quaternion(-0.7071067690849304f, 0, 0, -0.7071067690849304f);
                        newPortal2.rotation = new Quaternion(0, 0.7071067094802856f, 0.7071067094802856f, 0);
                        newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x, newPortal2.getDestPos().y + 1.001, newPortal2.getDestPos().z));
                        portal1Extension.adjustPositionAfterTeleport = true;
                        portal2Extension.adjustPositionAfterTeleport = true;
                    }
                    if (newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1 && newPortal1.axisH.y == 1 && newPortal1.axisW.x == -1) {
                        newPortal2.rotation = new Quaternion(-0.7071067690849304f, 0, 0, -0.7071067690849304f);
                        newPortal1.rotation = new Quaternion(0, 0.7071067094802856f, 0.7071067094802856f, 0);
                        newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x, newPortal2.getDestPos().y, newPortal2.getDestPos().z - 0.001));
                        portal1Extension.adjustPositionAfterTeleport = true;
                        portal2Extension.adjustPositionAfterTeleport = true;
                    }

                    if (newPortal1.axisH.y == 1 && newPortal1.axisW.z == -1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1) {
                        newPortal1.rotation = new Quaternion(0.5f, -0.5f, -0.5f, 0.5f);
                        newPortal2.rotation = new Quaternion(0.5f, -0.5f, -0.5f, -0.5f);
                        portal1Extension.adjustPositionAfterTeleport = true;
                        portal2Extension.adjustPositionAfterTeleport = true;
                        newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x + 0.001, newPortal2.getDestPos().y, newPortal2.getDestPos().z));
                    }
                    if (newPortal2.axisH.y == 1 && newPortal2.axisW.z == -1 && newPortal1.axisH.z == 1 && newPortal1.axisW.x == -1) {
                        newPortal2.rotation = new Quaternion(0.5f, -0.5f, -0.5f, 0.5f);
                        newPortal1.rotation = new Quaternion(0.5f, -0.5f, -0.5f, -0.5f);
                        portal1Extension.adjustPositionAfterTeleport = true;
                        portal2Extension.adjustPositionAfterTeleport = true;
                        newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x, newPortal2.getDestPos().y + 1.002, newPortal2.getDestPos().z - 0.999));
                    }


                    if (newPortal1.axisH.y == 1 && newPortal1.axisW.z == 1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == 1) {
                        newPortal1.rotation = new Quaternion(-0.5f, 0.5f, 0.5f, -0.5f);
                        newPortal2.rotation = new Quaternion(-0.5f, 0.5f, 0.5f, 0.5f);
                        portal1Extension.adjustPositionAfterTeleport = false;
                        portal2Extension.adjustPositionAfterTeleport = true;
                        newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x - 0.001, newPortal2.getDestPos().y, newPortal2.getDestPos().z));
                    }
                    if (newPortal2.axisH.y == 1 && newPortal2.axisW.z == 1 && newPortal1.axisH.z == 1 && newPortal1.axisW.x == 1) {
                        newPortal2.rotation = new Quaternion(-0.5f, 0.5f, 0.5f, -0.5f);
                        newPortal1.rotation = new Quaternion(-0.5f, 0.5f, 0.5f, 0.5f);
                        portal2Extension.adjustPositionAfterTeleport = false;
                        portal1Extension.adjustPositionAfterTeleport = true;
                        newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x - 0.001, newPortal2.getDestPos().y - 0.001, newPortal2.getDestPos().z));
                    }


                    if (newPortal1.axisH.z == 1 && newPortal1.axisW.x == 1 && newPortal2.axisH.y == 1 && newPortal2.axisW.x == -1) {
                        newPortal1.rotation = new Quaternion(0.7071067690849304f, 0, 0, -0.7071067690849304f);
                        newPortal2.rotation = new Quaternion(-0.7071067094802856f, 0, 0, -0.7071067094802856f);
                        newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x, newPortal2.getDestPos().y - 0.001, newPortal2.getDestPos().z));
                        portal1Extension.adjustPositionAfterTeleport = true;
                        portal2Extension.adjustPositionAfterTeleport = false;
                    }
                    if (newPortal2.axisH.z == 1 && newPortal2.axisW.x == 1 && newPortal1.axisH.y == 1 && newPortal1.axisW.x == -1) {
                        newPortal2.rotation = new Quaternion(0.7071067690849304f, 0, 0, -0.7071067690849304f);
                        newPortal1.rotation = new Quaternion(-0.7071067094802856f, 0, 0, -0.7071067094802856f);
                        newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x, newPortal2.getDestPos().y + -0.001, newPortal2.getDestPos().z));
                        portal2Extension.adjustPositionAfterTeleport = true;
                        portal1Extension.adjustPositionAfterTeleport = false;
                    }

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

            setPlaneInformation(hit);
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
                    Vec3d portal1AxisW = newPortal1.axisW;
                    Vec3d portal1AxisH = newPortal1.axisH;
                    newPortal2 = Settings2(direction, blockPos);

                    newPortal1 = Settings1(direction, blockPos);
                    newPortal1.updatePosition(newPortal2.getDestPos().getX(), newPortal2.getDestPos().getY(), newPortal2.getDestPos().getZ());
                    newPortal1.setDestination(new Vec3d(newPortal2.getX(), newPortal2.getY(), newPortal2.getZ()));
                    newPortal1.axisW = portal1AxisW;
                    newPortal1.axisH = portal1AxisH;

                        PortalExtension portal1Extension = PortalExtension.get(newPortal1);
                        PortalExtension portal2Extension = PortalExtension.get(newPortal2);

                        DQuaternion portal2OorentationInverse = PortalManipulation.getPortalOrientationQuaternion(newPortal2.axisW, newPortal2.axisH).getConjugated();
                        DQuaternion orientationCombined1 = PortalManipulation.getPortalOrientationQuaternion(newPortal1.axisW, newPortal1.axisH).hamiltonProduct(portal2OorentationInverse);

                        DQuaternion portal1OorentationInverse = PortalManipulation.getPortalOrientationQuaternion(newPortal1.axisW, newPortal1.axisH).getConjugated();
                        DQuaternion orientationCombined2 = PortalManipulation.getPortalOrientationQuaternion(newPortal2.axisW, newPortal2.axisH).hamiltonProduct(portal1OorentationInverse);

                        Quaternion orientationPortal1 = orientationCombined1.toMcQuaternion();
                        Quaternion orientationPortal2 = orientationCombined2.toMcQuaternion();

                        newPortal1.rotation = orientationPortal1;
                        newPortal2.rotation = orientationPortal2;

                        if (newPortal1.rotation.getW() == 1 && newPortal2.rotation.getW() == 1 || newPortal1.rotation.getW() == 1.8746996965264928E-33d && newPortal2.rotation.getW() == 1.8746996965264928E-33d){
                            newPortal1.rotation = new Quaternion(orientationPortal1.getX(), 1, orientationPortal1.getZ(), 0);
                            newPortal2.rotation = new Quaternion(orientationPortal2.getX(), 1, orientationPortal2.getZ(), 0);
                        }
                        if (newPortal1.rotation.getW() == 3.0616171314629196E-17d && newPortal2.rotation.getW() == 3.0616171314629196E-17d){
                            newPortal1.rotation = new Quaternion(orientationPortal1.getX(), 0.7071067690849304f, orientationPortal1.getZ(), 0.7071067690849304f);
                            newPortal2.rotation = new Quaternion(orientationPortal2.getX(), 0.7071067690849304f, orientationPortal2.getZ(), -0.7071067690849304f);
                        }
                        if (newPortal1.rotation.getW() == 0.7071067690849304d && newPortal2.rotation.getW() == -0.7071067690849304d) {
                            newPortal1.rotation = new Quaternion(orientationPortal1.getX(), 0.7071067690849304f, orientationPortal1.getZ(), -0.7071067690849304f);
                            newPortal2.rotation = new Quaternion(orientationPortal2.getX(), 0.7071067690849304f, orientationPortal2.getZ(), 0.7071067690849304f);
                        } else if (newPortal1.rotation.getW() == -0.7071067690849304d && newPortal2.rotation.getW() == 0.7071067690849304d) {
                            newPortal1.rotation = new Quaternion(orientationPortal1.getX(), 0.7071067690849304f, orientationPortal1.getZ(), 0.7071067690849304f);
                            newPortal2.rotation = new Quaternion(orientationPortal2.getX(), 0.7071067690849304f, orientationPortal2.getZ(), -0.7071067690849304f);
                        }
                        if (newPortal1.rotation.getW() == 4.329780301713277E-17d && newPortal2.rotation.getW() == 4.329780301713277E-17d || newPortal1.rotation.getW() == 2.220446049250313E-16d && newPortal2.rotation.getW() == 2.220446049250313E-16d || newPortal1.axisH.z == 1 && newPortal1.axisW.x == 1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1) {
                            newPortal1.rotation = null;
                            newPortal2.rotation = null;
                        }
                        if (newPortal2.axisH.y == 1 && newPortal2.axisW.z == 1 && newPortal1.axisH.y == 1 && newPortal1.axisW.x == -1 || newPortal1.axisH.y == 1 && newPortal1.axisW.z == -1 && newPortal2.axisH.y == 1 && newPortal2.axisW.x == -1 )
                        {
                            newPortal1.rotation = new Quaternion(orientationPortal1.getX(), 0.7071067690849304f, orientationPortal1.getZ(), -0.7071067690849304f);
                            newPortal2.rotation = new Quaternion(orientationPortal2.getX(), 0.7071067690849304f, orientationPortal2.getZ(), 0.7071067690849304f);
                        }
                        if (newPortal1.axisH.y == 1 && newPortal1.axisW.z == 1 && newPortal2.axisH.y == 1 && newPortal2.axisW.x == -1)
                        {
                            newPortal1.rotation = new Quaternion(orientationPortal1.getX(), 0.7071067690849304f, orientationPortal1.getZ(), 0.7071067690849304f);
                            newPortal2.rotation = new Quaternion(orientationPortal2.getX(), 0.7071067690849304f, orientationPortal2.getZ(), -0.7071067690849304f);
                        }
                        if (newPortal2.axisH.y == 1 && newPortal2.axisW.z == -1 && newPortal1.axisH.y == 1 && newPortal1.axisW.x == -1)
                        {
                            newPortal1.rotation = new Quaternion(orientationPortal1.getX(), 0.7071067690849304f, orientationPortal1.getZ(), 0.7071067690849304f);
                            newPortal2.rotation = new Quaternion(orientationPortal2.getX(), 0.7071067690849304f, orientationPortal2.getZ(), -0.7071067690849304f);
                        }

                        if (newPortal1.axisH.y == 1 && newPortal1.axisW.z == 1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1)
                        {
                            newPortal1.rotation = new Quaternion(orientationPortal1.getX(), orientationPortal1.getY(), 0.7071067690849304f, 0.7071067690849304f);
                        }


                            switch (direction) {
                                case WEST:
                                case EAST:
                                case SOUTH:
                                case NORTH:
                                case DOWN:
                                case UP:
                                    newPortal1.setDestination(new Vec3d(newPortal2.getX(), newPortal2.getY(), newPortal2.getZ()));
                                    break;
                            }

                        if (newPortal1.axisH.z == 1 && newPortal1.axisW.x == -1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == 1) {
                            newPortal1.rotation = null;
                            newPortal2.rotation = null;
                        }
                        if (newPortal1.axisH.y == 1 && newPortal1.axisW.z == 1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1) {
                            newPortal1.rotation = new Quaternion(0.5f, 0.5f, -0.5f, -0.5f);
                            newPortal2.rotation = new Quaternion(0.5f, 0.5f, -0.5f, 0.5f);
                            portal1Extension.adjustPositionAfterTeleport = true;
                            portal2Extension.adjustPositionAfterTeleport = true;
                        }
                        if (newPortal2.axisH.y == 1 && newPortal2.axisW.z == 1 && newPortal1.axisH.z == 1 && newPortal1.axisW.x == -1) {
                            newPortal2.rotation = new Quaternion(0.5f, 0.5f, -0.5f, -0.5f);
                            newPortal1.rotation = new Quaternion(0.5f, 0.5f, -0.5f, 0.5f);
                            portal1Extension.adjustPositionAfterTeleport = true;
                            portal2Extension.adjustPositionAfterTeleport = true;
                        }
                        if (newPortal1.axisH.z == 1 && newPortal1.axisW.x == -1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1) {
                            newPortal1.rotation = new Quaternion(0, 0, 1, 0);
                            newPortal2.rotation = new Quaternion(0, 0, 1, 0);
                        }
                        if (newPortal1.axisH.z == 1 && newPortal1.axisW.x == 1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == 1) {
                            newPortal1.rotation = new Quaternion(0, 0, 1, 0);
                            newPortal2.rotation = new Quaternion(0, 0, 1, 0);
                        }
                        if (newPortal1.axisH.z == 1 && newPortal1.axisW.x == -1 && newPortal2.axisH.y == 1 && newPortal2.axisW.x == 1) {
                            newPortal1.rotation = new Quaternion(-0.7071067690849304f, 0, 0, 0.7071067690849304f);
                            newPortal2.rotation = new Quaternion(0, 0.7071067094802856f, -0.7071067094802856f, 0);
                            portal1Extension.adjustPositionAfterTeleport = true;
                            portal2Extension.adjustPositionAfterTeleport = true;
                        }
                        if (newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1 && newPortal1.axisH.y == 1 && newPortal1.axisW.x == 1) {
                            newPortal2.rotation = new Quaternion(-0.7071067690849304f, 0, 0, 0.7071067690849304f);
                            newPortal1.rotation = new Quaternion(0, 0.7071067094802856f, -0.7071067094802856f, 0);
                            portal1Extension.adjustPositionAfterTeleport = true;
                            portal2Extension.adjustPositionAfterTeleport = true;
                        }

                        if (newPortal1.axisH.z == 1 && newPortal1.axisW.x == -1 && newPortal2.axisH.y == 1 && newPortal2.axisW.x == -1) {
                            newPortal1.rotation = new Quaternion(-0.7071067690849304f, 0, 0, -0.7071067690849304f);
                            newPortal2.rotation = new Quaternion(0, 0.7071067094802856f, 0.7071067094802856f, 0);
                            portal1Extension.adjustPositionAfterTeleport = true;
                            portal2Extension.adjustPositionAfterTeleport = true;
                        }
                        if (newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1 && newPortal1.axisH.y == 1 && newPortal1.axisW.x == -1) {
                            newPortal2.rotation = new Quaternion(-0.7071067690849304f, 0, 0, -0.7071067690849304f);
                            newPortal1.rotation = new Quaternion(0, 0.7071067094802856f, 0.7071067094802856f, 0);
                            portal1Extension.adjustPositionAfterTeleport = true;
                            portal2Extension.adjustPositionAfterTeleport = true;
                        }

                        if (newPortal1.axisH.y == 1 && newPortal1.axisW.z == -1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1) {
                            newPortal1.rotation = new Quaternion(0.5f, -0.5f, -0.5f, 0.5f);
                            newPortal2.rotation = new Quaternion(0.5f, -0.5f, -0.5f, -0.5f);
                            portal1Extension.adjustPositionAfterTeleport = true;
                            portal2Extension.adjustPositionAfterTeleport = true;
                        }
                        if (newPortal2.axisH.y == 1 && newPortal2.axisW.z == -1 && newPortal1.axisH.z == 1 && newPortal1.axisW.x == -1) {
                            newPortal2.rotation = new Quaternion(0.5f, -0.5f, -0.5f, 0.5f);
                            newPortal1.rotation = new Quaternion(0.5f, -0.5f, -0.5f, -0.5f);
                            portal1Extension.adjustPositionAfterTeleport = true;
                            portal2Extension.adjustPositionAfterTeleport = true;
                        }


                        if (newPortal1.axisH.y == 1 && newPortal1.axisW.z == 1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == 1) {
                            newPortal1.rotation = new Quaternion(-0.5f, 0.5f, 0.5f, -0.5f);
                            newPortal2.rotation = new Quaternion(-0.5f, 0.5f, 0.5f, 0.5f);
                            portal1Extension.adjustPositionAfterTeleport = false;
                            portal2Extension.adjustPositionAfterTeleport = true;
                        }
                        if (newPortal2.axisH.y == 1 && newPortal2.axisW.z == 1 && newPortal1.axisH.z == 1 && newPortal1.axisW.x == 1) {
                            newPortal2.rotation = new Quaternion(-0.5f, 0.5f, 0.5f, -0.5f);
                            newPortal1.rotation = new Quaternion(-0.5f, 0.5f, 0.5f, 0.5f);
                            portal2Extension.adjustPositionAfterTeleport = false;
                            portal1Extension.adjustPositionAfterTeleport = true;
                        }


                        if (newPortal1.axisH.z == 1 && newPortal1.axisW.x == 1 && newPortal2.axisH.y == 1 && newPortal2.axisW.x == -1) {
                            newPortal1.rotation = new Quaternion(0.7071067690849304f, 0, 0, -0.7071067690849304f);
                            newPortal2.rotation = new Quaternion(-0.7071067094802856f, 0, 0, -0.7071067094802856f);
                            portal1Extension.adjustPositionAfterTeleport = true;
                            portal2Extension.adjustPositionAfterTeleport = false;
                        }
                        if (newPortal2.axisH.z == 1 && newPortal2.axisW.x == 1 && newPortal1.axisH.y == 1 && newPortal1.axisW.x == -1) {
                            newPortal2.rotation = new Quaternion(0.7071067690849304f, 0, 0, -0.7071067690849304f);
                            newPortal1.rotation = new Quaternion(-0.7071067094802856f, 0, 0, -0.7071067094802856f);
                            portal2Extension.adjustPositionAfterTeleport = true;
                            portal1Extension.adjustPositionAfterTeleport = false;
                        }


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
