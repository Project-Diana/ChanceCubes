package chanceCubes.rewards.biodomeGen;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import chanceCubes.rewards.giantRewards.BioDomeReward;
import chanceCubes.rewards.rewardparts.OffsetBlock;
import chanceCubes.util.Scheduler;
import chanceCubes.util.Task;

public class SnowGlobeBiome implements IBioDomeBiome {

    private Random rand = new Random();

    @Override
    public void spawnEntities(final int centerX, final int centerY, final int centerZ, final World world) {
        for (int i = 0; i < rand.nextInt(10) + 5; i++) {
            EntitySnowman snowman = new EntitySnowman(world);
            snowman.setLocationAndAngles(
                centerX + (rand.nextInt(31) - 15),
                centerY + 1,
                centerZ + (rand.nextInt(31) - 15),
                0,
                0);
            world.spawnEntityInWorld(snowman);
        }

        Scheduler.scheduleTask(new Task("SnowGlobe Snow", 20) {

            @Override
            public void callback() {
                for (int i = 0; i < 100; i++) {
                    EntitySnowball snowball = new EntitySnowball(world);
                    snowball.motionX = -1 + (Math.random() * 2);
                    snowball.motionY = 0.8;
                    snowball.motionZ = -1 + (Math.random() * 2);
                    snowball.setLocationAndAngles(
                        centerX + (rand.nextInt(31) - 15),
                        centerY + 1,
                        centerZ + (rand.nextInt(31) - 15),
                        0,
                        0);
                    world.spawnEntityInWorld(snowball);
                }
            }
        });
    }

    @Override
    public Block getFloorBlock() {
        return Blocks.snow;
    }

    @Override
    public void getRandomGenBlock(float dist, Random rand, int x, int y, int z, List<OffsetBlock> blocks, int delay) {
        if (y != 0) return;
        if (dist < 0 && rand.nextInt(5) == 0) {
            OffsetBlock osb = new OffsetBlock(
                x,
                y + 1,
                z,
                Blocks.snow_layer,
                false,
                (delay / BioDomeReward.delayShorten));
            blocks.add(osb);
        }
    }
}
