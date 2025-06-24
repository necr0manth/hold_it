package dev.dsai03.hold_it.content.entities;

import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.spells.SpellCaster;
import dev.dsai03.hold_it.util.Entity2EntityReference;
import dev.dsai03.hold_it.util.SpellHolder;
import lombok.Getter;
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
    public static final EntityDataAccessor<Float> LIFETIME = SynchedEntityData.defineId(ChargeableSpellEntity.class, EntityDataSerializers.FLOAT);
    private Entity2EntityReference<LivingEntity> casterRef;
    @Getter
    private SpellHolder spellHolder;
    private boolean wasCharged = false;
    private long firstTickTime = -1;

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

    public ChargeableSpellEntity(EntityType<? extends ChargeableSpellEntity> entityType, LivingEntity casterRef, ISpellDefinition spell, Level world) {
        this(entityType, world);
        setCasterRef(casterRef);
        setSpell(spell);
        setPos(casterRef.position());
    }

    private void stopCast() {
        if (getCaster() instanceof Player player)
            player.releaseUsingItem();
        discard();
    }

    public void tick() {
        super.tick();
        if (tickCount == 0)
            return;
        if (getCaster() != null)
            setPos(getCaster().position());
        if (!level().isClientSide) {
            if (getCaster() == null) {
                discard();
                return;
            }
            if (firstTickTime == -1) {
                firstTickTime = System.nanoTime();
            }
            entityData.set(LIFETIME, (System.nanoTime() - firstTickTime) / 1e9f);
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
            if (!level().isClientSide) {
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


    public float getLifetime() {
        return entityData.get(LIFETIME);
    }

    public void setCasterRef(LivingEntity casterRef) {
        this.casterRef.set(casterRef);
    }

    public ISpellDefinition getSpell() {
        return spellHolder.getSpell();
    }

    @Nullable
    public LivingEntity getCaster() {
        return casterRef.get();
    }

    public void setSpell(ISpellDefinition spell) {
        spellHolder.setSpell(spell);
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
        casterRef.save(compound);
        casterRef.save(compound);
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
        spellHolder.load(compound);
        casterRef.load(compound);
    }

    protected void defineSynchedData() {
        entityData.define(LIFETIME, 0f);
        casterRef = Entity2EntityReference.createAndDefine("caster", this, ChargeableSpellEntity.class);
        spellHolder = SpellHolder.createAndDefine(entityData, "spell", ChargeableSpellEntity.class);
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
