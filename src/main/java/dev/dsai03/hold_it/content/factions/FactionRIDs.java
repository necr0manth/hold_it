package dev.dsai03.hold_it.content.factions;

import dev.dsai03.hold_it.MyAwesomeMnaAddon;
import net.minecraft.resources.ResourceLocation;

public interface FactionRIDs {
	ResourceLocation FACTION_EXAMPLE_ID = new ResourceLocation(MyAwesomeMnaAddon.MODID, "example_faction");
	ResourceLocation EXAMPLE_MANA = new ResourceLocation(MyAwesomeMnaAddon.MODID, "example_mana");
	ResourceLocation FACTION_EXAMPLE_ICON = new ResourceLocation(MyAwesomeMnaAddon.MODID, "textures/guide/faction_icon_example.png");
	ResourceLocation FACTION_HUD_TEXTURE = new ResourceLocation(MyAwesomeMnaAddon.MODID, "textures/gui/gui_manabars.png");
}
