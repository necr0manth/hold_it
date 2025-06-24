package dev.dsai03.hold_it.util;

import com.mna.api.spells.base.ISpellDefinition;
import com.mna.spells.crafting.SpellRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public class SpellHolder {
    private ISpellDefinition cachedSpell;
    private CompoundTag cachedTag;

    private final SynchedEntityData entityData;
    private final EntityDataAccessor<CompoundTag> dataAccessor;
    private final String name;
    private final static Map<Class<? extends Entity>, EntityDataAccessor<CompoundTag>> dataAccessorMap = new HashMap<>();

    public SpellHolder(SynchedEntityData entityData, String name, EntityDataAccessor<CompoundTag> dataAccessor) {
        this.entityData = entityData;
        this.dataAccessor = dataAccessor;
        this.name = name;
    }

    public <C extends Entity> SpellHolder(SynchedEntityData entityData, String name, Class<C> cls) {
        this(entityData, name, dataAccessorMap.computeIfAbsent(cls, cl -> SynchedEntityData.defineId(cl, EntityDataSerializers.COMPOUND_TAG)));
    }

    public static <C extends Entity> SpellHolder createAndDefine(SynchedEntityData entityData, String name, Class<C> cls) {
        var spellHolder = new SpellHolder(entityData, name, cls);
        spellHolder.define();
        return spellHolder;
    }

    public ISpellDefinition getSpell() {
        synchronized (this) {
            var tag = entityData.get(dataAccessor);
            if (tag == cachedTag)
                return cachedSpell;
            if ((cachedTag = tag).isEmpty())
                return cachedSpell = null;
            return cachedSpell = SpellRecipe.fromNBT(tag);
        }
    }

    public void setSpell(ISpellDefinition spell) {
        synchronized (this) {
            var tag = new CompoundTag();
            spell.writeToNBT(tag);
            entityData.set(dataAccessor, tag);
        }
    }

    public void define() {
        entityData.define(dataAccessor, new CompoundTag());
    }

    public void save(CompoundTag tag) {
        tag.put(name, entityData.get(dataAccessor));
    }

    public void load(CompoundTag tag) {
        entityData.set(dataAccessor, tag.getCompound(name));
    }
}
