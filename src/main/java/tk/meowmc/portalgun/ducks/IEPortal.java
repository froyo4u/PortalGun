package tk.meowmc.portalgun.ducks;

import com.qouteall.immersive_portals.portal.Portal;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;

public interface IEPortal {

    Boolean getActive(Portal portal);
    void setActive(Boolean active, Portal portal);


    TrackedData<Boolean> ISACTIVE = DataTracker.registerData(Portal.class, TrackedDataHandlerRegistry.BOOLEAN);

}
