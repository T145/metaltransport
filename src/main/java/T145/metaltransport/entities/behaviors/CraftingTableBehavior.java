package T145.metaltransport.entities.behaviors;

import T145.metaltransport.api.carts.CartBehavior;
import T145.metaltransport.containers.CraftingTableInterface;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class CraftingTableBehavior extends CartBehavior {

	public CraftingTableBehavior() {
		super(Blocks.CRAFTING_TABLE);
	}

	@Override
	public void activate(EntityMinecart cart, EntityPlayer player, EnumHand hand) {
		World world = cart.world;

		if (!world.isRemote) {
			player.displayGui(new CraftingTableInterface(world, cart.getPosition()));
			player.addStat(StatList.CRAFTING_TABLE_INTERACTION);
		}
	}
}