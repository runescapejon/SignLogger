package me.runescapejon.signlogger;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class Config {
	@Setting(value = "Console-Log", comment = "Default is true, but turning this false will prevent signs from being log in console.")
	public static boolean ConsoleLog = true;

	@Setting(value = "Log-File", comment = "Default is true, but turning this false will prevent signs from being log in log.txt file.")
	public static boolean LogFile = true;

}