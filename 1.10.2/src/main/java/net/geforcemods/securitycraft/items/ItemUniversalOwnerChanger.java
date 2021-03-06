package net.geforcemods.securitycraft.items;

import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.blocks.BlockReinforcedDoor;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemUniversalOwnerChanger extends Item
{
	public ItemUniversalOwnerChanger(){}

	/**
	 * Returns True is the item is renderer in full 3D when hold.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isFull3D()
	{
		return true;
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		TileEntity te = world.getTileEntity(pos);
		String newOwner = stack.getDisplayName();

		if(!world.isRemote)
		{
			if(!stack.hasDisplayName())
			{
				PlayerUtils.sendMessageToPlayer(player, I18n.format("item.universalOwnerChanger.name"), I18n.format("messages.universalOwnerChanger.noName"), TextFormatting.RED);
				return EnumActionResult.FAIL;
			}

			if(!(te instanceof IOwnable))
			{
				PlayerUtils.sendMessageToPlayer(player, I18n.format("item.universalOwnerChanger.name"), I18n.format("messages.universalOwnerChanger.cantChange"), TextFormatting.RED);
				return EnumActionResult.FAIL;
			}

			if(!((IOwnable)te).getOwner().isOwner(player))
			{
				PlayerUtils.sendMessageToPlayer(player, I18n.format("item.universalOwnerChanger.name"), I18n.format("messages.universalOwnerChanger.notOwned"), TextFormatting.RED);
				return EnumActionResult.FAIL;
			}

			if(BlockUtils.getBlock(world, pos) instanceof BlockReinforcedDoor)
			{
				if(BlockUtils.getBlock(world, pos.up()) instanceof BlockReinforcedDoor)
					((IOwnable)world.getTileEntity(pos.up())).getOwner().set(PlayerUtils.isPlayerOnline(newOwner) ? PlayerUtils.getPlayerFromName(newOwner).getUniqueID().toString() : "ownerUUID", newOwner);
				else
					((IOwnable)world.getTileEntity(pos.up())).getOwner().set(PlayerUtils.isPlayerOnline(newOwner) ? PlayerUtils.getPlayerFromName(newOwner).getUniqueID().toString() : "ownerUUID", newOwner);
			}

			if(te instanceof IOwnable)
				((IOwnable)te).getOwner().set(PlayerUtils.isPlayerOnline(newOwner) ? PlayerUtils.getPlayerFromName(newOwner).getUniqueID().toString() : "ownerUUID", newOwner);

			world.getMinecraftServer().getPlayerList().sendPacketToAllPlayers(te.getUpdatePacket());
			PlayerUtils.sendMessageToPlayer(player, I18n.format("item.universalOwnerChanger.name"), I18n.format("messages.universalOwnerChanger.changed").replace("#", newOwner), TextFormatting.GREEN);
			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.FAIL;
	}
}