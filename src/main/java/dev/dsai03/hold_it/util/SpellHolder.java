package dev.dsai03.hold_it.util;

import com.mna.api.spells.base.ISpellDefinition;
import com.mna.spells.crafting.SpellRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
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

    public SpellHolder(SynchedEntityData entityData, String name, EntityDataAccessor<CompoundTag> dataAccessor) {
        this.entityData = entityData;
        this.dataAccessor = dataAccessor;
        this.name = name;
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

    public static SpellHolder createAndDefine(EntityDataAccessor<CompoundTag> dataAccessor, SynchedEntityData entityData, String name){
        var holder = new SpellHolder(entityData, name, dataAccessor);
        holder.define();
        return holder;
    }

    public static EntityDataAccessor<CompoundTag> createDataAccessor(Class<? extends Entity> cls){
        return SynchedEntityData.defineId(cls, EntityDataSerializers.COMPOUND_TAG);
    }
}
