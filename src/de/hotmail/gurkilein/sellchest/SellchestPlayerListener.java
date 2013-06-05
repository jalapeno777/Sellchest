package de.hotmail.gurkilein.sellchest;


import java.text.DecimalFormat;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SellchestPlayerListener
  implements Listener
{
  public void chestsell(Player p, Block chestblock)
  {
    Double betrag = Double.valueOf(0.0D);
    if (chestblock.getType() == Material.CHEST) {
      Chest conblock = (Chest)chestblock.getState();
      Inventory inv = conblock.getInventory();

      for (int i = 0; i < inv.getSize(); i++) {
        for (int j = 0; j < Sellchest.priceArray.length; j++) {
          ItemStack itemstack = inv.getItem(i);
          if (itemstack != null) {
            Integer id = Integer.valueOf(itemstack.getTypeId());
            Integer amount = Integer.valueOf(itemstack.getAmount());
            String line = (String)Sellchest.priceArray[j];
            if (id.toString().equals(line.split("=")[0].split(":")[0]))
            {
              Integer data;
              if (!line.split("=")[0].split(":")[0].equals(line.split("=")[0]))
                data = new Integer(line.split("=")[0].split(":")[1]);
              else {
                data = null;
              }

              if ((data == null) || (itemstack.getDurability() == data.intValue())) {
                inv.clear(i);
                betrag = Double.valueOf(betrag.doubleValue() + new Double(line.split("=")[1]).doubleValue() * amount.intValue());
              }
            }
          }
        }
      }
      DecimalFormat df = new DecimalFormat("#0.00");
      String betragstring = df.format(betrag);
      betrag = Double.valueOf(Double.parseDouble(betragstring));
      p.sendMessage(Sellchest.success + " " + betrag);
      Sellchest.econ.depositPlayer(p.getName(), betrag.doubleValue());
    }
  }

  @EventHandler
  public void onklick(PlayerInteractEvent event) throws Exception
  {
    Player p = event.getPlayer();
    if (event.getClickedBlock() != null) {
      String block = event.getClickedBlock().getType().toString();
      if (block == "WALL_SIGN") {
        Block chestblock = event.getClickedBlock().getRelative(BlockFace.DOWN);
        if (((Sign)event.getClickedBlock().getState()).getLine(0).contains("[SellChest]")) {
          if (Sellchest.perms.has(p, "sellchest.use")) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
              chestsell(p, chestblock);
          }
          else {
            p.sendMessage(Sellchest.disallow);
          }
        }
        ((Sign)event.getClickedBlock().getState()).update();
      }
    }
  }
}