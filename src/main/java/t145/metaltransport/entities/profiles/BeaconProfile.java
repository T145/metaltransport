package t145.metaltransport.entities.profiles;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiBeacon;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import t145.metaltransport.api.profiles.IProfileFactory;
import t145.metaltransport.api.profiles.IUniversalProfile;
import t145.metaltransport.core.MetalTransport;

public class BeaconProfile implements IUniversalProfile {

	public static class ProfileFactoryBeacon implements IProfileFactory {

		@Override
		public BeaconProfile create(EntityMinecart cart) {
			return new BeaconProfile(cart);
		}
	}

	public final TileEntityBeacon beacon;
	private final EntityMinecart cart;

	public BeaconProfile(EntityMinecart cart) {
		Block block = Blocks.BEACON;
		World world = cart.world;

		this.beacon = (TileEntityBeacon) block.createTileEntity(world, block.getDefaultState());
		this.beacon.setWorld(world);
		this.beacon.setPos(cart.getPosition());
		this.cart = cart;
	}

	@Nonnull
	@Override
	public Container getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerBeacon(player.inventory, beacon) {

			@Override
			public boolean canInteractWith(EntityPlayer player) {
				return cart.isEntityAlive() && player.getDistanceSq(cart) <= 64.0D;
			}
		};
	}

	@SideOnly(Side.CLIENT)
	@Nonnull
	@Override
	public GuiContainer getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GuiBeacon(player.inventory, beacon);
	}

	@Override
	public NBTTagCompound serializeNBT() {
		return beacon.writeToNBT(new NBTTagCompound());
	}

	@Override
	public void deserializeNBT(NBTTagCompound tag) {
		beacon.readFromNBT(tag);
	}

	@Override
	public void tick(World world, BlockPos pos) {
		if (!pos.equals(beacon.getPos())) {
			this.beacon.setPos(pos);
		}

		this.beacon.update();
	}

	@Override
	public void activate(EntityPlayer player, EnumHand hand) {
		if (!player.world.isRemote) {
			MetalTransport.openGui(player, cart);
			player.addStat(StatList.BEACON_INTERACTION);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void render(Render renderer, EntityMinecart cart, ItemStack stack, float partialTicks) {
		TileEntityRendererDispatcher.instance.render(beacon, -0.5, 0, -0.5, partialTicks);
	}
}
