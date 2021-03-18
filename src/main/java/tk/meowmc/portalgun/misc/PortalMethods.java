package tk.meowmc.portalgun.misc;

import com.qouteall.immersive_portals.McHelper;
import com.qouteall.immersive_portals.portal.GeometryPortalShape;
import com.qouteall.immersive_portals.portal.Portal;
import com.qouteall.immersive_portals.portal.PortalExtension;
import com.qouteall.immersive_portals.portal.PortalManipulation;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import tk.meowmc.portalgun.items.PortalGunItem;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static tk.meowmc.portalgun.items.PortalGunItem.newPortal1;
import static tk.meowmc.portalgun.items.PortalGunItem.newPortal2;

public class PortalMethods {

    public static MinecraftClient client = MinecraftClient.getInstance();
    public static Vec3i dirUp1;
    public static Vec3i dirUp2;
    public static Vec3i dirOut1;
    public static Vec3i dirOut2;
    public static Vec3i dirRight1;
    public static Vec3i dirRight2;
    static Vec3d portal1AxisW;
    static Vec3d portal1AxisH;
    static Vec3d portal2AxisW;
    static Vec3d portal2AxisH;

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

    private static Vec3d calcPortalPos(BlockPos hit, Vec3i upright, Vec3i facing, Vec3i cross) {
        double upOffset = -0.5;
        double faceOffset = -0.505;
        double crossOffset = 0.0;
        return new Vec3d(
                ((hit.getX() + 0.5) + upOffset * upright.getX() + faceOffset * facing.getX() + crossOffset * cross.getX()), // x component
                ((hit.getY() + 0.5) + upOffset * upright.getY() + faceOffset * facing.getY() + crossOffset * cross.getY()), // y component
                ((hit.getZ() + 0.5) + upOffset * upright.getZ() + faceOffset * facing.getZ() + crossOffset * cross.getZ())  // z component
        );

    }

    public static Portal Settings1(Direction direction, BlockPos blockPos, HitResult hit) {
        Portal portal = Portal.entityType.create(McHelper.getServer().getWorld(client.world.getRegistryKey()));
        Vec3d portalPosition = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        Vec3d destPos = new Vec3d(blockPos.getX(), blockPos.getY() + 2, blockPos.getZ());
        PortalExtension portalExtension = PortalExtension.get(portal);

        portal.setDestination(destPos);
        if (newPortal2 != null)
            portal.dimensionTo = newPortal2.world.getRegistryKey();
        else
            portal.dimensionTo = client.world.getRegistryKey();

        portalExtension.adjustPositionAfterTeleport = direction == Direction.UP || direction == Direction.DOWN;

        dirOut1 = ((BlockHitResult) hit).getSide().getOpposite().getVector();
        if (dirOut1.getY() == 0) {
            dirUp1 = new Vec3i(0, 1, 0);
        } else {
            dirUp1 = client.player.getHorizontalFacing().getVector();
        }
        dirRight1 = dirUp1.crossProduct(dirOut1);

        dirRight1 = new Vec3i(-dirRight1.getX(), -dirRight1.getY(), -dirRight1.getZ());

        portal.setOriginPos(calcPortalPos(blockPos, dirUp1, dirOut1, dirRight1));
        portal.setOrientationAndSize(
                Vec3d.of(dirRight1), //axisW
                Vec3d.of(dirUp1), //axisH
                1, // width
                2 // height
        );
        makeRoundPortal(portal);
        portal.portalTag = "portalgun_portal1";
        return portal;
    }

    public static Portal Settings2(Direction direction, BlockPos blockPos, HitResult hit) {
        Portal portal = Portal.entityType.create(McHelper.getServer().getWorld(client.world.getRegistryKey()));
        Vec3d portalPosition = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        Vec3d destpos = newPortal1.getPos();
        PortalExtension portalExtension = PortalExtension.get(portal);

        if (newPortal1 != null)
            portal.dimensionTo = newPortal1.world.getRegistryKey();
        else
            portal.dimensionTo = client.world.getRegistryKey();
        portal.setDestination(newPortal1.getPos());
        portal.updatePosition(portalPosition.x, portalPosition.y, portalPosition.z);

        portalExtension.adjustPositionAfterTeleport = direction == Direction.UP || direction == Direction.DOWN;

        dirOut2 = ((BlockHitResult) hit).getSide().getOpposite().getVector();
        if (dirOut2.getY() == 0) {
            dirUp2 = new Vec3i(0, 1, 0);
        } else {
            dirUp2 = client.player.getHorizontalFacing().getVector();
        }
        dirRight2 = dirUp2.crossProduct(dirOut2);

        dirRight2 = new Vec3i(-dirRight2.getX(), -dirRight2.getY(), -dirRight2.getZ());

        portal.setOriginPos(calcPortalPos(blockPos, dirUp2, dirOut2, dirRight2));
        portal.setOrientationAndSize(
                Vec3d.of(dirRight2), //axisW
                Vec3d.of(dirUp2), //axisH
                1, // width
                2 // height
        );
        makeRoundPortal(portal);
        portal.portalTag = "portalgun_portal2";
        return portal;
    }


    public static void portal1Methods(LivingEntity user, HitResult hit) {
        Direction direction = ((BlockHitResult) hit).getSide();

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos blockPos = blockHit.getBlockPos();
        World portal2World = McHelper.getServerWorld(World.OVERWORLD);

        newPortal1 = Settings1(direction, blockPos, hit);
        newPortal1.setDestination(newPortal2.getPos());

        if (newPortal2 != null) {
            portal2World = newPortal2.getOriginWorld();
        }
        Vec3d portal2AxisW = newPortal2.axisW;
        Vec3d portal2AxisH = newPortal2.axisH;

        newPortal2 = Settings2(direction, blockPos, hit);
        newPortal2.updatePosition(newPortal1.getDestPos().getX(), newPortal1.getDestPos().getY(), newPortal1.getDestPos().getZ());
        newPortal2.setDestination(newPortal1.getPos());
        newPortal2.setWorld(portal2World);

        newPortal2.axisW = portal2AxisW;
        newPortal2.axisH = portal2AxisH;

        PortalManipulation.adjustRotationToConnect(newPortal1, newPortal2);

    }

    public static void portal2Methods(LivingEntity user, HitResult hit) {
        Direction direction = ((BlockHitResult) hit).getSide();

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos blockPos = blockHit.getBlockPos();
        World portal1World = McHelper.getServerWorld(World.OVERWORLD);

        if (newPortal1 != null) {
            portal1World = newPortal1.getOriginWorld();
        }
        if (newPortal1 != null) {
            portal1AxisW = newPortal1.axisW;
            portal1AxisH = newPortal1.axisH;
        }
        newPortal2 = Settings2(direction, blockPos, hit);
        newPortal1 = Settings1(direction, blockPos, hit);

        if (PortalGunItem.space2BlockState.getBlock().is(Blocks.SNOW) && direction == Direction.UP) {
            newPortal2.updatePosition(newPortal2.getX(), newPortal2.getY() - 0.875, newPortal2.getZ());
        }

        newPortal1.updatePosition(newPortal2.getDestPos().getX(), newPortal2.getDestPos().getY(), newPortal2.getDestPos().getZ());
        newPortal1.setDestination(new Vec3d(newPortal2.getX(), newPortal2.getY(), newPortal2.getZ()));
        newPortal1.setWorld(portal1World);
        newPortal1.axisW = portal1AxisW;
        newPortal1.axisH = portal1AxisH;

        PortalManipulation.adjustRotationToConnect(newPortal2, newPortal1);
    }
}
