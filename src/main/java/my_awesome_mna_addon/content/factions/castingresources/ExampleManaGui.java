package my_awesome_mna_addon.content.factions.castingresources;


import com.mna.api.capabilities.resource.ICastingResourceGuiProvider;
import my_awesome_mna_addon.content.factions.FactionRIDs;
import my_awesome_mna_addon.init.AwesomeItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ExampleManaGui implements ICastingResourceGuiProvider {
    public ResourceLocation getTexture() {
        return FactionRIDs.FACTION_HUD_TEXTURE;
    }

    public ItemStack getBadgeItem() {
        return new ItemStack(AwesomeItems.MARK_OF_THE_EXAMPLE_FACTION.get());
    }

    public int getXPBarColor() {
        return 0xdaa520;
    }

    public int getBarColor() {
        return 0xdaa520;
    }

    public int getBarManaCostEstimateColor() {
        return 0xffd700;
    }

    public int getResourceNumericTextColor() {
        return 0xffd700;
    }

    public int getBadgeItemOffsetY() {
        return 10;
    }

    public int getBadgeSize() {
        return 64;
    }

    public int getFrameU() {
        return 0;
    }

    public int getFrameV() {
        return 0;
    }

    public int getFrameWidth() {
        return 153;
    }

    public int getFrameHeight() {
        return 24;
    }

    public int getLevelDisplayY() {
        return this.getFrameHeight() - 2;
    }
}
