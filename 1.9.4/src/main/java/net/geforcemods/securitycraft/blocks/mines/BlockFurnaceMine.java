package net.geforcemods.securitycraft.blocks.mines;

import net.geforcemods.securitycraft.api.IExplosive;
import net.geforcemods.securitycraft.blocks.BlockOwnable;
import net.geforcemods.securitycraft.imc.waila.ICustomWailaDisplay;
import net.geforcemods.securitycraft.main.mod_SecurityCraft;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class BlockFurnaceMine extends BlockOwnable implements IExplosive, ICustomWailaDisplay {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

	public BlockFurnaceMine(Material par1Material) {
		super(par1Material);
	}

	/**
	 * Called upon the block being destroyed by an explosion
	 */
	@Override
	public void onBlockDestroyedByExplosion(World par1World, BlockPos pos, Explosion par5Explosion)
	{
		if (!par1World.isRemote)
		{
			this.explode(par1World, pos);
		}
	}

	@Override
	public void onBlockDestroyedByPlayer(World par1World, BlockPos pos, IBlockState state){
		if (!par1World.isRemote)
		{
			this.explode(par1World, pos);
		}
	}	

	public boolean onBlockActivated(World par1World, BlockPos pos, IBlockState state, EntityPlayer par5EntityPlayer, EnumFacing side, float par7, float par8, float par9){
		if(par1World.isRemote){
			return true;
		}else{
			if(par5EntityPlayer.inventory.getCurrentItem() == null || par5EntityPlayer.inventory.getCurrentItem().getItem() != mod_SecurityCraft.remoteAccessMine){
				this.explode(par1World, pos);
				return true;
			}else{
				return false;	   		
			}
		}
	}	
	
	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer){
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }
	
	@Override
	public void activateMine(World world, BlockPos pos) {}

	@Override
	public void defuseMine(World world, BlockPos pos) {}
	
	@Override
	public void explode(World par1World, BlockPos pos) {
		par1World.destroyBlock(pos, false);

		if(mod_SecurityCraft.configHandler.smallerMineExplosion){
			par1World.createExplosion((Entity)null, pos.getX(), pos.getY(), pos.getZ(), 2.5F, true);
		}else{
			par1World.createExplosion((Entity)null, pos.getX(), pos.getY(), pos.getZ(), 5.0F, true);
		}

	}

	/**
	 * Return whether this block can drop from an explosion.
	 */
	@Override
	public boolean canDropFromExplosion(Explosion par1Explosion)
	{
		return false;
	}
	
	/* TODO: no clue about this
	@SideOnly(Side.CLIENT)
    public IBlockState getStateForEntityRender(IBlockState state)
    {
        return this.getDefaultState().withProperty(FACING, EnumFacing.SOUTH);
    }*/

	@Override
	public IBlockState getStateFromMeta(int meta){
        return this.getDefaultState().withProperty(FACING, EnumFacing.values()[meta].getAxis() == EnumFacing.Axis.Y ? EnumFacing.NORTH : EnumFacing.values()[meta]);
    }

    @Override
	public int getMetaFromState(IBlockState state){
        return state.getValue(FACING).getIndex();
    }

    @Override
	protected BlockStateContainer createBlockState(){
        return new BlockStateContainer(this, new IProperty[] {FACING});
    }
	
	@Override
	public boolean isActive(World world, BlockPos pos) {
		return true;
	}
	
	@Override
	public boolean isDefusable() {
		return false;
	}

	@Override
	public ItemStack getDisplayStack(World world, IBlockState state, BlockPos pos) {
		return new ItemStack(Blocks.FURNACE);
	}

	@Override
	public boolean shouldShowSCInfo(World world, IBlockState state, BlockPos pos) {
		return false;
	}

}
