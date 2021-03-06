package net.geforcemods.securitycraft.blocks.mines;

import java.util.Random;

import net.geforcemods.securitycraft.api.IIntersectable;
import net.geforcemods.securitycraft.entity.EntityBouncingBetty;
import net.geforcemods.securitycraft.tileentity.TileEntityOwnable;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockBouncingBetty extends BlockExplosive implements IIntersectable {

	public static final PropertyBool DEACTIVATED = PropertyBool.create("deactivated");

	public BlockBouncingBetty(Material par2Material) {
		super(par2Material);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state){
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state){
		return false;
	} 

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
	{
		return new AxisAlignedBB(0.200F, 0.000F, 0.200F, 0.800F, 0.200F, 0.800F);
	}
	
	/**
	 * Checks to see if its valid to put this block at the specified coordinates. Args: world, x, y, z
	 */
	@Override
	public boolean canPlaceBlockAt(World par1World, BlockPos pos){
		return par1World.isSideSolid(pos.down(), EnumFacing.UP);
	}
	
	@Override
	public void onEntityIntersected(World world, BlockPos pos, Entity entity) {
		if(entity instanceof EntityLivingBase){
			this.explode(world, pos);
		}
	}

	@Override
	public void onBlockClicked(World par1World, BlockPos pos, EntityPlayer par5EntityPlayer){
		if(par5EntityPlayer instanceof EntityLivingBase){
			this.explode(par1World, pos);
		}
	}
	
	@Override
	public void activateMine(World world, BlockPos pos) {
		BlockUtils.setBlockProperty(world, pos, DEACTIVATED, false);
	}

	@Override
	public void defuseMine(World world, BlockPos pos) {
		BlockUtils.setBlockProperty(world, pos, DEACTIVATED, true);
	}
	
	@Override
	public void explode(World par1World, BlockPos pos){
		if(par1World.isRemote){ return; }

		par1World.setBlockToAir(pos);
		EntityBouncingBetty entitytntprimed = new EntityBouncingBetty(par1World, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
		entitytntprimed.fuse = 15;
		entitytntprimed.motionY = 0.50D;
		par1World.spawnEntityInWorld(entitytntprimed);
		entitytntprimed.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("game.tnt.primed")), 1.0F, 1.0F);
	}

	/**
	 * Returns the ID of the items to drop on destruction.
	 */
	@Override
	public Item getItemDropped(IBlockState state, Random par2Random, int par3)
	{
		return Item.getItemFromBlock(this);
	}

	/**
	 * only called by clickMiddleMouseButton , and passed to inventory.setCurrentItem (along with isCreative)
	 */
	@Override
	public ItemStack getItem(World par1World, BlockPos pos, IBlockState state){
		return new ItemStack(Item.getItemFromBlock(this));
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return this.getDefaultState().withProperty(DEACTIVATED, meta == 1 ? true : false);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return (state.getValue(DEACTIVATED).booleanValue() ? 1 : 0);
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, new IProperty[] {DEACTIVATED});
	}
	
	@Override
	public boolean isActive(World world, BlockPos pos) {
		return !world.getBlockState(pos).getValue(DEACTIVATED).booleanValue();
	}
	
	@Override
	public boolean isDefusable() {
		return true;
	}
	
	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileEntityOwnable().intersectsEntities();
	}
	
}
