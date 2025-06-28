package dev.dsai03.hold_it.content.entities;

import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.spells.base.ISpellDefinition;
import dev.dsai03.hold_it.content.client.particles.ParticleUtils;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import dev.dsai03.hold_it.util.AffinityDistribution;
import dev.dsai03.hold_it.util.Entity2EntityReference;
import dev.dsai03.hold_it.util.SpellHolder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

public class PortalEntity extends Entity {
    private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> SWORD_COUNT = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SWORDS_LAUNCHED = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.INT);
    private static final Entity2EntityReference.DataAccessor CASTER = new Entity2EntityReference.DataAccessor(PortalEntity.class);
    private static final EntityDataAccessor<CompoundTag> SPELL = SpellHolder.createDataAccessor(PortalEntity.class);
    
    private Entity2EntityReference<LivingEntity> casterRef;
    private SpellHolder spellHolder;
    private Random random = new Random();
    private int launchDelay = 0;
    private static final int LAUNCH_INTERVAL = 20; // 1 секунда между запусками мечей

    public PortalEntity(EntityType<? extends PortalEntity> entityType, Level level) {
        super(entityType, level);
        setNoGravity(true);
        setInvulnerable(true);
    }

    public PortalEntity(Level level, LivingEntity caster, ISpellDefinition spell, Vec3 position, float size, int swordCount) {
        this(AwesomeEntityTypes.PORTAL_ENTITY_TYPE.get(), level);
        casterRef.set(caster);
        spellHolder.setSpell(spell);
        setPos(position);
        setSize(size);
        setSwordCount(swordCount);
    }

    public LivingEntity getCaster() {
        return casterRef.get();
    }

    public ISpellDefinition getSpell() {
        return spellHolder.getSpell();
    }

    @Override
    public void tick() {
        super.tick();
        
        if (level().isClientSide) {
            clientTick();
            return;
        }

        // Логика запуска мечей
        if (getSwordsLaunched() < getSwordCount()) {
            launchDelay++;
            if (launchDelay >= LAUNCH_INTERVAL) {
                launchSword();
                launchDelay = 0;
            }
        } else {
            // Все мечи запущены, удаляем портал
            discard();
        }
    }

    private void launchSword() {
        if (level().isClientSide) return;
        
        // Создаем меч
        SwordEntity sword = new SwordEntity(level(), this);
        sword.setPos(position());
        sword.setPower(getSize() * 0.5f);
        
        // Случайное направление для меча
        double angle = random.nextDouble() * Math.PI * 2;
        double pitch = (random.nextDouble() - 0.5) * Math.PI * 0.5; // Небольшой разброс по вертикали
        Vec3 direction = getCaster().getLookAngle();

        sword.shoot(direction);
        level().addFreshEntity(sword);
        
        setSwordsLaunched(getSwordsLaunched() + 1);
    }

    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        // Создаем частицы портала
        Affinity affinity = AffinityDistribution.fromSpell(spellHolder.getSpell()).getRandomAffinity();
        
        // Основные частицы портала
        if (tickCount % 3 == 0) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = getSize() * (0.8 + random.nextDouble() * 0.4);
            
            Vec3 portalPos = position().add(
                Math.cos(angle) * radius,
                (random.nextDouble() - 0.5) * getSize() * 0.5,
                Math.sin(angle) * radius
            );
            
            ParticleUtils.addParticle(
                spellHolder.getSpell().colorParticle(new MAParticleType(ParticleUtils.getParticleType(affinity)), getCaster()),
                portalPos,
                Vec3.ZERO,
                ParticleUtils.EMPTY_TICKER,
                ParticleUtils.relativeTo(() -> position(), ParticleUtils.EMPTY_TICKER)
            );
        }
        
        // Спиральные частицы
        if (tickCount % 5 == 0) {
            double angle = (tickCount * 0.2) % (Math.PI * 2);
            double radius = getSize() * 0.6;
            
            Vec3 spiralPos = position().add(
                Math.cos(angle) * radius,
                Math.sin(tickCount * 0.1) * getSize() * 0.3,
                Math.sin(angle) * radius
            );
            
            Vec3 velocity = new Vec3(
                -Math.sin(angle) * 0.02,
                0.01,
                Math.cos(angle) * 0.02
            );
            
            ParticleUtils.addParticle(
                spellHolder.getSpell().colorParticle(new MAParticleType(ParticleUtils.getParticleType(affinity)), getCaster()),
                spiralPos,
                velocity,
                ParticleUtils.EMPTY_TICKER,
                ParticleUtils.relativeTo(() -> position(), ParticleUtils.EMPTY_TICKER)
            );
        }
        
        // Частицы при запуске меча
        if (getSwordsLaunched() < getSwordCount() && launchDelay >= LAUNCH_INTERVAL - 10) {
            for (int i = 0; i < 5; i++) {
                Vec3 burstPos = position().add(
                    (random.nextDouble() - 0.5) * getSize() * 0.5,
                    (random.nextDouble() - 0.5) * getSize() * 0.5,
                    (random.nextDouble() - 0.5) * getSize() * 0.5
                );
                
                Vec3 burstVelocity = position().subtract(burstPos).normalize().scale(0.1);
                
                ParticleUtils.addParticle(
                    spellHolder.getSpell().colorParticle(new MAParticleType(ParticleUtils.getParticleType(affinity)), getCaster()),
                    burstPos,
                    burstVelocity,
                    ParticleUtils.EMPTY_TICKER,
                    ParticleUtils.relativeTo(() -> position(), ParticleUtils.EMPTY_TICKER)
                );
            }
        }
    }

    public void setSize(float size) {
        entityData.set(SIZE, size);
    }

    public float getSize() {
        return entityData.get(SIZE);
    }

    public void setSwordCount(int count) {
        entityData.set(SWORD_COUNT, count);
    }

    public int getSwordCount() {
        return entityData.get(SWORD_COUNT);
    }

    public void setSwordsLaunched(int count) {
        entityData.set(SWORDS_LAUNCHED, count);
    }

    public int getSwordsLaunched() {
        return entityData.get(SWORDS_LAUNCHED);
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        return new EntityDimensions(getSize(), getSize(), false);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(SIZE, 1.0f);
        entityData.define(SWORD_COUNT, 5);
        entityData.define(SWORDS_LAUNCHED, 0);
        spellHolder = SpellHolder.createAndDefine(SPELL, entityData, "spell");
        casterRef = Entity2EntityReference.createAndDefine(CASTER, "caster", this);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (SIZE.equals(key)) {
            refreshDimensions();
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putFloat("size", getSize());
        compound.putInt("swordCount", getSwordCount());
        compound.putInt("swordsLaunched", getSwordsLaunched());
        compound.putInt("launchDelay", launchDelay);
        spellHolder.save(compound);
        casterRef.save(compound);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        setSize(compound.getFloat("size"));
        setSwordCount(compound.getInt("swordCount"));
        setSwordsLaunched(compound.getInt("swordsLaunched"));
        launchDelay = compound.getInt("launchDelay");
        spellHolder.load(compound);
        casterRef.load(compound);
    }
} 