package de.hotmail.gurkilein.sellchest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Sellchest extends JavaPlugin
{
  public static Economy econ = null;
  public static Permission perms = null;
  public static Vault vault = null;
  public static Double interest;
  public static Double interestxp;
  public static ChatColor chestcolor;
  public static String make;
  public static String destroy;
  public static String disallow;
  public static String errorcreate;
  public static String success;
  public static String ready;
  public final SellchestPlayerListener playerListener = new SellchestPlayerListener();
  public final SellchestBlockListener blockListener = new SellchestBlockListener();
  public static Logger log = Logger.getLogger("Minecraft");
  public static Object[] priceArray;
  private static final ChatColor[] colors = { ChatColor.AQUA, ChatColor.BLACK, ChatColor.BLUE, ChatColor.DARK_AQUA, ChatColor.DARK_BLUE, ChatColor.DARK_GRAY, ChatColor.DARK_GREEN, ChatColor.DARK_PURPLE, ChatColor.DARK_RED, ChatColor.GOLD, ChatColor.GRAY, ChatColor.GREEN, ChatColor.LIGHT_PURPLE, ChatColor.RED, ChatColor.WHITE, ChatColor.YELLOW };

  public Integer getEssentialID(String name)
    throws Exception
  {
    File file = new File("plugins" + System.getProperty("file.separator") + "Essentials" + System.getProperty("file.separator") + "items.csv");
    Integer id = Integer.valueOf(-1);
    FileReader fr = new FileReader(file);
    BufferedReader reader = new BufferedReader(fr);
    String st;
    while ((st = reader.readLine()) != null)
    {
      if ((!st.startsWith("#")) && 
        (name.equals(st.split(",")[0]))) {
        id = new Integer(st.split(",")[1]);
      }
    }

    reader.close();
    fr.close();
    return id;
  }

  public void importEssentialWorth() {
    File file = new File("plugins" + System.getProperty("file.separator") + "Sellchest" + System.getProperty("file.separator") + "selltable.db");
    File file2 = new File("plugins" + System.getProperty("file.separator") + "Essentials" + System.getProperty("file.separator") + "worth.yml");
    try
    {
      FileConfiguration itemsdb = YamlConfiguration.loadConfiguration(file2);
      log.info("[Sellchest] Starting import of essentials database...");
      String output = "";
      String ohnedata = "";

      Integer dava = Integer.valueOf(-1);

      Set<String> set = itemsdb.getKeys(true);
      String[] array = (String[])set.toArray();
      String matstr = "";
      for (int i = 1; i < array.length; i++) {
        try {
          matstr = ((String)array[i]).split("worth.")[1].trim();
          Integer id = getEssentialID(matstr);
          if (id.intValue() != -1) {
            Double worth = Double.valueOf(itemsdb.getDouble(array[i]));
            ohnedata = matstr;
            if (!((String)array[(i + 1)]).contains((String)array[i]))
            {
              output = output + id + "=" + worth + System.getProperty("line.separator");
            }
          } else {
            String dot = ohnedata + ".";
            id = getEssentialID(ohnedata);
            if (id.intValue() != -1) {
              Double worth = Double.valueOf(itemsdb.getDouble(array[i]));
              dava = new Integer(matstr.split(dot)[1]);
              output = output + id + ":" + dava + "=" + worth + System.getProperty("line.separator");
            }
          }
        }
        catch (Exception e) {
          log.info("Could not import " + matstr + "! Please add this to your items.csv!");
        }
      }
      log.info("[Sellchest] Finished import!");

      FileWriter writer = new FileWriter(file, false);
      writer.write(output);
      writer.flush();
      writer.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public void onEnable() {
    Plugin x = getServer().getPluginManager().getPlugin("Vault");
    if ((x != null & x instanceof Vault))
      vault = (Vault)x;
    if (!setupEconomy()) {
      log.info("Vault needed");
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    setupPermissions();
    try
    {
      File configfile = new File("plugins" + System.getProperty("file.separator") + "Sellchest");
      configfile.mkdir();

      FileConfiguration config = getConfig();
      if (!config.contains("general.importfromessentials")) {
        config.set("general.importfromessentials", Boolean.valueOf(false));
      }
      if (!config.contains("language.color")) {
        config.set("language.color", Integer.valueOf(11));
      }
      if (!config.contains("language.prefix")) {
        config.set("language.prefix", "[SellChest]");
      }
      if (!config.contains("language.signcolor")) {
        config.set("language.signcolor", Integer.valueOf(11));
      }
      if (!config.contains("language.ready")) {
        config.set("language.ready", "Ready");
      }
      if (!config.contains("language.make")) {
        config.set("language.make", "SellChest created!");
      }
      if (!config.contains("language.destroy")) {
        config.set("language.destroy", "SellChest removed!");
      }
      if (!config.contains("language.disallow")) {
        config.set("language.disallow", "You are not allowed to do this!");
      }
      if (!config.contains("language.errorcreate")) {
        config.set("language.errorcreate", "Error creating SellChest!");
      }

      if (!config.contains("language.success")) {
        config.set("language.success", "Earned money: ");
      }
      saveConfig();
      configReload();

      if (config.getBoolean("general.importfromessentials")) {
        pricereload();
        importEssentialWorth();
      }
    }
    catch (Exception e1) {
      e1.printStackTrace();
    }

    PluginManager pm = getServer().getPluginManager();
    pm.registerEvents(this.playerListener, this);
    pm.registerEvents(this.blockListener, this);
    pricereload();

    log.info("Sellchest has been enabled!");
  }

  public void onDisable() {
    log.info("Sellchest has been disabled.");
  }
  private boolean setupPermissions() {
    RegisteredServiceProvider rsp = getServer().getServicesManager().getRegistration(Permission.class);
    perms = (Permission)rsp.getProvider();
    return perms != null;
  }
  public boolean setupEconomy() {
    if (getServer().getPluginManager().getPlugin("Vault") == null) {
      return false;
    }
    RegisteredServiceProvider rsp = getServer().getServicesManager().getRegistration(Economy.class);
    econ = (Economy)rsp.getProvider();
    return econ != null;
  }
  public void configReload() {
    reloadConfig();
    ChatColor color = colors[getConfig().getInt("language.color")];
    chestcolor = colors[getConfig().getInt("language.signcolor")];
    String prefix = getConfig().getString("language.prefix") + " ";
    ready = getConfig().getString("language.ready");
    make = color + prefix + getConfig().getString("language.make");
    destroy = color + prefix + getConfig().getString("language.destroy");
    disallow = color + prefix + getConfig().getString("language.disallow");
    errorcreate = color + prefix + getConfig().getString("language.errorcreate");
    success = color + prefix + getConfig().getString("language.success");
    pricereload();
  }

  public void pricereload()
  {
    File file = new File("plugins" + System.getProperty("file.separator") + "Sellchest");
    File file2 = new File(file + System.getProperty("file.separator") + "selltable.db");
    try {
      if (!file2.exists()) {
        file.mkdirs();
        FileWriter writer = new FileWriter(file2, true);
        writer.write("1=10");
        writer.write(System.getProperty("line.separator"));
        writer.flush();
        writer.close();
        log.info("New Selltable created!");
      }
      FileReader fr = new FileReader(file2);
      BufferedReader reader = new BufferedReader(fr);
      String st = "";
      List priceArrayload = new ArrayList();
      while ((st = reader.readLine()) != null)
      {
        priceArrayload.add(st);
      }
      fr.close();
      reader.close();
      priceArray = priceArrayload.toArray();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    Player player = null;
    if ((sender instanceof Player)) {
      player = (Player)sender;
    }
    if ((cmd.getName().equalsIgnoreCase("screload")) && 
      (player == null)) {
      sender.sendMessage("Sellchest reloaded!");
      configReload();
      return true;
    }

    return false;
  }
}