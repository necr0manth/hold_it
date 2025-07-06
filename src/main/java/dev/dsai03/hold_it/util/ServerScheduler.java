package dev.dsai03.hold_it.util;

import lombok.AllArgsConstructor;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class ServerScheduler {
    private static final Object lock = new Object();
    private static final List<Task> pendingQueue = new ArrayList<>();
    private static final List<Task> activeQueue = new ArrayList<>();

    public static void schedule(int ticks, Runnable task) {
        synchronized (lock) {
            pendingQueue.add(new Task(ticks, task));
        }
    }

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START)
            return;
        synchronized (lock) {
            if (!pendingQueue.isEmpty()) {
                activeQueue.addAll(pendingQueue);
                pendingQueue.clear();
            }
        }

        activeQueue.removeIf(Task::tick);
    }

    @AllArgsConstructor
    static class Task {
        int ticks;
        Runnable action;

        boolean tick() {
            if (--ticks <= 0) {
                action.run();
                return true;
            }
            return false;
        }
    }
}
