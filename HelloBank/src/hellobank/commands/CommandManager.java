package hellobank.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hellobank.data.ATM;
import hellobank.data.BankAccount;
import net.md_5.bungee.api.ChatColor;
import hellobank.main.Main;
import hellobank.utils.Utils;

public class CommandManager implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		try {
			Player plr = (Player) sender;
			if (args.length == 0) {
				Utils.showGuide(plr);
				return true;
			}
			String func = args[0];
			UUID uuid = plr.getUniqueId();
			if (func.equals("atm")) {
				if (!plr.hasPermission(Utils.pm)) {
					plr.sendMessage(ChatColor.RED + "You don't have access to this command!");
					return false;
				}
				Location loc = plr.getLocation();
				Bukkit.getServer().getWorld(loc.getWorld().getName()).getBlockAt(loc).setType(Material.CHEST);
				Block chest = loc.getWorld().getBlockAt(loc);
				ATM atm = new ATM(uuid, chest, chest.getLocation());
				Main.atmManager.addAtm(atm);
				plr.sendMessage(ChatColor.GREEN + "Added an ATM successfully!");
				return true;
			}
			if (func.equals("addaccount")) {
				if (Main.accountManager.getAccountFromUUID(uuid) != null) {
					plr.sendMessage(ChatColor.RED + "You already have a registered account.");
					return false;
				}
				if (args.length < 2) {
					plr.sendMessage(ChatColor.RED + "Usage: /bank addaccount <pin>.");
					return false;
				}
				String pin = args[1];
				if (pin.length() < 4 || pin.length() > 12) {
					plr.sendMessage(ChatColor.RED + "Pin must be between 4 and 12 characters.");
					return false;
				}
				BankAccount bankAccount = new BankAccount(uuid, pin);
				Main.accountManager.addAccount(bankAccount);
				plr.sendMessage(ChatColor.GREEN + "Successfully created account with '" + pin + "' pin!");
				return true;
			}
			if (func.equals("deleteaccount") || func.equals("removeaccount")) {
				BankAccount acc = Main.accountManager.getAccountFromUUID(uuid);
				if (acc == null) {
					plr.sendMessage(ChatColor.RED + "You don't have an account to delete.");
					return false;
				}
				if (args.length < 3) {
					plr.sendMessage(ChatColor.RED + "Usage: /bank " + func + " <pin>.");
					return false;
				}
				String pass = args[1];
				if ((!acc.getPassword().equals(pass))) {
					plr.sendMessage(ChatColor.RED + "Invalid credentials.");
					return false;
				}
				boolean success = Main.accountManager.deleteAccount(uuid);
				if (success) {
					Main.INSTANCE.getConfig().set("Accounts." + uuid.toString(), null);
					plr.sendMessage(ChatColor.GREEN + "Successfully deleted your account!");
				} else {
					plr.sendMessage(ChatColor.RED + "An error has occurred while deleting your account.");
				}
				return true;
			}
			if (func.equals("pin")) {
				BankAccount acc = Main.accountManager.getAccountFromUUID(uuid);
				if (acc == null) {
					plr.sendMessage(ChatColor.RED + "You don't have an account.");
					return false;
				}
				if (args.length < 2) {
					plr.sendMessage(ChatColor.RED + "Usage: /bank pin <password>.");
					return false;
				}
				String enteredPassword = args[1];
				if (!acc.getPassword().equals(enteredPassword)) {
					plr.sendMessage(ChatColor.RED + "Wrong password.");
					return false;
				}
				acc.setLastPasswordCheck(System.currentTimeMillis());
				plr.sendMessage(ChatColor.GREEN + "Successfully entered your account for 30 seconds!");
				return true;
			}
			if (func.equals("changepin")) {
				BankAccount acc = Main.accountManager.getAccountFromUUID(uuid);
				if (acc == null) {
					plr.sendMessage(ChatColor.RED + "You don't have an account.");
					return false;
				}
				if (args.length < 3) {
					plr.sendMessage(ChatColor.RED + "Usage: /bank changepin <oldPin> <newPin>.");
					return false;
				}
				String oldPass = args[1];
				String newPass = args[2];
				if (!acc.getPassword().equals(oldPass)) {
					plr.sendMessage(ChatColor.RED + "Wrong password.");
					return false;
				}
				if (newPass.length() < 4 || newPass.length() > 12) {
					plr.sendMessage(ChatColor.RED + "Pin must be between 4 and 12 characters.");
					return false;
				}
				acc.setPassword(newPass);
				plr.sendMessage(ChatColor.GREEN + "Successfully changed your password from '" + oldPass + "' to '" + newPass + "'!");
				return true;
			}
			Utils.showGuide(plr);
			return false;
		} catch(Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

}