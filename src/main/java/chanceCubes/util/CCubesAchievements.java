package chanceCubes.util;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;

import chanceCubes.blocks.CCubesBlocks;

public class CCubesAchievements {

    public static AchievementPage page;
    public static Achievement chanceIcosahedron;
    public static Achievement GiantChanceCube;
    public static Achievement lonelyDirt;
    public static Achievement wither;
    public static Achievement herobrine;
    public static Achievement itsALie;

    public static void loadAchievements() {
        chanceIcosahedron = new Achievement(
            "chancecubes.chanceIcosahedron",
            "chancecubes.chanceIcosahedron",
            0,
            0,
            CCubesBlocks.chanceIcosahedron,
            null).registerStat();
        GiantChanceCube = new Achievement(
            "chancecubes.GiantChanceCube",
            "chancecubes.GiantChanceCube",
            1,
            0,
            CCubesBlocks.chanceCompactGiantCube,
            null).registerStat();
        lonelyDirt = new Achievement("chancecubes.lonelyDirt", "chancecubes.lonelyDirt", 0, -1, Blocks.dirt, null)
            .registerStat();
        wither = new Achievement(
            "chancecubes.wither",
            "chancecubes.wither",
            1,
            -1,
            new ItemStack(Items.skull, 1, 1),
            null).registerStat();
        herobrine = new Achievement(
            "chancecubes.herobrine",
            "chancecubes.herobrine",
            2,
            -1,
            new ItemStack(Items.skull, 1, 2),
            null).registerStat();
        itsALie = new Achievement("chancecubes.itsALie", "chancecubes.itsALie", 0, 1, Items.cake, null).registerStat();
        page = new AchievementPage(
            "Chance Cubes",
            chanceIcosahedron,
            GiantChanceCube,
            lonelyDirt,
            wither,
            herobrine,
            itsALie);
        AchievementPage.registerAchievementPage(page);
    }
}
