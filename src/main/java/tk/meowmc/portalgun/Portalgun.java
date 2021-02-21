package tk.meowmc.portalgun;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import tk.meowmc.portalgun.items.PortalGunItem;

public class Portalgun implements ModInitializer {
    protected static final String MODID = "portalgun";


    public static final Item PORTALGUN = new PortalGunItem(new FabricItemSettings().fireproof().group(ItemGroup.TOOLS));

    public static final Identifier PORTAL_SHOOT = new Identifier("portalgun:portal_shoot");
    public static SoundEvent PORTAL_SHOOT_EVENT = new SoundEvent(PORTAL_SHOOT);

    @Override
    public void onInitialize() {
        Registry.register(Registry.ITEM, new Identifier(MODID, "portalgun"), PORTALGUN);
        Registry.register(Registry.SOUND_EVENT, PORTAL_SHOOT, PORTAL_SHOOT_EVENT);
    }
}
