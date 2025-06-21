package dev.dsai03.hold_it.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.Objects;
import java.util.UUID;

public class Entity2EntityReference<T extends Entity> {
    public final EntityDataAccessor<Integer> ID;
    public final EntityDataAccessor<String> DIMENSION;
    public final Entity entity;
    public final String name;
    private UUID uuid;

    public Entity2EntityReference(EntityDataAccessor<Integer> idDataAccessor, EntityDataAccessor<String> dimensionDataAccessor, String name, Entity entity) {
        this.ID = idDataAccessor;
        this.DIMENSION = dimensionDataAccessor;
        this.entity = entity;
        this.name = name;
    }

    public void save(CompoundTag tag) {
        var tag1 = new CompoundTag();
        tag1.putUUID("uuid", uuid);
        tag1.putString("dimension", entity.getEntityData().get(DIMENSION));
        tag.put(name, tag1);
    }

    public void load(CompoundTag tag) {
        var tag1 = tag.getCompound(name);
        uuid = tag1.getUUID("uuid");
        entity.getEntityData().set(DIMENSION, tag1.getString("dimension"));
    }

    public void define() {
        entity.getEntityData().define(ID, -1);
        entity.getEntityData().define(DIMENSION, "");
    }

    public T get() {
        if (entity.level() instanceof ServerLevel serverLevel) {
            if (uuid == null)
                return null;
            var dimension = entity.getEntityData().get(DIMENSION);
            if (dimension.isEmpty())
                return null;
            var level = serverLevel.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.tryParse(dimension)));
            if(level == null)
                return null;
            return (T) level.getEntity(uuid);
        }
        if (!Objects.equals(entity.level().dimension().location().toString(), entity.getEntityData().get(DIMENSION)))
            return null;
        var id = entity.getEntityData().get(ID);
        return (T) entity.level().getEntity(id);
    }

    public void set(T entity) {
        uuid = entity.getUUID();
        this.entity.getEntityData().set(ID, entity.getId());
        this.entity.getEntityData().set(DIMENSION, entity.level().dimension().location().toString());
    }
}
