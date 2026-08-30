/*
 * BlockSnad.java
 * Copyright (c) 2016 TheRoBrit
 * SNAD is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * SNAD is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robrit.snad.common.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockReed;
import net.minecraft.block.BlockSand;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

import com.robrit.snad.common.util.ConfigurationData;

public class BlockSnad extends BlockSand {

    public BlockSnad() {
        super();

        this.setTickRandomly(true);
        this.setHardness(0.5F);
        this.setStepSound(Block.soundTypeSand);
        this.setBlockName("snad");
        this.setCreativeTab(CreativeTabs.tabMisc);
        this.setBlockTextureName("sand");
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        Block blockAbove = world.getBlock(x, y + 1, z);

        if (!world.blockExists(x, y + 1, z)) {
            return;
        }

        if (blockAbove instanceof BlockReed || blockAbove instanceof BlockCactus) {
            // Walk up the plant column and tick each segment.
            // We use instanceof rather than exact class matching so that
            // modded subclasses of BlockReed / BlockCactus are accelerated too.
            final boolean isReedKind = blockAbove instanceof BlockReed;

            for (int height = 0;; height++) {
                if (!world.blockExists(x, y + 1 + height, z)) {
                    break;
                }

                Block currBlock = world.getBlock(x, y + 1 + height, z);

                // Stop when the block is no longer part of the same plant family.
                if (isReedKind ? !(currBlock instanceof BlockReed) : !(currBlock instanceof BlockCactus)) {
                    break;
                }

                for (int i = 0; i < ConfigurationData.SPEED_INCREASE_VALUE; i++) {
                    currBlock.updateTick(world, x, y + 1 + height, z, rand);
                }
            }
        } else if (blockAbove instanceof IPlantable) {
            // For other IPlantables (crops, stems, mod plants) call one extra
            // updateTick. The plant's own updateTick handles conditions.
            blockAbove.updateTick(world, x, y + 1, z, rand);
        }
    }

    @Override
    public boolean canSustainPlant(IBlockAccess world, int x, int y, int z, ForgeDirection direction,
        IPlantable plantable) {
        // Accept any plant type. The plant's own canBlockStay / placement checks
        // handle soil-specific validity (e.g. adjacent water for reeds, farmland for
        // crops, solid block for cave plants). This broad acceptance is what gives
        // Snad compatibility with mods that add new plant types.
        return true;
    }
}
