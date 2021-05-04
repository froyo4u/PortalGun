package tk.meowmc.portalgun.misc;

import com.qouteall.immersive_portals.McHelper;
import com.qouteall.immersive_portals.portal.GeometryPortalShape;
import com.qouteall.immersive_portals.portal.PortalExtension;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import tk.meowmc.portalgun.Portalgun;
import tk.meowmc.portalgun.config.PortalGunConfig;
import tk.meowmc.portalgun.entities.CustomPortal;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static tk.meowmc.portalgun.items.PortalGunItem.*;

public class PortalMethods {
    public static final int TRIANGLE_NUM = 30; //Number of triangles used to approximate the elliptical shape of the newPortal1
    public static final double TAU = Math.PI * 2; //mathematical name for 2 * PI
    public static final int PORTAL_HEIGHT = 2;
    public static final int PORTAL_WIDTH = 1;
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
        double upOffset = -0.5, faceOffset = -0.505, crossOffset = 0.0;
        return new Vec3d(
                ((hit.getX() + 0.5) + upOffset * upright.getX() + faceOffset * facing.getX() + crossOffset * cross.getX()), // x component
                ((hit.getY() + 0.5) + upOffset * upright.getY() + faceOffset * facing.getY() + crossOffset * cross.getY()), // y component
                ((hit.getZ() + 0.5) + upOffset * upright.getZ() + faceOffset * facing.getZ() + crossOffset * cross.getZ())  // z component
        );
    }

    public static void Settings1(Direction direction, BlockPos blockPos, HitResult hit, LivingEntity user) {
        if (newPortal1 == null)
            newPortal1 = Portalgun.CUSTOM_PORTAL.create(McHelper.getServer().getWorld(user.world.getRegistryKey()));

        PortalExtension portalExtension = PortalExtension.get(newPortal1);
        portalExtension.adjustPositionAfterTeleport = direction == Direction.UP || direction == Direction.DOWN;

        PortalGunConfig config = AutoConfig.getConfigHolder(PortalGunConfig.class).getConfig();

        newPortal1.setDestination(newPortal2 != null ? newPortal2.getPos() : calcPortalPos(blockPos, dirUp1, dirOut1, dirRight1));

        newPortal1.dimensionTo = newPortal2 != null ? newPortal2.world.getRegistryKey() : user.world.getRegistryKey();

        dirOut1 = ((BlockHitResult) hit).getSide().getOpposite().getVector();

        dirUp1 = dirOut1.getY() == 0 ? new Vec3i(0, 1, 0) : user.getHorizontalFacing().getVector();

        dirRight1 = dirUp1.crossProduct(dirOut1);

        dirRight1 = new Vec3i(-dirRight1.getX(), -dirRight1.getY(), -dirRight1.getZ());

        newPortal1.setOriginPos(calcPortalPos(blockPos, dirUp1, dirOut1, dirRight1));
        newPortal1.setOrientationAndSize(
                Vec3d.of(dirRight1), //axisW
                Vec3d.of(dirUp1), //axisH
                PORTAL_WIDTH, // width
                PORTAL_HEIGHT // height
        );
        if (config.enabled.enableRoundPortals)
            makeRoundPortal(newPortal1);
        newPortal1.portalTag = "portalgun_portal1";
    }

    public static void Settings2(Direction direction, BlockPos blockPos, HitResult hit, LivingEntity user) {
        if (newPortal2 == null)
            newPortal2 = Portalgun.CUSTOM_PORTAL.create(McHelper.getServer().getWorld(user.world.getRegistryKey()));

        PortalExtension portalExtension = PortalExtension.get(newPortal2);
        portalExtension.adjustPositionAfterTeleport = direction == Direction.UP || direction == Direction.DOWN;

        PortalGunConfig config = AutoConfig.getConfigHolder(PortalGunConfig.class).getConfig();

        newPortal2.dimensionTo = newPortal1 != null ? newPortal1.world.getRegistryKey() : user.world.getRegistryKey();

        newPortal2.setDestination(newPortal1 != null ? newPortal1.getPos() : calcPortalPos(blockPos, dirUp2, dirOut2, dirRight2));

        dirOut2 = ((BlockHitResult) hit).getSide().getOpposite().getVector();
        dirUp2 = dirOut2.getY() == 0 ? new Vec3i(0, 1, 0) : user.getHorizontalFacing().getVector();

        dirRight2 = dirUp2.crossProduct(dirOut2);

        dirRight2 = new Vec3i(-dirRight2.getX(), -dirRight2.getY(), -dirRight2.getZ());

        newPortal2.setOriginPos(calcPortalPos(blockPos, dirUp2, dirOut2, dirRight2));
        newPortal2.setOrientationAndSize(
                Vec3d.of(dirRight2), //axisW
                Vec3d.of(dirUp2), //axisH
                PORTAL_WIDTH, // width
                PORTAL_HEIGHT // height
        );
        if (config.enabled.enableRoundPortals)
            makeRoundPortal(newPortal2);
        newPortal2.portalTag = "portalgun_portal2";
    }

    public static void portal1Methods(LivingEntity user, HitResult hit, World world) {
        Direction direction = ((BlockHitResult) hit).getSide();

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos blockPos = blockHit.getBlockPos();


        Settings1(direction, blockPos, hit, user);

        if (newPortal2 != null) {
            portal2AxisW = newPortal2.axisW;
            portal2AxisH = newPortal2.axisH;
        }

        Settings2(direction, blockPos, hit, user);

        if (isSnowUp(direction))
            newPortal1.updatePosition(newPortal1.getX(), newPortal1.getY() - 0.875, newPortal1.getZ());

        newPortal2.updatePosition(newPortal1.getDestPos().getX(), newPortal1.getDestPos().getY(), newPortal1.getDestPos().getZ());
        newPortal2.setDestination(newPortal1.getPos());
        newPortal2.axisW = portal2AxisW;
        newPortal2.axisH = portal2AxisH;
    }

    public static void portal2Methods(LivingEntity user, HitResult hit, World world) {
        Direction direction = ((BlockHitResult) hit).getSide();

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos blockPos = blockHit.getBlockPos();
        World portal1World = McHelper.getServerWorld(World.OVERWORLD);

        if (newPortal1 != null) {
            portal1AxisW = newPortal1.axisW;
            portal1AxisH = newPortal1.axisH;
        }

        Settings2(direction, blockPos, hit, user);

        Settings1(direction, blockPos, hit, user);

        if (isSnowUp(direction))
            newPortal2.updatePosition(newPortal2.getX(), newPortal2.getY() - 0.875, newPortal2.getZ());

        newPortal1.updatePosition(newPortal2.getDestPos().getX(), newPortal2.getDestPos().getY(), newPortal2.getDestPos().getZ());
        newPortal1.setDestination(newPortal2.getPos());
        newPortal1.setWorld(portal1World);
        newPortal1.axisW = portal1AxisW;
        newPortal1.axisH = portal1AxisH;
    }
}
