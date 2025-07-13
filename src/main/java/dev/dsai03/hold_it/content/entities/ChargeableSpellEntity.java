package dev.dsai03.hold_it.content.entities;

import com.mna.api.spells.adjusters.SpellAdjustingContext;
import com.mna.api.spells.adjusters.SpellCastStage;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.spells.crafting.SpellRecipe;
import dev.dsai03.hold_it.util.Entity2EntityReference;
import dev.dsai03.hold_it.util.SpellHolder;
import dev.dsai03.hold_it.util.SpellUtils;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class ChargeableSpellEntity extends Entity {
    private static final EntityDataAccessor<Float> LIFETIME = SynchedEntityData.defineId(ChargeableSpellEntity.class, EntityDataSerializers.FLOAT);
    private static final Entity2EntityReference.DataAccessor CASTER = new Entity2EntityReference.DataAccessor(ChargeableSpellEntity.class);
    private static final EntityDataAccessor<CompoundTag> SPELL = SpellHolder.createDataAccessor(ChargeableSpellEntity.class);
    private Entity2EntityReference<LivingEntity> casterRef;
    @Getter
    private SpellHolder spellHolder;
    @Getter
    private boolean spellCasted = false;
    private boolean overrideManaCost = true;
    private long firstTickTime = -1;
    private final EventListener eventListener = new EventListener();

    public enum InterruptReason {
        NOT_PREPARED,
        INVALID_RECIPE,
        DEAD_CASTER,
        NOT_ENOUGH_MANA,
        INVALID_CASTER,
        STOP_CASTING,
        OTHER
    }

    private class EventListener {
        @SubscribeEvent
        public void onStopUsingItem(LivingEntityUseItemEvent.Stop event) {
            if (!level().isClientSide && event.getEntity() == getCaster()) {
                if (isPrepared())
                    if (allowCastWhenNotEnoughMana() || casterHasEnoughMana())
                        applySpell();
                    else
                        onInterrupt(InterruptReason.NOT_ENOUGH_MANA);
                else
                    onInterrupt(InterruptReason.NOT_PREPARED);
            }
        }
    }

    public boolean casterHasEnoughMana() {
        var manaCost = getRequestedManaCost();
        boolean[] ans = new boolean[1];
        Optional.ofNullable(getCaster()).ifPresent(
                caster -> caster.getCapability(PlayerMagicProvider.MAGIC).ifPresent(
                        magic -> {
                            if (magic.getCastingResource().hasEnough(caster, manaCost))
                                ans[0] = true;
                        }));
        return ans[0];
    }

    public void adjustSpell(SpellAdjustingContext context) {
        if (overrideManaCost)
            context.spell.setManaCost(getRequestedManaCost());
    }

    public ChargeableSpellEntity(EntityType<? extends ChargeableSpellEntity> entityType, Level world) {
        super(entityType, world);
        setNoGravity(true);
        setInvulnerable(true);
        refreshDimensions();
        MinecraftForge.EVENT_BUS.register(eventListener);
    }

    @Override
    public void remove(RemovalReason pReason) {
        super.remove(pReason);
        MinecraftForge.EVENT_BUS.unregister(eventListener);
    }

    public float getBaseSpellManaCost() {
        overrideManaCost = false;
        if (getSpell() instanceof SpellRecipe recipe)
            recipe.calculateManaCost();
        SpellUtils.applyAdjusters(getSpell(), getCaster(), false, SpellCastStage.CALCULATING_MANA_COST);
        overrideManaCost = true;
        return getSpell().getManaCost();
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
            if (firstTickTime == -1) {
                firstTickTime = System.nanoTime();
            }
            entityData.set(LIFETIME, (System.nanoTime() - firstTickTime) / 1e9f);
        }
        LivingEntity caster = getCaster();
        ISpellDefinition recipe = getSpell();
        if (spellCasted)
            return;
        if (!level().isClientSide) {
            if (caster == null) {
                interrupt(InterruptReason.OTHER);
                return;
            }
            if (recipe == null || !recipe.isValid()) {
                interrupt(InterruptReason.INVALID_RECIPE);
                return;
            }
            if (!caster.isAlive()) {
                interrupt(InterruptReason.DEAD_CASTER);
                return;
            }
            if (!caster.level().dimension().equals(level().dimension()) || caster.getUseItemRemainingTicks() <= 0) {
                interrupt(InterruptReason.INVALID_CASTER);
                return;
            }
            if (getCaster() instanceof Player player) {
                player.getCapability(PlayerMagicProvider.MAGIC).ifPresent(magic -> {
                    magic.getCastingResource().addRegenerationModifier("chargeableSpell", -1);
                });
            }
        }
        spellTick();
    }

    public void interrupt(InterruptReason reason) {
        onInterrupt(reason);
        stopCast();
    }

    public boolean isPrepared() {
        return true;
    }

    protected abstract void spellTick();

    public boolean allowCastWhenNotEnoughMana() {
        return false;
    }

    public abstract float getRequestedManaCost();

    public float getManaCost() {
        if (allowCastWhenNotEnoughMana())
            return Math.min(getRequestedManaCost(), getCasterMana());
        return getRequestedManaCost();
    }

    protected abstract void applySpell(float requestedManaCost, float casterMana);

    public float getCasterMana() {
        float[] ans = new float[1];
        Optional.ofNullable(getCaster()).ifPresent(
                caster -> caster.getCapability(PlayerMagicProvider.MAGIC).ifPresent(
                        magic -> {
                            ans[0] = magic.getCastingResource().getAmount();
                        }));
        return ans[0];
    }

    protected void applySpell() {
        var manaCost = getRequestedManaCost();
        Optional.ofNullable(getCaster()).ifPresent(
                caster -> caster.getCapability(PlayerMagicProvider.MAGIC).ifPresent(
                        magic -> {
                            applySpell(manaCost, magic.getCastingResource().getAmount());
                            SpellUtils.consumeMana(getCaster(), manaCost);
                            spellCasted = true;
                        }));
        if (!spellCasted) {
            onInterrupt(InterruptReason.INVALID_CASTER);
            spellCasted = true;
        }
        discard();
    }

    public void applySpellAndStopCast() {
        applySpell();
        stopCast();
    }

    protected void onInterrupt(InterruptReason reason) {
    }

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
        casterRef = Entity2EntityReference.createAndDefine(CASTER, "caster", this);
        spellHolder = SpellHolder.createAndDefine(SPELL, entityData, "spell");
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
