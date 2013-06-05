package de.hotmail.gurkilein.sellchest;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

public class SellchestBlockListener
  implements Listener
{
  private static final BlockFace[] faces = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };

  public boolean isInteger(String input)
  {
    try {
      Integer.parseInt(input);
      return true;
    }
    catch (Exception e) {
    }
    return false;
  }

  @EventHandler
  public void onBlockDestroy(BlockBreakEvent event)
    throws Exception
  {
    Player p = event.getPlayer();
    Block testblock = event.getBlock();
    Material type1 = testblock.getType();

    for (BlockFace face : faces) {
      Material type = testblock.getRelative(face).getType();
      Block nachbarblock = testblock.getRelative(face);

      if (type == Material.WALL_SIGN) {
        if ((nachbarblock.getData() == 3 & face.equals(BlockFace.WEST) | nachbarblock.getData() == 2 & face.equals(BlockFace.EAST) | nachbarblock.getData() == 4 & face.equals(BlockFace.NORTH) | nachbarblock.getData() == 5 & face.equals(BlockFace.SOUTH)))
        {
          Sign sign = (Sign)testblock.getRelative(face).getState();

          if (sign.getLine(0).contains("[SellChest]")) {
            if (!Sellchest.perms.has(p, "sellchest.admin")) {
              p.sendMessage(Sellchest.disallow);
              event.setCancelled(true);
              return;
            }
            p.sendMessage(Sellchest.destroy);
          }
        }
      }
    }

    Material type = testblock.getRelative(BlockFace.UP).getType();
    Block nachbarblock = testblock.getRelative(BlockFace.UP);
    if ((type1 == Material.CHEST) && 
      (type == Material.WALL_SIGN)) {
      Sign sign = (Sign)nachbarblock.getState();
      if (sign.getLine(0).contains("[SellChest]")) {
        if (!Sellchest.perms.has(p, "sellchest.admin")) {
          p.sendMessage(Sellchest.disallow);
          event.setCancelled(true);
          return;
        }
        sign.setLine(0, "");
        sign.setLine(1, "");
        sign.setLine(2, "");
        sign.setLine(3, "");
        sign.update();
        p.sendMessage(Sellchest.destroy);
      }

    }

    String block = event.getBlock().getType().toString();
    if (block == "WALL_SIGN") {
      Sign sign = (Sign)event.getBlock().getState();
      if (sign.getLine(0).contains("[SellChest]"))
        if (Sellchest.perms.has(p, "sellchest.admin")) {
          p.sendMessage(Sellchest.destroy);
        } else {
          event.setCancelled(true);
          p.sendMessage(Sellchest.disallow);
        }
    }
  }

  @EventHandler
  public void onSignPlace(SignChangeEvent event)
  {
    Player p = event.getPlayer();
    String ersteReihe = event.getLine(0);
    if (ersteReihe.equalsIgnoreCase("[SellChest]"))
      if (Sellchest.perms.has(p, "sellchest.admin")) {
        if (event.getBlock().getRelative(BlockFace.DOWN).getType() == Material.CHEST)
        {
          event.setLine(0, Sellchest.chestcolor + "[SellChest]");
          event.setLine(1, Sellchest.ready);
          p.sendMessage(Sellchest.make);
        }
        else {
          p.sendMessage(Sellchest.errorcreate);
          event.setLine(0, "");
          event.setLine(1, "");
          event.setLine(2, "");
          event.setLine(3, "");
        }
      } else {
        p.sendMessage(Sellchest.disallow);
        event.setLine(0, "");
        event.setLine(1, "");
        event.setLine(2, "");
        event.setLine(3, "");
      }
  }
}