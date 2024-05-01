package chanceCubes.rewards.biodomeGen;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import chanceCubes.rewards.giantRewards.BioDomeReward;
import chanceCubes.rewards.rewardparts.OffsetBlock;

public class DesertBiome implements IBioDomeBiome {

    @Override
    public void spawnEntities(int centerX, int centerY, int centerZ, World world) {

    }

    @Override
    public Block getFloorBlock() {
        return Blocks.sandstone;
    }

    @Override
    public void getRandomGenBlock(float dist, Random rand, int x, int y, int z, List<OffsetBlock> blocks, int delay) {
        if (y != 0) return;

        if (dist < 0 && rand.nextInt(50) == 0) {
            delay++;
            OffsetBlock osb = new OffsetBlock(x, y, z, Blocks.sand, false, (delay / BioDomeReward.delayShorten) + 1);
            blocks.add(osb);
            osb = new OffsetBlock(x, y - 1, z, Blocks.sandstone, false, (delay / BioDomeReward.delayShorten));
            blocks.add(osb);
            delay++;
            osb = new OffsetBlock(x, y + 1, z, Blocks.deadbush, false, (delay / BioDomeReward.delayShorten));
            blocks.add(osb);
            delay++;
        }

        if (dist < 0 && rand.nextInt(60) == 0) {
            delay++;
            OffsetBlock osb = new OffsetBlock(x, y, z, Blocks.sand, false, (delay / BioDomeReward.delayShorten) + 1);
            blocks.add(osb);
            osb = new OffsetBlock(x, y - 1, z, Blocks.sandstone, false, (delay / BioDomeReward.delayShorten));
            blocks.add(osb);
            delay++;
            osb = new OffsetBlock(x, y + 1, z, Blocks.cactus, false, (delay / BioDomeReward.delayShorten));
            blocks.add(osb);
            delay++;
            osb = new OffsetBlock(x, y + 2, z, Blocks.cactus, false, (delay / BioDomeReward.delayShorten));
            blocks.add(osb);
            delay++;
            osb = new OffsetBlock(x, y + 3, z, Blocks.cactus, false, (delay / BioDomeReward.delayShorten));
            blocks.add(osb);
            delay++;
        }
    }
}
