package dev.dsai03.hold_it.init;

import com.mna.api.entities.construct.ai.ConstructTask;
import com.mna.api.tools.RLoc;
import dev.dsai03.hold_it.MyAwesomeMnaAddon;
import net.minecraftforge.registries.DeferredRegister;

public interface AwesomeConstructTasks {
	DeferredRegister<ConstructTask> CONSTRUCT_TASKS = DeferredRegister.create(RLoc.create("construct_task"), MyAwesomeMnaAddon.MODID);
}
