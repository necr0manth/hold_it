package my_awesome_mna_addon.content.factions;
// bobr kurwa

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.faction.BaseFaction;
import com.mna.api.faction.IFaction;
import com.mna.api.sound.SFX;
import my_awesome_mna_addon.init.AwesomeFactions;
import my_awesome_mna_addon.init.AwesomeItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ExampleFaction extends BaseFaction {
	public ExampleFaction() {
		super(FactionRIDs.FACTION_EXAMPLE_ID, FactionRIDs.EXAMPLE_MANA);
	}

	@Override
	public List<IFaction> getEnemyFactions() {
		return ManaAndArtificeMod.getFactionHelper().getFactionsExcept(this);

		// if there is peace with another factions:
		// return ManaAndArtificeMod.getFactionHelper().getFactionsExcept(this.getRegistryName(), FactionIDs.FEY);
	}

	@Override
	public ItemStack getFactionGrimoire() {
		// This item stack is given to the player when they summon the FactionGrimoire.
		// It is best to return your own grimoire here, but you can also return one of another faction or other items

		return new ItemStack(AwesomeItems.MARK_OF_THE_EXAMPLE_FACTION.get());
	}

	@Override
	public Item getTokenItem() {
		// The token/mark that are dropped by the monster of this faction and is used for trading
		return AwesomeItems.MARK_OF_THE_EXAMPLE_FACTION.get();
	}

	@Override
	public Component getOcculusTaskPrompt(int i) {
		// You have to create the ritual (that lets you become part of this faction) yourself and pass its ID here
		return Component.literal("example_upgrade_ritual");
	}

	@Override
	public SoundEvent getRaidSound() {
		return SFX.Event.Faction.FACTION_RAID_FEY;
	}

	@Nullable
	@Override
	public SoundEvent getHornSound() {
		return SFX.Event.Faction.FACTION_HORN_FEY;
	}

	@Override
	public ResourceLocation getFactionIcon() {
		return FactionRIDs.FACTION_EXAMPLE_ICON;
	}

	@Override
	public int[] getManaweaveRGB() {
		return new int[]{218, 165, 32};
	}

	@Override
	public ChatFormatting getTornJournalPageFactionColor() {
		return ChatFormatting.GOLD;
	}
}
