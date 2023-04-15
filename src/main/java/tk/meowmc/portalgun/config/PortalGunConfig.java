package tk.meowmc.portalgun.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import tk.meowmc.portalgun.PortalGunMod;

@Config(name = PortalGunMod.MODID)
public class PortalGunConfig implements ConfigData {
    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("enabled")
    public final Enabled enabled = new Enabled();

    public static void register() {
        AutoConfig.register(PortalGunConfig.class, JanksonConfigSerializer::new);
    }

    public static PortalGunConfig get() {
        return AutoConfig.getConfigHolder(PortalGunConfig.class).getConfig();
    }

    public static void save() {
        AutoConfig.getConfigHolder(PortalGunConfig.class).save();
    }


    public static class Enabled {
        //public final boolean enableOldPortalGunModel = false;
        //public final boolean enableRoundPortals = true;
//        public final boolean portalFunneling = true;
    }

}
