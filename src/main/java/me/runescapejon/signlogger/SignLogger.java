package me.runescapejon.signlogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;

@Plugin(id = "signlogger", name = "SignLogger", description = "Basically log all the signs in game and console..", version = "1.0", authors = "runescapejon")
public class SignLogger {
	private static Logger logger;
	// hmmm why are you reading this? xD
	public static File Logf;
	private static File logDirectory;
	private Config configoptions;
	private SignLogger plugin;
	GuiceObjectMapperFactory factory;

	@Inject
	public SignLogger(Logger logger, @ConfigDir(sharedRoot = false) File log, GuiceObjectMapperFactory factory) {
		SignLogger.logger = logger;
		SignLogger.logDirectory = log;
		this.factory = factory;

	}

	public static Logger getLogger() {
		return logger;
	}

	public Config getCfg() {
		return configoptions;
	}

	public GuiceObjectMapperFactory getFactory() {
		return factory;
	}

	
	@Listener
	public void onStartUp(GameInitializationEvent e) {
		configload();
	}
	@Listener
	public void onPreInit(GamePreInitializationEvent event) {
		configload();
	}
	@Listener
	public void onGameInitializationEvent(GameInitializationEvent event) {
		plugin = this;
		SignLogger.getLogger().info("SignLogger is Enabled");
		configload();
	}

	public static File getlogDirectory() {
		return logDirectory;
	}

	public void configload() {
		if (!SignLogger.getlogDirectory().exists()) {
			SignLogger.getlogDirectory().mkdirs();
		}
		try {
			File logFile = new File(getlogDirectory(), "log.txt");
			if (!logFile.exists()) {
				logFile.createNewFile();
				logger.info("creating the pretty logs stuff..");
			}
			File configFile = new File(getlogDirectory(), "config.conf");
			if (!configFile.exists()) {
				configFile.createNewFile();
				logger.info("Also, creating the pretty config.");
			}

			ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
					.setFile(configFile).build();
			CommentedConfigurationNode config = loader.load(ConfigurationOptions.defaults()
					.setObjectMapperFactory(plugin.getFactory()).setShouldCopyDefaults(true));
			configoptions = config.getValue(TypeToken.of(Config.class), new Config());
			loader.save(config);
		} catch (Exception error) {
			getLogger().error("Couldn't create log/config", error);
			return;
		}
	}
	
	@Listener
	public void onChangeSignEvent(ChangeSignEvent event, @First Player player) throws IOException {

		SignData sign = event.getText();

		String playername = player.getName();
		World world = player.getWorld();
		String worldname = world.getName();
		int x = event.getTargetTile().getLocation().getBlockX();
		int y = event.getTargetTile().getLocation().getBlockY();
		int z = event.getTargetTile().getLocation().getBlockZ();
		Location<World> location = world.getLocation(x, y, z);
		//here log the files
		if (Config.LogFile) {
		logger(player, event, z, z, z);
		}
		// Logging stuff in console here :)
		if (Config.ConsoleLog ) {
		getLogger().info(playername.toString() + " placed a sign: " + " Line 1: " + "["
				+ sign.getListValue().get().get(0).toPlain() + "]" + " Line 2: " + "["
				+ sign.getListValue().get().get(1).toPlainSingle() + "]" + " Line 3: " + "["
				+ sign.getListValue().get().get(2).toPlain() + "]" + " Line 4: " + "["
				+ sign.getListValue().get().get(3).toPlain() + "] ");
		getLogger().info("Location: " + worldname + " " + x + " " + y + " " + z);
		}
		// Have the permission for ingame logging stuff there <3
		if (player.hasPermission("sign.logger")) {
			player.sendMessage(Text.of(playername.toString(), TextColors.RED, " placed a sign: ", TextColors.DARK_GREEN,
					"\n", "Line 1: ", TextColors.RED, "[" + sign.getListValue().get().get(0).toPlain() + "]",
					TextColors.DARK_GREEN, "\n", "Line 2: ", TextColors.RED, "[",
					sign.getListValue().get().get(1).toPlainSingle() + "]", TextColors.DARK_GREEN, "\n", "Line 3: ",
					TextColors.RED, "[", sign.getListValue().get().get(2).toPlain() + "]", TextColors.DARK_GREEN, "\n",
					"Line 4: ", TextColors.RED, "[", sign.getListValue().get().get(3).toPlain() + "] "));

			player.sendMessage(Text.of(
					Text.builder("[TP] ").color(TextColors.AQUA).style(TextStyles.BOLD)
							.onClick(TextActions.executeCallback(Teleport(player, location)))
							.onHover(TextActions.showText(Text.of("Click here to teleport to the sign."))),
					TextColors.RED, "Location: ", worldname, " ", x, " ", y, " ", z));
		}
	}
//I blame Karim it's his setup here .-.
	static void logger(Player player, ChangeSignEvent event, int x, int y, int z) throws IOException {
		Logf = new File(getlogDirectory(), "log.txt");
		BufferedWriter writer = new BufferedWriter(new FileWriter(Logf, true));
		SignData sign = event.getText();
		World world = player.getWorld();
		String worldname = world.getName();
		if (Logf.length() == 0) {

			writer.write(player.getName() + " placed a sign: " + " Line 1: " + "["
					+ sign.getListValue().get().get(0).toPlain() + "]" + " Line 2: " + "["
					+ sign.getListValue().get().get(1).toPlainSingle() + "]" + " Line 3: " + "["
					+ sign.getListValue().get().get(2).toPlain() + "]" + " Line 4: " + "["
					+ sign.getListValue().get().get(3).toPlain() + "] ");
			writer.write("Location: " + worldname + " " + x + " " + y + " " + z);
			writer.newLine();
		} else {
			writer.newLine();
			writer.write(player.getName() + " placed a sign: " + " Line 1: " + "["
					+ sign.getListValue().get().get(0).toPlain() + "]" + " Line 2: " + "["
					+ sign.getListValue().get().get(1).toPlainSingle() + "]" + " Line 3: " + "["
					+ sign.getListValue().get().get(2).toPlain() + "]" + " Line 4: " + "["
					+ sign.getListValue().get().get(3).toPlain() + "] ");
			writer.write("Location: " + worldname + " " + x + " " + y + " " + z);
		}
		writer.close();
	}

	private static Consumer<CommandSource> Teleport(Player player, Location<World> location) {
		return teleport -> player.setLocation(location);
	}
}
