package com.smoothlight;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;

public final class LightTransitions {
    private record Entry(int curEmission, int targetEmission, int curOpacity, int targetOpacity) {
        boolean done() {
            return curEmission == targetEmission && curOpacity == targetOpacity;
        }
    }

    private static final int FADE_STEP = 2;

    private static final ConcurrentHashMap<Object, ConcurrentHashMap<Long, Entry>> LEVELS = new ConcurrentHashMap<>();
    private static volatile boolean active;

    private LightTransitions() {
    }

    public static void onBlockChanged(Level level, BlockPos pos, BlockState oldState, BlockState newState) {
        if (oldState == newState) {
            return;
        }
        int newEmission = newState.getLightEmission();
        int newOpacity = Math.max(1, newState.getLightDampening());
        int oldEmission = oldState.getLightEmission();
        int oldOpacity = Math.max(1, oldState.getLightDampening());
        long key = pos.asLong();
        ConcurrentHashMap<Long, Entry> map = LEVELS.get(level);
        Entry prev = map != null ? map.get(key) : null;
        int fromEmission = prev != null ? prev.curEmission() : oldEmission;
        int fromOpacity = prev != null ? prev.curOpacity() : oldOpacity;
        if (fromEmission == newEmission && fromOpacity == newOpacity) {
            if (prev != null) {
                map.remove(key);
            }
            return;
        }
        if (map == null) {
            map = LEVELS.computeIfAbsent(level, l -> new ConcurrentHashMap<>());
        }
        map.put(key, new Entry(fromEmission, newEmission, fromOpacity, newOpacity));
        active = true;
    }

    public static int emission(Object level, long pos, int actual) {
        Entry e = entry(level, pos);
        return e != null ? e.curEmission() : actual;
    }

    public static int opacity(Object level, long pos, int actual) {
        Entry e = entry(level, pos);
        return e != null ? e.curOpacity() : actual;
    }

    public static boolean overridesShape(Object level, long pos) {
        return entry(level, pos) != null;
    }

    private static Entry entry(Object level, long pos) {
        if (!active) {
            return null;
        }
        ConcurrentHashMap<Long, Entry> map = LEVELS.get(level);
        return map != null ? map.get(pos) : null;
    }

    public static void tick(Level level) {
        ConcurrentHashMap<Long, Entry> map = LEVELS.get(level);
        if (map == null || map.isEmpty()) {
            return;
        }
        LevelLightEngine light = level.getLightEngine();
        Iterator<Map.Entry<Long, Entry>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, Entry> en = it.next();
            Entry e = en.getValue();
            Entry next = new Entry(
                    step(e.curEmission(), e.targetEmission()), e.targetEmission(),
                    step(e.curOpacity(), e.targetOpacity()), e.targetOpacity());
            if (next.done()) {
                it.remove();
            } else {
                en.setValue(next);
            }
            light.checkBlock(BlockPos.of(en.getKey()));
        }
    }

    public static void drop(Predicate<Object> levelFilter) {
        LEVELS.keySet().removeIf(levelFilter);
    }

    public static int step(int cur, int target) {
        if (cur < target) {
            return Math.min(cur + FADE_STEP, target);
        }
        if (cur > target) {
            return Math.max(cur - FADE_STEP, target);
        }
        return cur;
    }
}
