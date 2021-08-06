package tk.meowmc.portalgun.misc;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import qouteall.imm_ptl.core.McHelper;
import qouteall.imm_ptl.core.portal.GeometryPortalShape;
import qouteall.imm_ptl.core.portal.PortalExtension;
import qouteall.imm_ptl.core.portal.PortalManipulation;
import qouteall.q_misc_util.my_util.DQuaternion;
import tk.meowmc.portalgun.Portalgun;
import tk.meowmc.portalgun.config.PortalGunConfig;
import tk.meowmc.portalgun.entities.CustomPortal;
import tk.meowmc.portalgun.items.PortalGunItem;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PortalMethods {
    public static final int TRIANGLE_NUM = 100; //Number of triangles used to approximate the elliptical shape of the portal
    public static final double TAU = Math.PI * 2; //mathematical name for 2 * PI
    public static final double PORTAL_HEIGHT = 1.9;
    public static final double PORTAL_WIDTH = 0.9;
    public static Vec3i dirUp1; //Portal 1 AxisH
    public static Vec3i dirUp2; //Portal 2 AxisH
    public static Vec3i dirOut1;
    public static Vec3i dirOut2;
    public static Vec3i dirRight1; //Portal 1 AxisW
    public static Vec3i dirRight2; //Portal 2 AxisW
    static Vec3d portal1AxisW;
    static Vec3d portal1AxisH;
    static Vec3d portal2AxisW;
    static Vec3d portal2AxisH;

    public static void makeRoundPortal(CustomPortal portal) {
        GeometryPortalShape shape = new GeometryPortalShape();
        shape.triangles = IntStream.range(0, TRIANGLE_NUM)
                .mapToObj(i -> new GeometryPortalShape.TriangleInPlane(
                        0, 0,
                        portal.width * 0.5 * Math.cos(TAU * ((double) i) / TRIANGLE_NUM),
                        portal.height * 0.5 * Math.sin(TAU * ((double) i) / TRIANGLE_NUM),
                        portal.width * 0.5 * Math.cos(TAU * ((double) i + 1) / TRIANGLE_NUM),
                        portal.height * 0.5 * Math.sin(TAU * ((double) i + 1) / TRIANGLE_NUM)
                )).collect(Collectors.toList());
        portal.specialShape = shape;
        portal.cullableXStart = portal.cullableXEnd = portal.cullableYStart = portal.cullableYEnd = 0;

    }

    public static Vec3d calcPortalPos(BlockPos hit, Vec3i upright, Vec3i facing, Vec3i cross) {
        double upOffset = -0.5, faceOffset = -0.51, crossOffset = 0.0;
        return new Vec3d(
                ((hit.getX() + 0.5) + upOffset * upright.getX() + faceOffset * facing.getX() + crossOffset * cross.getX()), // x component
                ((hit.getY() + 0.5) + upOffset * upright.getY() + faceOffset * facing.getY() + crossOffset * cross.getY()), // y component
                ((hit.getZ() + 0.5) + upOffset * upright.getZ() + faceOffset * facing.getZ() + crossOffset * cross.getZ())  // z component
        );
    }

    public static Vec3d calcOutlinePos(BlockPos hit, Vec3i upright, Vec3i facing, Vec3i cross) {
        double upOffset = -1, faceOffset = -0.5105, crossOffset = 0.0;
        return new Vec3d(
                ((hit.getX() + 0.5) + upOffset * upright.getX() + faceOffset * facing.getX() + crossOffset * cross.getX()), // x component
                ((hit.getY() + 0.5) + upOffset * upright.getY() + faceOffset * facing.getY() + crossOffset * cross.getY()), // y component
                ((hit.getZ() + 0.5) + upOffset * upright.getZ() + faceOffset * facing.getZ() + crossOffset * cross.getZ())  // z component
        );
    }

    public static void Settings1(Direction direction, BlockPos blockPos, HitResult hit, LivingEntity user) {
        PortalGunItem gunItem = (PortalGunItem) Portalgun.PORTALGUN;

        if (gunItem.newPortal1 == null)
            gunItem.newPortal1 = Portalgun.CUSTOM_PORTAL.create(McHelper.getServerWorld(user.world.getRegistryKey()));

        PortalExtension portalExtension = PortalExtension.get(gunItem.newPortal1);
        portalExtension.adjustPositionAfterTeleport = direction == Direction.UP || direction == Direction.DOWN;

        PortalGunConfig config = AutoConfig.getConfigHolder(PortalGunConfig.class).getConfig();

        gunItem.newPortal1.setDestination(gunItem.newPortal2 != null ? gunItem.newPortal2.getPos() : calcPortalPos(blockPos, dirUp1, dirOut1, dirRight1));

        gunItem.newPortal1.dimensionTo = gunItem.newPortal2 != null ? gunItem.newPortal2.world.getRegistryKey() : user.world.getRegistryKey();

        dirOut1 = ((BlockHitResult) hit).getSide().getOpposite().getVector();

        dirUp1 = dirOut1.getY() == 0 ? new Vec3i(0, 1, 0) : user.getHorizontalFacing().getVector();

        dirRight1 = dirUp1.crossProduct(dirOut1);

        gunItem.newPortal1.setOriginPos(calcPortalPos(blockPos, dirUp1, dirOut1, dirRight1));
        gunItem.newPortal1.setOrientationAndSize(
                Vec3d.of(dirRight1), //axisW
                Vec3d.of(dirUp1).multiply(-1), //axisH
                PORTAL_WIDTH, // width
                PORTAL_HEIGHT // height
        );
        if (config.enabled.enableRoundPortals)
            makeRoundPortal(gunItem.newPortal1);
        gunItem.newPortal1.portalTag = "portalgun_portal1";
    }

    public static void Settings2(Direction direction, BlockPos blockPos, HitResult hit, LivingEntity user) {
        PortalGunItem gunItem = (PortalGunItem) Portalgun.PORTALGUN;

        if (gunItem.newPortal2 == null)
            gunItem.newPortal2 = Portalgun.CUSTOM_PORTAL.create(McHelper.getServerWorld(user.world.getRegistryKey()));

        PortalExtension portalExtension = PortalExtension.get(gunItem.newPortal2);
        portalExtension.adjustPositionAfterTeleport = direction == Direction.UP || direction == Direction.DOWN;

        PortalGunConfig config = AutoConfig.getConfigHolder(PortalGunConfig.class).getConfig();

        gunItem.newPortal2.dimensionTo = gunItem.newPortal1 != null ? gunItem.newPortal1.world.getRegistryKey() : user.world.getRegistryKey();

        gunItem.newPortal2.setDestination(gunItem.newPortal1 != null ? gunItem.newPortal1.getPos() : calcPortalPos(blockPos, dirUp2, dirOut2, dirRight2));

        dirOut2 = ((BlockHitResult) hit).getSide().getOpposite().getVector();
        dirUp2 = dirOut2.getY() == 0 ? new Vec3i(0, 1, 0) : user.getHorizontalFacing().getVector();

        dirRight2 = dirUp2.crossProduct(dirOut2);

        gunItem.newPortal2.setOriginPos(calcPortalPos(blockPos, dirUp2, dirOut2, dirRight2));
        gunItem.newPortal2.setOrientationAndSize(
                Vec3d.of(dirRight2), //axisW
                Vec3d.of(dirUp2).multiply(-1), //axisH
                PORTAL_WIDTH, // width
                PORTAL_HEIGHT // height
        );
        if (config.enabled.enableRoundPortals)
            makeRoundPortal(gunItem.newPortal2);
        gunItem.newPortal2.portalTag = "portalgun_portal2";
    }

    public static void portal1Methods(LivingEntity user, HitResult hit, World world) {
        PortalGunItem gunItem = (PortalGunItem) Portalgun.PORTALGUN;

        Direction direction = ((BlockHitResult) hit).getSide();

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos blockPos = blockHit.getBlockPos();

        if (gunItem.newPortal2 != null) {
            portal2AxisW = gunItem.newPortal2.axisW;
            portal2AxisH = gunItem.newPortal2.axisH;
        }

        Settings1(direction, blockPos, hit, user);

        Settings2(direction, blockPos, hit, user);

        if (gunItem.portalOutline1 == null)
            gunItem.portalOutline1 = Portalgun.PORTAL_OVERLAY.create(world);

        Pair<Double, Double> angles = DQuaternion.getPitchYawFromRotation(PortalManipulation.getPortalOrientationQuaternion(Vec3d.of(dirRight1), Vec3d.of(dirUp1)));
        gunItem.portalOutline1.axisH = gunItem.newPortal1.axisH;
        gunItem.portalOutline1.axisW = gunItem.newPortal1.axisW;
        gunItem.portalOutline1.yaw = angles.getLeft().floatValue() + (90 * dirUp1.getX());
        gunItem.portalOutline1.pitch = angles.getRight().floatValue();
        gunItem.portalOutline1.setRoll((angles.getRight().floatValue() + 90) * dirUp1.getX());
        gunItem.portalOutline1.setColor(false);
        gunItem.portalOutline1.noClip = true;
        gunItem.portalOutline1.applyRotation(BlockRotation.CLOCKWISE_180);
        gunItem.newPortal1.setOutline(gunItem.portalOutline1.getUuidAsString());

        gunItem.newPortal2.setOriginPos(gunItem.newPortal1.getDestPos());
        gunItem.newPortal2.setDestination(gunItem.newPortal1.getPos());
        gunItem.newPortal2.axisW = portal2AxisW;
        gunItem.newPortal2.axisH = portal2AxisH;
    }

    public static void portal2Methods(LivingEntity user, HitResult hit, World world) {
        PortalGunItem gunItem = (PortalGunItem) Portalgun.PORTALGUN;

        Direction direction = ((BlockHitResult) hit).getSide();

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos blockPos = blockHit.getBlockPos();
        ServerWorld portal1World = McHelper.getServerWorld(World.OVERWORLD);

        if (gunItem.newPortal1 != null) {
            portal1AxisW = gunItem.newPortal1.axisW;
            portal1AxisH = gunItem.newPortal1.axisH;
        }

        Settings2(direction, blockPos, hit, user);

        Settings1(direction, blockPos, hit, user);

        if (gunItem.portalOutline2 == null)
            gunItem.portalOutline2 = Portalgun.PORTAL_OVERLAY.create(world);

        Pair<Double, Double> angles = DQuaternion.getPitchYawFromRotation(PortalManipulation.getPortalOrientationQuaternion(Vec3d.of(dirRight2), Vec3d.of(dirUp2)));
        gunItem.portalOutline2.axisH = gunItem.newPortal2.axisH;
        gunItem.portalOutline2.axisW = gunItem.newPortal2.axisW;
        gunItem.portalOutline2.yaw = angles.getLeft().floatValue() + (90 * dirUp2.getX());
        gunItem.portalOutline2.pitch = angles.getRight().floatValue();
        gunItem.portalOutline2.setRoll((angles.getRight().floatValue() + 90) * dirUp2.getX());
        gunItem.portalOutline2.setColor(true);
        gunItem.portalOutline2.noClip = true;
        gunItem.newPortal2.setOutline(gunItem.portalOutline2.getUuidAsString());

        gunItem.newPortal1.setOriginPos(gunItem.newPortal2.getDestPos());
        gunItem.newPortal1.setDestination(gunItem.newPortal2.getPos());
        gunItem.newPortal1.moveToWorld(portal1World);
        gunItem.newPortal1.axisW = portal1AxisW;
        gunItem.newPortal1.axisH = portal1AxisH;
    }
}
