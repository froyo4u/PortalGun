package tk.meowmc.portalgun.misc;

import qouteall.imm_ptl.core.portal.GeometryPortalShape;
import tk.meowmc.portalgun.entities.CustomPortal;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PortalMethods {
    public static final int TRIANGLE_NUM = 100; //Number of triangles used to approximate the elliptical shape of the portal
    public static final double TAU = Math.PI * 2; //mathematical name for 2 * PI
//    public static final double PORTAL_HEIGHT = 1.9;
//    public static final double PORTAL_WIDTH = 0.9;
//    public static Vec3i dirUp1; //Portal 1 AxisH
//    public static Vec3i dirUp2; //Portal 2 AxisH
//    public static Vec3i dirOut1;
//    public static Vec3i dirOut2;
//    public static Vec3i dirRight1; //Portal 1 AxisW
//    public static Vec3i dirRight2; //Portal 2 AxisW
//    static Vec3 portal1AxisW;
//    static Vec3 portal1AxisH;
//    static Vec3 portal2AxisW;
//    static Vec3 portal2AxisH;

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
//
//    public static Vec3 calcPortalPos(BlockPos hit, Vec3i upright, Vec3i facing, Vec3i cross) {
//        double upOffset = -0.5, faceOffset = -0.51, crossOffset = 0.0;
//        return new Vec3(
//                ((hit.getX() + 0.5) + upOffset * upright.getX() + faceOffset * facing.getX() + crossOffset * cross.getX()), // x component
//                ((hit.getY() + 0.5) + upOffset * upright.getY() + faceOffset * facing.getY() + crossOffset * cross.getY()), // y component
//                ((hit.getZ() + 0.5) + upOffset * upright.getZ() + faceOffset * facing.getZ() + crossOffset * cross.getZ())  // z component
//        );
//    }
//
//    public static Vec3 calcOutlinePos(BlockPos hit, Vec3i upright, Vec3i facing, Vec3i cross) {
//        double upOffset = -1, faceOffset = -0.5105, crossOffset = 0.0;
//        return new Vec3(
//                ((hit.getX() + 0.5) + upOffset * upright.getX() + faceOffset * facing.getX() + crossOffset * cross.getX()), // x component
//                ((hit.getY() + 0.5) + upOffset * upright.getY() + faceOffset * facing.getY() + crossOffset * cross.getY()), // y component
//                ((hit.getZ() + 0.5) + upOffset * upright.getZ() + faceOffset * facing.getZ() + crossOffset * cross.getZ())  // z component
//        );
//    }
//
//    public static void Settings1(Direction direction, BlockPos blockPos, HitResult hit, LivingEntity user) {
//        PortalGunItem gunItem = (PortalGunItem) Portalgun.PORTALGUN;
//
//        if (gunItem.newPortal1 == null)
//            gunItem.newPortal1 = Portalgun.CUSTOM_PORTAL.create(McHelper.getServerWorld(user.level.dimension()));
//
//        PortalExtension portalExtension = PortalExtension.get(gunItem.newPortal1);
//        portalExtension.adjustPositionAfterTeleport = direction == Direction.UP || direction == Direction.DOWN;
//
//        PortalGunConfig config = AutoConfig.getConfigHolder(PortalGunConfig.class).getConfig();
//
//        gunItem.newPortal1.setDestination(PortalGunItem.portal2Exists && gunItem.newPortal2 != null ? gunItem.newPortal2.position() : calcPortalPos(blockPos, dirUp1, dirOut1, dirRight1));
//
//        gunItem.newPortal1.dimensionTo = PortalGunItem.portal2Exists && gunItem.newPortal2 != null ? gunItem.newPortal2.level.dimension() : user.level.dimension();
//
//        dirOut1 = ((BlockHitResult) hit).getDirection().getOpposite().getNormal();
//
//        dirUp1 = dirOut1.getY() == 0 ? new Vec3i(0, 1, 0) : user.getDirection().getNormal();
//
//        dirRight1 = dirUp1.cross(dirOut1);
//
//        gunItem.newPortal1.setOriginPos(calcPortalPos(blockPos, dirUp1, dirOut1, dirRight1));
//        gunItem.newPortal1.setOrientationAndSize(
//                Vec3.atLowerCornerOf(dirRight1), //axisW
//                Vec3.atLowerCornerOf(dirUp1).scale(-1), //axisH
//                PORTAL_WIDTH, // width
//                PORTAL_HEIGHT // height
//        );
//        makeRoundPortal(gunItem.newPortal1);
//        gunItem.newPortal1.portalTag = "portalgun_portal1";
//    }
//
//    public static void Settings2(Direction direction, BlockPos blockPos, HitResult hit, LivingEntity user) {
//        PortalGunItem gunItem = (PortalGunItem) Portalgun.PORTALGUN;
//
//        if (gunItem.newPortal2 == null)
//            gunItem.newPortal2 = Portalgun.CUSTOM_PORTAL.create(McHelper.getServerWorld(user.level.dimension()));
//
//        PortalExtension portalExtension = PortalExtension.get(gunItem.newPortal2);
//        portalExtension.adjustPositionAfterTeleport = direction == Direction.UP || direction == Direction.DOWN;
//
//        PortalGunConfig config = AutoConfig.getConfigHolder(PortalGunConfig.class).getConfig();
//
//        gunItem.newPortal2.dimensionTo = PortalGunItem.portal1Exists && gunItem.newPortal1 != null ? gunItem.newPortal1.level.dimension() : user.level.dimension();
//
//        gunItem.newPortal2.setDestination(PortalGunItem.portal1Exists && gunItem.newPortal1 != null ? gunItem.newPortal1.position() : calcPortalPos(blockPos, dirUp2, dirOut2, dirRight2));
//
//        dirOut2 = ((BlockHitResult) hit).getDirection().getOpposite().getNormal();
//        dirUp2 = dirOut2.getY() == 0 ? new Vec3i(0, 1, 0) : user.getDirection().getNormal();
//
//        dirRight2 = dirUp2.cross(dirOut2);
//
//        gunItem.newPortal2.setOriginPos(calcPortalPos(blockPos, dirUp2, dirOut2, dirRight2));
//        gunItem.newPortal2.setOrientationAndSize(
//                Vec3.atLowerCornerOf(dirRight2), //axisW
//                Vec3.atLowerCornerOf(dirUp2).scale(-1), //axisH
//                PORTAL_WIDTH, // width
//                PORTAL_HEIGHT // height
//        );
//        makeRoundPortal(gunItem.newPortal2);
//        gunItem.newPortal2.portalTag = "portalgun_portal2";
//    }
//
//    public static void portal1Methods(LivingEntity user, HitResult hit, Level world) {
//        PortalGunItem gunItem = (PortalGunItem) Portalgun.PORTALGUN;
//
//        Direction direction = ((BlockHitResult) hit).getDirection();
//
//        BlockHitResult blockHit = (BlockHitResult) hit;
//        BlockPos blockPos = blockHit.getBlockPos();
//
//        if (gunItem.newPortal2 != null) {
//            portal2AxisW = gunItem.newPortal2.axisW;
//            portal2AxisH = gunItem.newPortal2.axisH;
//        }
//
//        Settings1(direction, blockPos, hit, user);
//
//        Settings2(direction, blockPos, hit, user);
//
//        if (gunItem.portalOutline1 == null)
//            gunItem.portalOutline1 = Portalgun.PORTAL_OVERLAY.create(world);
//
//        Tuple<Double, Double> angles = DQuaternion.getPitchYawFromRotation(PortalManipulation.getPortalOrientationQuaternion(Vec3.atLowerCornerOf(dirRight1), Vec3.atLowerCornerOf(dirUp1)));
//        gunItem.portalOutline1.axisH = gunItem.newPortal1.axisH;
//        gunItem.portalOutline1.axisW = gunItem.newPortal1.axisW;
//        gunItem.portalOutline1.yRot = angles.getA().floatValue() + (90 * dirUp1.getX());
//        gunItem.portalOutline1.xRot = angles.getB().floatValue();
//        gunItem.portalOutline1.setRoll((angles.getB().floatValue() + 90) * dirUp1.getX());
//        gunItem.portalOutline1.setColor(false);
//        gunItem.portalOutline1.noPhysics = true;
//        gunItem.portalOutline1.rotate(Rotation.CLOCKWISE_180);
//        gunItem.newPortal1.setOutline(gunItem.portalOutline1.getStringUUID());
//
//        gunItem.newPortal2.setOriginPos(gunItem.newPortal1.getDestPos());
//        gunItem.newPortal2.setDestination(gunItem.newPortal1.position());
//        gunItem.newPortal2.axisW = portal2AxisW;
//        gunItem.newPortal2.axisH = portal2AxisH;
//    }
//
//    public static void portal2Methods(LivingEntity user, HitResult hit, Level world) {
//        PortalGunItem gunItem = (PortalGunItem) Portalgun.PORTALGUN;
//
//        Direction direction = ((BlockHitResult) hit).getDirection();
//
//        BlockHitResult blockHit = (BlockHitResult) hit;
//        BlockPos blockPos = blockHit.getBlockPos();
//        ServerLevel portal1World = McHelper.getServerWorld(Level.OVERWORLD);
//
//        if (gunItem.newPortal1 != null) {
//            portal1AxisW = gunItem.newPortal1.axisW;
//            portal1AxisH = gunItem.newPortal1.axisH;
//        }
//
//        Settings2(direction, blockPos, hit, user);
//
//        Settings1(direction, blockPos, hit, user);
//
//        if (gunItem.portalOutline2 == null)
//            gunItem.portalOutline2 = Portalgun.PORTAL_OVERLAY.create(world);
//
//        Tuple<Double, Double> angles = DQuaternion.getPitchYawFromRotation(PortalManipulation.getPortalOrientationQuaternion(Vec3.atLowerCornerOf(dirRight2), Vec3.atLowerCornerOf(dirUp2)));
//        gunItem.portalOutline2.axisH = gunItem.newPortal2.axisH;
//        gunItem.portalOutline2.axisW = gunItem.newPortal2.axisW;
//        gunItem.portalOutline2.yRot = angles.getA().floatValue() + (90 * dirUp2.getX());
//        gunItem.portalOutline2.xRot = angles.getB().floatValue();
//        gunItem.portalOutline2.setRoll((angles.getB().floatValue() + 90) * dirUp2.getX());
//        gunItem.portalOutline2.setColor(true);
//        gunItem.portalOutline2.noPhysics = true;
//        gunItem.newPortal2.setOutline(gunItem.portalOutline2.getStringUUID());
//
//        gunItem.newPortal1.setOriginPos(gunItem.newPortal2.getDestPos());
//        gunItem.newPortal1.setDestination(gunItem.newPortal2.position());
//        gunItem.newPortal1.changeDimension(portal1World);
//        gunItem.newPortal1.axisW = portal1AxisW;
//        gunItem.newPortal1.axisH = portal1AxisH;
//    }
}
