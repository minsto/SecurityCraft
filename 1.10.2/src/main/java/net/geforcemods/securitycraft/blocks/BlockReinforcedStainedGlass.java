package net.geforcemods.securitycraft.blocks;

import java.util.Random;

import net.geforcemods.securitycraft.tileentity.TileEntityOwnable;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockReinforcedStainedGlass extends BlockStainedGlass implements ITileEntityProvider {

	public BlockReinforcedStainedGlass(Material par1Material) {
		super(par1Material);
		setSoundType(SoundType.GLASS);
	}
	
	@Override
	public void breakBlock(World par1World, BlockPos pos, IBlockState state){
        super.breakBlock(par1World, pos, state);
        par1World.removeTileEntity(pos);
    }

	/* TODO: no clue about this
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item par1Item, CreativeTabs par2CreativeTabs, List par3List){    	
    	for(int i = 0; i < 16; i++){
        	par3List.add(new ItemStack(par1Item, 1, i));
        }
    }*/
    
	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileEntityOwnable();
	}

	@Override
	public int quantityDropped(Random random)
	{
		return 1;
	}
}
