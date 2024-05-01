package chanceCubes.items;

import net.minecraft.item.ItemStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemSilkTouchPendant extends BaseChanceCubesItem {

    public ItemSilkTouchPendant() {
        super("silkTouchPendant");
        this.setMaxStackSize(1);
        super.addLore("Use this pendant to retrieve Chance Cubes");
        super.addLore("Player must hold this in hand to get the cube!");
    }

    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return true;
    }
}
