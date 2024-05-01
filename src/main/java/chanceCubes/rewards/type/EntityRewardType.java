package chanceCubes.rewards.type;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.apache.logging.log4j.Level;

import chanceCubes.CCubesCore;
import chanceCubes.rewards.rewardparts.EntityPart;
import chanceCubes.util.Scheduler;
import chanceCubes.util.Task;

public class EntityRewardType extends BaseRewardType<EntityPart> {

    public EntityRewardType(EntityPart... entities) {
        super(entities);
    }

    @Override
    public void trigger(final EntityPart part, final World world, final int x, final int y, final int z,
        final EntityPlayer player) {
        if (part.getDelay() != 0) {
            Task task = new Task("Entity Reward Delay", part.getDelay()) {

                @Override
                public void callback() {
                    spawnEntity(part, world, x, y, z, player);
                }
            };
            Scheduler.scheduleTask(task);
        } else {
            spawnEntity(part, world, x, y, z, player);
        }
    }

    public void spawnEntity(EntityPart part, World world, int x, int y, int z, EntityPlayer player) {
        if (part.shouldRemovedBlocks()) for (int yy = 0; yy < 4; yy++) for (int xx = -1; xx < 2; xx++)
            for (int zz = -1; zz < 2; zz++) world.setBlock(x + xx, y + yy, z + zz, Blocks.air);

        Entity newEnt = EntityList.createEntityFromNBT(part.getNBT(), world);
        newEnt.setPosition(x + 0.5, y, z + 0.5);
        world.spawnEntityInWorld(newEnt);
    }

    public static NBTTagCompound getBasicNBTForEntity(String entity) {
        String json = "{id:" + entity + "}";
        NBTTagCompound nbt = null;
        try {
            nbt = (NBTTagCompound) JsonToNBT.func_150315_a(json);
        } catch (NBTException e) {
            CCubesCore.logger.log(Level.ERROR, "Failed to create a simple NBTTagCompound from " + entity);
            return null;
        }
        return nbt;
    }
}
