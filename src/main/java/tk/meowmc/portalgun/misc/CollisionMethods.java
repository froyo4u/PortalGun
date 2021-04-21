package tk.meowmc.portalgun.misc;


public class CollisionMethods {

    public static boolean isRecentlyCollidingWithPortal_pg(int age, long collidingPortalActiveTickTime) {
        return (long)age - collidingPortalActiveTickTime < 5L;
    }
}
