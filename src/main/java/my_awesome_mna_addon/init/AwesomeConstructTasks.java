package my_awesome_mna_addon.init;

import com.mna.api.entities.construct.ai.ConstructTask;
import com.mna.api.tools.RLoc;
import my_awesome_mna_addon.MyAwesomeMnaAddon;
import net.minecraftforge.registries.DeferredRegister;

public interface AwesomeConstructTasks {
	DeferredRegister<ConstructTask> CONSTRUCT_TASKS = DeferredRegister.create(RLoc.create("construct_task"), MyAwesomeMnaAddon.MODID);
}
