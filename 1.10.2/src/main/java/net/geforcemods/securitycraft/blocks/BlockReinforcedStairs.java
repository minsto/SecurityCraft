package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.main.mod_SecurityCraft;
import net.geforcemods.securitycraft.tileentity.TileEntityOwnable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockReinforcedStairs extends BlockStairs implements ITileEntityProvider {

	public BlockReinforcedStairs(Block baseBlock, int meta) {
		super(meta != 0 ? baseBlock.getStateFromMeta(meta) : baseBlock.getDefaultState());
		this.useNeighborBrightness = true;
		
		if(baseBlock == mod_SecurityCraft.reinforcedStone || baseBlock == mod_SecurityCraft.reinforcedCobblestone || baseBlock == mod_SecurityCraft.reinforcedSandstone) {
			setSoundType(SoundType.STONE);
		} else { 
			setSoundType(SoundType.WOOD);
		}
	}
	
	@Override
	public void breakBlock(World par1World, BlockPos pos, IBlockState state){
        super.breakBlock(par1World, pos, state);
        par1World.removeTileEntity(pos);
    }

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityOwnable();
	}

}
