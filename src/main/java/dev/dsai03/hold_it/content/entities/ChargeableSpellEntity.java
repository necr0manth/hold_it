package dev.dsai03.hold_it.content.entities;

import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.SpellRecipe;
import dev.dsai03.hold_it.util.Entity2EntityReference;
import dev.dsai03.hold_it.util.LazySpellHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;

public abstract class ChargeableSpellEntity extends Entity {
    public static final EntityDataAccessor<CompoundTag> SPELL_RECIPE = SynchedEntityData.defineId(ChargeableSpellEntity.class, EntityDataSerializers.COMPOUND_TAG);
    public static final EntityDataAccessor<Float> LIFETIME = SynchedEntityData.defineId(ChargeableSpellEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Integer> CASTER_ID = SynchedEntityData.defineId(ChargeableSpellEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<String> CASTER_DIMENSION = SynchedEntityData.defineId(ChargeableSpellEntity.class, EntityDataSerializers.STRING);
    private Entity2EntityReference<LivingEntity> caster;
    public final LazySpellHolder spellHolder = new LazySpellHolder(() -> {
        var s = entityData.get(SPELL_RECIPE);
        if (s.isEmpty())
            return null;
        return SpellRecipe.fromNBT(s);
    });
    private boolean wasCharged = false;
    private long firstTimeTime = -1;

    public ChargeableSpellEntity(EntityType<? extends ChargeableSpellEntity> entityType, Level world) {
        super(entityType, world);
        setNoGravity(true);
        setInvulnerable(true);
        refreshDimensions();
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        return new EntityDimensions(0, 0, true);
    }

    @Override
    public boolean isAlwaysTicking() {
        return true;
    }

    public ChargeableSpellEntity(EntityType<? extends ChargeableSpellEntity> entityType, LivingEntity caster, ISpellDefinition spell, Level world) {
        this(entityType, world);
        setCaster(caster);
        setSpell(spell);
        setPos(caster.position());
    }

    private void stopCast() {
        if (getCaster() instanceof Player player)
            player.releaseUsingItem();
        discard();
    }

    public void tick() {
        super.tick();
        if(tickCount==0)
            return;
        if (getCaster() != null)
            setPos(getCaster().position());
        if (!level().isClientSide) {
            if (getCaster() == null) {
                discard();
                return;
            }
            if (firstTimeTime == -1) {
                firstTimeTime = System.nanoTime();
            }
            entityData.set(LIFETIME, (System.nanoTime() - firstTimeTime) / 1e9f);
        }
        LivingEntity caster = getCaster();
        ISpellDefinition recipe = getSpell();
        if (caster != null && caster.isAlive() && caster.level().dimension().equals(level().dimension()) && caster.getUseItemRemainingTicks() > 0) {
            if (isOverCharged() && !level().isClientSide) {
                applySpell();
                stopCast();
            } else {
                if (!level().isClientSide() && !recipe.isValid()) {
                    stopCast();
                } else {
                    boolean isCharged = isCharged();
                    if (isCharged) {
                        if (!wasCharged)
                            onCharged();
                        else
                            overChargeTick();
                    } else
                        chargeTick();
                    wasCharged = isCharged;
                }
            }
        } else {
            if(!level().isClientSide) {
                if (isCharged())
                    applySpell();
                else {
                    onInterrupt();
                }
                stopCast();
            }
        }
    }

    protected abstract boolean isCharged();

    protected abstract void chargeTick();

    protected abstract void overChargeTick();

    protected abstract boolean isOverCharged();

    protected abstract void onCharged();

    protected void applySpell() {
        if (level().isClientSide())
            return;
        for (var target : target()) {
            SpellSource source = new SpellSource(getCaster(), InteractionHand.MAIN_HAND);
            SpellContext context = new SpellContext(this.level(), getSpell());
            HashMap<SpellEffect, ComponentApplicationResult> results = SpellCaster.ApplyComponents(getSpell(), source, target, context);
            if (getCaster() instanceof Player player) {
                results.forEach((key, value) -> {
                    if (value.is_success) {
                        SpellCaster.addComponentRoteProgress(player, key);
                    }
                });
            }
        }
    }

    protected abstract Collection<SpellTarget> target();

    protected abstract void onInterrupt();

    @Nullable
    public LivingEntity getCaster() {
        return caster.get();
    }

    public float getLifetime() {
        return entityData.get(LIFETIME);
    }

    public void setCaster(LivingEntity caster) {
        this.caster.set(caster);
    }

    public ISpellDefinition getSpell() {
        return spellHolder.getSpell();
    }

    public void setSpell(ISpellDefinition spell) {
        CompoundTag nbt = new CompoundTag();
        spell.writeToNBT(nbt);
        entityData.set(SPELL_RECIPE, nbt);
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.put("spell", entityData.get(SPELL_RECIPE));
        caster.save(compound);
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("spell")) {
            entityData.set(SPELL_RECIPE, (CompoundTag) compound.get("spell"));
        }
        caster.load(compound);
    }

    protected void defineSynchedData() {
        entityData.define(SPELL_RECIPE, new CompoundTag());
        entityData.define(LIFETIME, 0f);
        caster = new Entity2EntityReference<>(CASTER_ID, CASTER_DIMENSION, "caster", this);
        caster.define();
    }

    public final int getOverrideColor() {
        LivingEntity caster = getCaster();
        ISpellDefinition recipe = getSpell();
        if (caster != null && recipe != null) {
            MutableInt color = new MutableInt(-1);
            if (caster instanceof Player) {
                caster.getCapability(PlayerMagicProvider.MAGIC).ifPresent((m) -> {
                    color.setValue(m.getParticleColorOverride());
                });
            }

            if (color.getValue() == -1) {
                color.setValue(recipe.getParticleColorOverride());
            }

            return color.getValue();
        } else {
            return -1;
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
