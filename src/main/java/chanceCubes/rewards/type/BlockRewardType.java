package chanceCubes.rewards.type;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import chanceCubes.rewards.rewardparts.OffsetBlock;
import chanceCubes.util.RewardsUtil;

public class BlockRewardType extends BaseRewardType<OffsetBlock> {

    public BlockRewardType(OffsetBlock... blocks) {
        super(blocks);
    }

    @Override
    protected void trigger(OffsetBlock block, World world, int x, int y, int z, EntityPlayer player) {
        if (block == null) return;
        if (block.isRelativeToPlayer() && !RewardsUtil.isBlockUnbreakable(
            world,
            (int) Math.floor(player.posX),
            (int) Math.floor(player.posY),
            (int) Math.floor(player.posZ)))
            block.spawnInWorld(
                world,
                (int) Math.floor(player.posX),
                (int) Math.floor(player.posY),
                (int) Math.floor(player.posZ));
        else if (!RewardsUtil.isBlockUnbreakable(world, x, y + 3, z)) block.spawnInWorld(world, x, y, z);
    }
}
