package dev.dsai03.hold_it.content.spells.shapes;

import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellSource;
import dev.dsai03.hold_it.content.entities.PortalSwordShapeEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class PortalSwordShape extends BaseChargeableSpellShape<PortalSwordShapeEntity> {
    public PortalSwordShape(ResourceLocation guiIcon, AttributeValuePair... attributeValuePairs) {
        super(guiIcon, attributeValuePairs);
    }

    @Override
    public PortalSwordShapeEntity createEntity(SpellSource source, Level level, ISpellDefinition recipe) {
        return new PortalSwordShapeEntity(source.getCaster(), level, recipe);
    }

    @Override
    public int baselineCooldown() {
        return 120; // 6 секунд кулдаун
    }

    @Override
    public float initialComplexity() {
        return 15; // Средняя сложность
    }

    @Override
    public int requiredXPForRote() {
        return 200; // Требуется 200 опыта для роута
    }
} 