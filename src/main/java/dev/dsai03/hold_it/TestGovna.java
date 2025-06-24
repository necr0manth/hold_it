package dev.dsai03.hold_it;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;

@GameTestHolder(MyAwesomeMnaAddon.MODID)
public class TestGovna {
    @GameTest
    public static void test(GameTestHelper helper){
        helper.succeedIf(() -> helper.assertTrue(true, "idi nahuy"));
    }
}
