package chanceCubes.rewards.type;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import chanceCubes.rewards.rewardparts.PotionPart;
import chanceCubes.util.Scheduler;
import chanceCubes.util.Task;

public class PotionRewardType extends BaseRewardType<PotionPart> {

    public PotionRewardType(PotionPart... effects) {
        super(effects);
    }

    @Override
    public void trigger(final PotionPart part, final World world, final int x, final int y, final int z,
        final EntityPlayer player) {
        if (part.getDelay() != 0) {
            Task task = new Task("Potion Reward Delay", part.getDelay()) {

                @Override
                public void callback() {
                    triggerPotion(part, world, x, y, z, player);
                }
            };
            Scheduler.scheduleTask(task);
        } else {
            triggerPotion(part, world, x, y, z, player);
        }
    }

    public void triggerPotion(PotionPart part, World world, int x, int y, int z, EntityPlayer player) {
        ItemStack potion = new ItemStack(Items.potionitem);
        NBTTagList effects = new NBTTagList();
        NBTTagCompound effectData = new NBTTagCompound();
        part.getEffect()
            .writeCustomPotionEffectToNBT(effectData);
        effects.appendTag(effectData);
        potion.stackTagCompound = new NBTTagCompound();
        potion.stackTagCompound.setTag("CustomPotionEffects", effects);

        EntityPotion entity = new EntityPotion(world, player, potion);
        entity.posX = player.posX;
        entity.posY = player.posY + 2;
        entity.posZ = player.posZ;
        entity.motionX = 0;
        entity.motionY = 0.1;
        entity.motionZ = 0;

        world.spawnEntityInWorld(entity);
    }
}
