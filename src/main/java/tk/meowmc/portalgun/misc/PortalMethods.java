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
import net.minecraft.world.World;
import org.ejml.data.FixedMatrix3x3_64F;
import tk.meowmc.portalgun.items.PortalGunItem;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static tk.meowmc.portalgun.items.PortalGunItem.newPortal1;
import static tk.meowmc.portalgun.items.PortalGunItem.newPortal2;

public class PortalMethods {

    public static MinecraftClient client = MinecraftClient.getInstance();
    static FixedMatrix3x3_64F planeMatrix;
    static FixedMatrix3x3_64F planeMatrixInverse;
    static Direction direction;
    static Direction playerDirection;

    @SuppressWarnings("ReturnOfNull")
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

    public static void setPlaneInformation(HitResult hit) {
        planeMatrix = new FixedMatrix3x3_64F();
        planeMatrixInverse = new FixedMatrix3x3_64F();
        planeMatrix.a11 = 0;
        planeMatrix.a22 = 0;
        planeMatrix.a33 = 0;
        planeMatrixInverse.a11 = 0;
        planeMatrixInverse.a22 = 0;
        planeMatrixInverse.a33 = 0;
        direction = ((BlockHitResult) hit).getSide();
        switch (direction) {
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
                planeMatrix.a11 = 1;
                planeMatrix.a22 = 1;
                planeMatrixInverse.a11 = 1;
                planeMatrixInverse.a22 = 1;
                break;
        }
    }

    public static Vec3d multiply(FixedMatrix3x3_64F mat, Vec3d vec) {
        double x = mat.a11 * vec.x + mat.a12 * vec.y + mat.a13 * vec.z;
        double y = mat.a21 * vec.x + mat.a22 * vec.y + mat.a23 * vec.z;
        double z = mat.a31 * vec.x + mat.a32 * vec.y + mat.a33 * vec.z;
        return new Vec3d(x, y, z);
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

    public static Portal Settings1(Direction direction, BlockPos blockPos) {
        Portal portal = Portal.entityType.create(McHelper.getServer().getWorld(client.world.getRegistryKey()));
        Vec3d portalPosition = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        Vec3d destPos = new Vec3d(blockPos.getX(), blockPos.getY() + 2, blockPos.getZ());
        PortalExtension portalExtension = PortalExtension.get(portal);

        portal.setDestination(destPos);
        if (newPortal2 != null)
            portal.dimensionTo = newPortal2.world.getRegistryKey();
        else
            portal.dimensionTo = client.world.getRegistryKey();
        portalExtension.adjustPositionAfterTeleport = false;

        switch (direction) {
            case SOUTH:
                portal.updatePosition(portalPosition.x + 0.5, portalPosition.y, portalPosition.z + 1.005);
                break;
            case NORTH:
                portal.updatePosition(portalPosition.x + 0.5, portalPosition.y, portalPosition.z - 0.005);
                break;
            case WEST:
                portal.updatePosition(portalPosition.x - 0.005, portalPosition.y, portalPosition.z + 0.5);
                break;
            case EAST:
                portal.updatePosition(portalPosition.x + 1.005, portalPosition.y, portalPosition.z + 0.5);
                break;
            case UP:
                portal.updatePosition(portalPosition.x + 0.5, portalPosition.y + 1.005, portalPosition.z);
                portalExtension.adjustPositionAfterTeleport = true;
                break;
            case DOWN:
                portal.updatePosition(portalPosition.x + 0.5, portalPosition.y - 0.005, portalPosition.z);
                portalExtension.adjustPositionAfterTeleport = true;
                break;
        }


        Vec3d directionVec = getDirectionVec(direction);
        double scalar = directionVec.x + directionVec.y + directionVec.z;
        Vec3d rightVec = multiply(planeMatrixInverse, new Vec3d(scalar * 1, 0, 0));

        Vec3d axisH = multiply(planeMatrixInverse, new Vec3d(0, 1, 0));

        portal.axisW = rightVec;
        portal.axisH = axisH;
        portal.width = 1;
        portal.height = 2;
        makeRoundPortal(portal);
        portal.portalTag = "portalgun_portal1";
        return portal;
    }

    public static Portal Settings2(Direction direction, BlockPos blockPos) {
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
        portalExtension.adjustPositionAfterTeleport = false;

        switch (direction) {
            case SOUTH:
                portal.updatePosition(portalPosition.x + 0.5, portalPosition.y, portalPosition.z + 1.005);
                break;
            case NORTH:
                portal.updatePosition(portalPosition.x + 0.5, portalPosition.y, portalPosition.z - 0.005);
                break;
            case WEST:
                portal.updatePosition(portalPosition.x - 0.005, portalPosition.y, portalPosition.z + 0.5);
                break;
            case EAST:
                portal.updatePosition(portalPosition.x + 1.005, portalPosition.y, portalPosition.z + 0.5);
                break;
            case UP:
                portal.updatePosition(portalPosition.x + 0.5, portalPosition.y + 1.005, portalPosition.z);
                portalExtension.adjustPositionAfterTeleport = true;
                break;
            case DOWN:
                portal.updatePosition(portalPosition.x + 0.5, portalPosition.y - 0.005, portalPosition.z);
                portalExtension.adjustPositionAfterTeleport = true;
                break;
        }

        Vec3d directionVec = getDirectionVec(direction);
        double scalar = directionVec.x + directionVec.y + directionVec.z;
        Vec3d rightVec = multiply(planeMatrixInverse, new Vec3d(scalar * 1, 0, 0));

        Vec3d axisH = multiply(planeMatrixInverse, new Vec3d(0, 1, 0));

        portal.axisW = rightVec;
        portal.axisH = axisH;
        portal.width = 1;
        portal.height = 2;
        makeRoundPortal(portal);
        portal.portalTag = "portalgun_portal2";
        return portal;
    }


    public static void portal1Methods(LivingEntity user, HitResult hit) {
        Direction direction = ((BlockHitResult) hit).getSide();

        setPlaneInformation(hit);
        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos blockPos = blockHit.getBlockPos();
        World portal2World = McHelper.getServerWorld(World.OVERWORLD);

        newPortal1 = Settings1(direction, blockPos);
        newPortal1.setDestination(newPortal2.getPos());

        if (PortalGunItem.space2BlockState.getBlock().is(Blocks.SNOW) && direction == Direction.UP) {
            newPortal1.updatePosition(newPortal1.getX(), newPortal1.getY() - 0.875, newPortal1.getZ());
        }

        if (newPortal2 != null) {
            portal2World = newPortal2.getOriginWorld();
        }
        Vec3d portal2AxisW = newPortal2.axisW;
        Vec3d portal2AxisH = newPortal2.axisH;

        newPortal2 = Settings2(direction, blockPos);
        newPortal2.updatePosition(newPortal1.getDestPos().getX(), newPortal1.getDestPos().getY(), newPortal1.getDestPos().getZ());
        newPortal2.setDestination(newPortal1.getPos());
        newPortal2.setWorld(portal2World);

        newPortal2.axisW = portal2AxisW;
        newPortal2.axisH = portal2AxisH;

        PortalManipulation.adjustRotationToConnect(newPortal1, newPortal2);

    }

    public static void portal2Methods(LivingEntity user, HitResult hit) {
        Direction direction = ((BlockHitResult) hit).getSide();

        setPlaneInformation(hit);
        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos blockPos = blockHit.getBlockPos();
        World portal1World = McHelper.getServerWorld(World.OVERWORLD);

        if (newPortal1 != null) {
            portal1World = newPortal1.getOriginWorld();
        }
        Vec3d portal1AxisW = newPortal1.axisW;
        Vec3d portal1AxisH = newPortal1.axisH;
        newPortal2 = Settings2(direction, blockPos);
        newPortal1 = Settings1(direction, blockPos);

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
