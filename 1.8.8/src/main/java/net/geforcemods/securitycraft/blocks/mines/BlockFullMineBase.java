package net.geforcemods.securitycraft.blocks.mines;

import java.util.Random;

import net.geforcemods.securitycraft.api.IIntersectable;
import net.geforcemods.securitycraft.imc.waila.ICustomWailaDisplay;
import net.geforcemods.securitycraft.main.mod_SecurityCraft;
import net.geforcemods.securitycraft.tileentity.TileEntityOwnable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class BlockFullMineBase extends BlockExplosive implements IIntersectable, ICustomWailaDisplay {

	private final Block blockDisguisedAs;

	public BlockFullMineBase(Material par1Material, Block disguisedBlock) {
		super(par1Material);
		 blockDisguisedAs = disguisedBlock; 
	}

	public int getRenderType(){
		return 3;
	}

	public AxisAlignedBB getCollisionBoundingBox(World par1World, BlockPos pos, IBlockState state){
		return null;
	}

	public void onEntityIntersected(World world, BlockPos pos, Entity entity){
		if(entity instanceof EntityItem){
			return;
		}else{
			this.explode(world, pos);
		}
	}

	/**
	 * Called upon the block being destroyed by an explosion
	 */
	public void onBlockDestroyedByExplosion(World par1World, BlockPos pos, Explosion par5Explosion){
		if (!par1World.isRemote)
		{
			Random random = new Random();

			if(random.nextInt(3) == 1){
				this.explode(par1World, pos);
			}
		}
	}

	public void onBlockDestroyedByPlayer(World par1World, BlockPos pos, IBlockState state){
		if (!par1World.isRemote)
		{
			this.explode(par1World, pos);
		}
	}	
	
	public void activateMine(World world, BlockPos pos) {}

	public void defuseMine(World world, BlockPos pos) {}

	public void explode(World par1World, BlockPos pos) {
		par1World.destroyBlock(pos, false);

		if(mod_SecurityCraft.configHandler.smallerMineExplosion){
			par1World.createExplosion((Entity)null, pos.getX(), pos.getY() + 0.5D, pos.getZ(), 2.5F, true);
		}else{
			par1World.createExplosion((Entity)null, pos.getX(), pos.getY() + 0.5D, pos.getZ(), 5.0F, true);
		}
	}

	/**
	 * Return whether this block can drop from an explosion.
	 */
	public boolean canDropFromExplosion(Explosion par1Explosion){
		return false;
	}
	
	public boolean isActive(World world, BlockPos pos) {
		return true;
	}
	
	public boolean explodesWhenInteractedWith() {
		return false;
	}

	public boolean isDefusable() {
		return false;
	}

	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityOwnable().intersectsEntities();
	}
	
	public ItemStack getDisplayStack(World world, IBlockState state, BlockPos pos) {
		return new ItemStack(blockDisguisedAs);
	}

	public boolean shouldShowSCInfo(World world, IBlockState state, BlockPos pos) {
		return false;
	}

}