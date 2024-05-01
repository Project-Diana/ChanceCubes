package chanceCubes.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import chanceCubes.CCubesCore;
import chanceCubes.blocks.CCubesBlocks;
import chanceCubes.tileentities.TileGiantCube;

public class GiantCubeUtil {

    /**
     * Check that structure is properly formed
     * 
     * @param xCoord
     * @param yCoord
     * @param zCoord
     * @param world
     * @param build  if the giant cube should be built if the structure is valid
     * @return if there is a valid 3x3x3 configuration
     */
    public static boolean checkMultiBlockForm(int xCoord, int yCoord, int zCoord, World world, boolean build) {
        Location3I bottomLeft = findBottomCorner(xCoord, yCoord, zCoord, world);
        int cx = bottomLeft.getX();
        int cy = bottomLeft.getY();
        int cz = bottomLeft.getZ();

        int i = 0;
        // Scan a 3x3x3 area, starting with the bottom left corner
        for (int x = cx; x < cx + 3; x++)
            for (int y = cy; y < cy + 3; y++) for (int z = cz; z < cz + 3; z++) if (world.getBlock(x, y, z)
                .equals(CCubesBlocks.chanceCube)) i++;
        // check if there are 27 blocks present (3*3*3) and if a giant cube should be built
        if (build) {
            if (i > 26) {
                setupStructure(cx, cy, cz, world, true);
                return true;
            }
            return false;
        } else {
            return i > 26;
        }
    }

    /** Setup all the blocks in the structure */
    public static void setupStructure(int xCoord, int yCoord, int zCoord, World world, boolean areCoordsCorrect) {
        int cx = xCoord;
        int cy = yCoord;
        int cz = zCoord;

        if (!areCoordsCorrect) {
            Location3I bottomLeft = findBottomCorner(xCoord, yCoord, zCoord, world);
            cx = bottomLeft.getX();
            cy = bottomLeft.getY();
            cz = bottomLeft.getZ();
        }

        int i = 0;
        for (int x = cx; x < cx + 3; x++) {
            for (int z = cz; z < cz + 3; z++) {
                for (int y = cy; y < cy + 3; y++) {
                    i++;
                    RewardsUtil.placeBlock(CCubesBlocks.chanceGiantCube, world, x, y, z, 0, i == 27 ? 3 : 2);
                    TileEntity tile = world.getTileEntity(x, y, z);
                    boolean master = (x == cx && y == cy + 1 && z == cz);
                    if (tile != null && (tile instanceof TileGiantCube)) {
                        ((TileGiantCube) tile).setMasterCoords(cx + 1, cy + 1, cz + 1);
                        ((TileGiantCube) tile).setHasMaster(true);
                        ((TileGiantCube) tile).setIsMaster(master);
                    }
                }
            }
        }
        world.playSoundEffect(xCoord, yCoord, zCoord, CCubesCore.MODID + ":giant_Cube_Spawn", 1, 1);
    }

    public static Location3I findBottomCorner(int xCoord, int yCoord, int zCoord, World world) {
        int cx = xCoord;
        int cy = yCoord;
        int cz = zCoord;
        while (world.getBlock(cx, cy - 1, cz)
            .equals(CCubesBlocks.chanceCube)) cy--;
        while (world.getBlock(cx - 1, cy, cz)
            .equals(CCubesBlocks.chanceCube)) cx--;
        while (world.getBlock(cx, cy, cz - 1)
            .equals(CCubesBlocks.chanceCube)) cz--;
        return new Location3I(cx, cy, cz);
    }

    /** Reset all the parts of the structure */
    public static void resetStructure(int xCoord, int yCoord, int zCoord, World world) {
        for (int x = xCoord - 1; x < xCoord + 2; x++)
            for (int y = yCoord - 1; y < yCoord + 2; y++) for (int z = zCoord - 1; z < zCoord + 2; z++) {
                TileEntity tile = world.getTileEntity(x, y, z);
                if (tile != null && (tile instanceof TileGiantCube)) {
                    ((TileGiantCube) tile).reset();
                    world.removeTileEntity(x, y, z);
                    world.setBlock(x, y, z, CCubesBlocks.chanceCube);
                }
            }
    }

    /** Reset all the parts of the structure */
    public static void removeStructure(int xCoord, int yCoord, int zCoord, World world) {
        for (int x = xCoord - 1; x < xCoord + 2; x++)
            for (int y = yCoord - 1; y < yCoord + 2; y++) for (int z = zCoord - 1; z < zCoord + 2; z++) {
                TileEntity tile = world.getTileEntity(x, y, z);
                if (tile != null && (tile instanceof TileGiantCube)) {
                    ((TileGiantCube) tile).reset();
                    world.removeTileEntity(x, y, z);
                    world.setBlockToAir(x, y, z);
                }
            }
    }
}
