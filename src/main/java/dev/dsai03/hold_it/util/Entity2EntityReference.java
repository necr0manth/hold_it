package dev.dsai03.hold_it.util;

import lombok.AllArgsConstructor;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Entity2EntityReference<T extends Entity> {
    public final DataAccessor dataAccessor;
    public final Entity entity;
    public final String name;
    private UUID uuid;

    @AllArgsConstructor
    public static class DataAccessor {
        public final EntityDataAccessor<Integer> ID;
        public final EntityDataAccessor<String> DIMENSION;

        public DataAccessor(Class<? extends Entity> cls) {
            this(SynchedEntityData.defineId(cls, EntityDataSerializers.INT), SynchedEntityData.defineId(cls, EntityDataSerializers.STRING));
        }
    }

    public Entity2EntityReference(EntityDataAccessor<Integer> idDataAccessor, EntityDataAccessor<String> dimensionDataAccessor, String name, Entity entity) {
        this(new DataAccessor(idDataAccessor, dimensionDataAccessor), name, entity);
    }

    public Entity2EntityReference(DataAccessor dataAccessor, String name, Entity entity) {
        this.dataAccessor = dataAccessor;
        this.entity = entity;
        this.name = name;
    }

    public static <T extends Entity> Entity2EntityReference<T> createAndDefine(DataAccessor dataAccessor, String name, Entity entity) {
        var ref = new Entity2EntityReference<T>(dataAccessor, name, entity);
        ref.define();
        return ref;
    }

    public void save(CompoundTag tag) {
        var tag1 = new CompoundTag();
        // Проверяем uuid на null перед сохранением
        if (uuid != null) {
            tag1.putUUID("uuid", uuid);
        }
        tag1.putString("dimension", entity.getEntityData().get(dataAccessor.DIMENSION));
        tag.put(name, tag1);
    }

    public void load(CompoundTag tag) {
        var tag1 = tag.getCompound(name);
        // Проверяем наличие uuid в NBT перед загрузкой
        if (tag1.contains("uuid")) {
            uuid = tag1.getUUID("uuid");
        } else {
            uuid = null;
        }
        entity.getEntityData().set(dataAccessor.DIMENSION, tag1.getString("dimension"));
    }

    public void define() {
        entity.getEntityData().define(dataAccessor.ID, -1);
        entity.getEntityData().define(dataAccessor.DIMENSION, "");
    }

    public T get() {
        if (entity.level() instanceof ServerLevel serverLevel) {
            if (uuid == null)
                return null;
            var dimension = entity.getEntityData().get(dataAccessor.DIMENSION);
            if (dimension.isEmpty())
                return null;
            var level = serverLevel.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.tryParse(dimension)));
            if (level == null)
                return null;
            return (T) level.getEntity(uuid);
        }
        if (!Objects.equals(entity.level().dimension().location().toString(), entity.getEntityData().get(dataAccessor.DIMENSION)))
            return null;
        var id = entity.getEntityData().get(dataAccessor.ID);
        return (T) entity.level().getEntity(id);
    }

    public void set(T entity) {
        if (entity == null) {
            uuid = null;
            this.entity.getEntityData().set(dataAccessor.ID, -1);
            this.entity.getEntityData().set(dataAccessor.DIMENSION, "");
            return;
        }
        uuid = entity.getUUID();
        this.entity.getEntityData().set(dataAccessor.ID, entity.getId());
        this.entity.getEntityData().set(dataAccessor.DIMENSION, entity.level().dimension().location().toString());
    }
}
