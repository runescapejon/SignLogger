package me.runescapejon.signlogger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class Upload implements CommandExecutor {

	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (src instanceof Player) {
			Player p = (Player) src;
			if (p.hasPermission("signlogger.upload")) {
				File log = new File(SignLogger.getlogDirectory(), "log.txt");
				try {
					String url = getPastie(readfiles(log));
					p.sendMessage(Text.of(Text.builder("Uploaded log: " + url).color(TextColors.RED)
							.onClick(TextActions.openUrl(new URL(url)))));
				} catch (IOException e) {
					p.sendMessage(Text.of(TextColors.RED, "Oh no something went wrong check console for more detail."));
					e.printStackTrace();
				}
			}
			return CommandResult.success();
		} else if (src instanceof ConsoleSource) {
			ConsoleSource c = (ConsoleSource) src;
			try {
				File log = new File(SignLogger.getlogDirectory(), "log.txt");
				c.sendMessage(Text.of(TextColors.RED, "Uploaded log: ", TextColors.AQUA, getPastie(readfiles(log))));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return CommandResult.success();
		}
		return CommandResult.success();
	}

	public static String getPastie(String string) {
		HttpsURLConnection connection = null;
		try {
			connection = (HttpsURLConnection) new URL("https://hastebin.com/documents").openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/53.0.2785.143 Chrome/53.0.2785.143 Safari/537.36");
		    connection.setDoInput(true);
		    connection.setDoOutput(true);
			connection.setRequestMethod("POST");
		 
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		try (DataOutputStream writer = new DataOutputStream(connection.getOutputStream())) {
			writer.write(string.getBytes(StandardCharsets.UTF_8));
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		StringBuilder response = new StringBuilder();
		try (BufferedReader read = new BufferedReader(
				new InputStreamReader(connection.getInputStream()))) {
			String inputLine;
			while ((inputLine = read.readLine()) != null) {
				response.append(inputLine);
			}
		} catch (IOException error) {
			error.printStackTrace();
		}

		JsonElement json = new JsonParser().parse(response.toString());
		return "https://hastebin.com/" + json.getAsJsonObject().get("key").getAsString();
	}

	private static String readfiles(File file) throws IOException {
		List<String> contents = new ArrayList<String>();
		FileReader FileReader = new FileReader(file);
		BufferedReader BufferedReader = new BufferedReader(FileReader);
		String line;
		while ((line = BufferedReader.readLine()) != null) {
			contents.add(line);
		}
		BufferedReader.close();
		FileReader.close();
		return String.join("\n", contents);
	}
}
