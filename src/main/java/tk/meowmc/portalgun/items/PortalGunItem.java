package tk.meowmc.portalgun.items;

import com.qouteall.immersive_portals.McHelper;
import com.qouteall.immersive_portals.portal.Portal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
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
import org.ejml.data.FixedMatrix3x3_64F;
import tk.meowmc.portalgun.Portalgun;

import static net.minecraft.util.hit.HitResult.Type.BLOCK;
import static net.minecraft.util.hit.HitResult.Type.MISS;

public class PortalGunItem extends Item {
    public PortalGunItem(Settings settings) {
        super(settings);
    }

    MinecraftClient client = MinecraftClient.getInstance();
    HitResult hit;
    FixedMatrix3x3_64F planeMatrixInverse = new FixedMatrix3x3_64F();
    Portal newPortal1;


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



    public static Vec3d multiply(FixedMatrix3x3_64F mat, Vec3d vec){
        double x = mat.a11 * vec.x + mat.a12 * vec.y + mat.a13 * vec.z;
        double y = mat.a21 * vec.x + mat.a22 * vec.y + mat.a23 * vec.z;
        double z = mat.a31 * vec.x + mat.a32 * vec.y + mat.a33 * vec.z;
        return  new Vec3d(x, y, z);
    }

    private Portal Settings(Direction direction) {
        Entity entity = this.client.getCameraEntity();
        this.hit = entity.raycast(50.0D, 0.0F, false);


        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos blockPos = blockHit.getBlockPos();


        Portal portal = Portal.entityType.create(McHelper.getServer().getWorld(client.world.getRegistryKey()));

        Vec3d portalPosition = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        Vec3d destPos = new Vec3d(blockPos.getX(), blockPos.getY()+2, blockPos.getZ());

        Vec3d directionVec = getDirectionVec(direction);
        double scalar = directionVec.x + directionVec.y + directionVec.z;
        Vec3d rightVec = multiply(planeMatrixInverse, new Vec3d(scalar*1,0,0));

        Vec3d axisH = multiply(planeMatrixInverse, new Vec3d(0,1,0));

        portal.axisW = rightVec;
        portal.axisH = axisH;
        portal.updatePosition(portalPosition.x, portalPosition.y, portalPosition.z);
        portal.width = 1;
        portal.height = 2;
        portal.dimensionTo = client.world.getRegistryKey();
        portal.setDestination(destPos);
        portal.portalTag = "portalgun_portal";
        return portal;
    }

    public static void updatePortalRotation(Portal portal, Direction direction) {
        switch (direction){
            case EAST:
                portal.rotation = new Quaternion(new Vector3f(0, 1, 0), 90, true);
                break;
            case WEST:
                portal.rotation = new Quaternion(new Vector3f(0, 1, 0), -90, true);
                break;
            case NORTH:
                break;
            case SOUTH:
                portal.rotation = new Quaternion(new Vector3f(0, 1, 0), 180, true);
                break;
        }
    }


    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        user.getItemCooldownManager().set(this, 4);
        Entity entity = this.client.getCameraEntity();
        this.hit = entity.raycast(50.0D, 0.0F, false);


        if (this.hit.getType() == MISS) {
            return TypedActionResult.fail(itemStack);
        } else if (this.hit.getType() == BLOCK){

            Direction direction = ((BlockHitResult) hit).getSide();

            BlockHitResult blockHit = (BlockHitResult) hit;
            BlockPos blockPos = blockHit.getBlockPos();

            newPortal1 = Settings(direction);



            if (user.fishHook == null) {
                world.playSound(null, user.getX(), user.getY(), user.getZ(), Portalgun.PORTAL_SHOOT_EVENT, SoundCategory.PLAYERS, 1.0F, 1F);
                if (!world.isClient && !user.isSneaking()) {
                    McHelper.spawnServerEntity(newPortal1);
                }
                user.incrementStat(Stats.USED.getOrCreateStat(this));
            }
        }
        return TypedActionResult.success(itemStack, world.isClient());
    }

}
