/*
 * MCreator (https://mcreator.net/)
 * Copyright (C) 2020 Pylo and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.mcreator;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import net.mcreator.io.LoggingSystem;
import net.mcreator.io.OS;
import net.mcreator.io.UserFolderManager;
import net.mcreator.preferences.PreferencesManager;
import net.mcreator.ui.MCreatorApplication;
import net.mcreator.ui.blockly.WebConsoleListener;
import net.mcreator.ui.component.util.ThreadUtil;
import net.mcreator.util.MCreatorVersionNumber;
import net.mcreator.util.TerribleModuleHacks;
import net.mcreator.util.UTF8Forcer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

public class Launcher {

	public static MCreatorVersionNumber version;

	public static void main(String[] args) {
		LoggingSystem.init();

		TerribleModuleHacks.openAllFor(ClassLoader.getSystemClassLoader().getUnnamedModule());
		TerribleModuleHacks.openMCreatorRequirements();

		UTF8Forcer.forceGlobalUTF8();

		final Logger LOG = LogManager.getLogger("Launcher"); // init logger after log directory is set

		try {
			Properties conf = new Properties();
			conf.load(Launcher.class.getResourceAsStream("/esmmaker.conf"));
			version = new MCreatorVersionNumber(conf);
		} catch (IOException e) {
			LOG.error("Failed to read MCreator config", e);
		}

		LOG.info("Starting ESMMaker {}", version);

		// print version of Java
		LOG.info("Java version: {}, VM: {}, vendor: {}", System.getProperty("java.version"),
				System.getProperty("java.vm.name"), System.getProperty("java.vendor"));
		LOG.info("Current JAVA_HOME for running instance: {}", System.getProperty("java.home"));

		// after we have libraries loaded, we load preferences
		PreferencesManager.init();

		// set system properties from preferences
		System.setProperty("apple.laf.useScreenMenuBar",
				Boolean.toString(PreferencesManager.PREFERENCES.ui.usemacOSMenuBar.get()));
		System.setProperty("sun.java2d.opengl",
				Boolean.toString(PreferencesManager.PREFERENCES.ui.use2DAcceleration.get()));
		System.setProperty("sun.java2d.d3d", "false");

		// Init JFX Toolkit
		ThreadUtil.runOnSwingThreadAndWait(JFXPanel::new);
		Platform.setImplicitExit(false);

		WebConsoleListener.registerLogger(LOG);

		// check if proper version of MCreator per architecture is used
		if (OS.getSystemBits() == OS.BIT32) {
			JOptionPane.showMessageDialog(null, "<html>You are trying to run 64-bit ESMMaker on 32-bit computer.<br>"
					+ "ESMMaker no longer supports 32-bit platforms.", "ESMMaker error", JOptionPane.WARNING_MESSAGE);
			System.exit(-1);
		}

		LOG.info("Installation path: {}", System.getProperty("user.dir"));
		LOG.info("User home of ESMMaker: {}", UserFolderManager.getFileFromUserFolder("/"));

		if (!UserFolderManager.createUserFolderIfNotExists()) {
			JOptionPane.showMessageDialog(null, "<html><b>ESMMaker failed to write to user directory!</b><br><br>"
							+ "Make sure that the user running ESMMaker has permissions to read and write to the directory<br>"
							+ "in which ESMMaker tried to create user specific data storage. The path ESMMaker could not write to is:<br><br>"
							+ UserFolderManager.getFileFromUserFolder("/") + "<br>", "ESMMaker file system error",
					JOptionPane.WARNING_MESSAGE);
			System.exit(-2);
		}

		MCreatorApplication.createApplication(Arrays.asList(args));
	}

}
