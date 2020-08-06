package online.smyhw.DynamicEconomy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class smyhw extends JavaPlugin implements Listener 
{
	public static Plugin smyhw_;
	public static Logger loger;
	public static FileConfiguration configer;
	//消息前缀
	public static String prefix;
	//每次降价降多少？
	public static double cuts;
	//每次降价后给其他物品涨价多少?
	public static double increase;
	//给予玩家货币的指令
	public static String cmd;
	@Override
    public void onEnable() 
	{
		getLogger().info("DynamicEconomy加载");
		getLogger().info("正在加载环境...");
		loger=getLogger();
		configer = getConfig();
		smyhw_=this;
		getLogger().info("正在加载配置...");
		saveDefaultConfig();
		prefix = configer.getString("config.prefix");
		cuts = configer.getDouble("config.cuts");
		increase = configer.getDouble("config.increase");
		cmd = configer.getString("config.cmd");
		getLogger().info("正在注册监听器...");
		Bukkit.getPluginManager().registerEvents(this,this);
		getLogger().info("DynamicEconomy加载完成");
    }

	@Override
    public void onDisable() 
	{
		saveConfig();
		getLogger().info("DynamicEconomy卸载");
    }
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
        if (cmd.getName().equals("de"))
        {
        	if(args.length<1) {helper(sender);return true;}
        	switch(args[0])
        	{
        		case "sell":
        		{
        			String itemName = ((Player) sender).getInventory().getItemInMainHand().getType().name();
        			double price = configer.getDouble("data."+itemName);
        			if(price==0) {sender.sendMessage(prefix+"该物品不可被卖出"); return true;}
        			//给予玩家货币
        			double num = ((Player) sender).getInventory().getItemInMainHand().getAmount()*price;
        			String do_cmd = smyhw.cmd;
        			do_cmd = do_cmd.replace("%player%", sender.getName());
        			do_cmd = do_cmd.replace("%num%",num+"");
        			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),do_cmd);
        			//降价该物品
        			price = price-smyhw.cuts;
        			configer.set("data."+itemName, price);
        			//升价其他物品
        			for(Material i:Material.values())
        			{
        				Double price_ = configer.getDouble("data."+i.name());
        				if(i.name().equals(itemName)) {continue;}
        				if(price_==0) {continue;}
        				configer.set("data."+i.name(),price_);
        			}
        			sender.sendMessage(prefix+"卖出物品<"+itemName+">获得货币<"+ String.format("%.2f", num)+">[目前单价<"+ String.format("%.2f", price)+">]");
        			//删除玩家物品
        			((Player) sender).getInventory().setItemInMainHand(null);
        			break;
        		}
        		case "reset":
        		{
        			if(args.length<2) 
        			{
            			configer.set(((Player) sender).getInventory().getItemInMainHand().getType().name(), 0);
            			sender.sendMessage(prefix+"物品<"+((Player) sender).getInventory().getItemInMainHand().getType().name()+">的价格已经重置");
        			}
        			else
        			{
        				configer.set(args[1], 0);
        				sender.sendMessage(prefix+"物品<"+args[1]+">的价格已经重置");
        			}
        			saveConfig();
        			break;
        		}
        		case "set":
        		{
        			if(args.length<2) {helper(sender);return true;}
        			configer.set(((Player) sender).getInventory().getItemInMainHand().getType().name(), Double.valueOf(args[1]));
        			sender.sendMessage(prefix+"物品<"+((Player) sender).getInventory().getItemInMainHand().getType().name()+">的价格已经设置为<"+args[1]+">");
        			saveConfig();
        			break;
        		}
        		default:
        		{
        			helper(sender);
        		}
        	}
        }
       return true;
	}
	
	public void helper(CommandSender sender)
	{
		String[] helper_admin = 
		{
				smyhw.prefix+"§7§m================",
				smyhw.prefix+"/de set <单价> §d#设置手上物品的价格",
				smyhw.prefix+"/de reset [物品ID] §d#重置价格,不指定ID则为手上物品",
				smyhw.prefix+"/de sell §d#出售手上的物品",
				smyhw.prefix+"§7§m---------------",
		};
		String[] helper_player = 
		{
				smyhw.prefix+"§7§m================",
				smyhw.prefix+"/de sell §d#出售手上的物品",
				smyhw.prefix+"§7§m---------------",
		};
		if(sender.hasPermission("DynamicEconomy.admin"))
		{
			sender.sendMessage(helper_admin);
		}
		else
		{
			sender.sendMessage(helper_player);
		}
	}
}