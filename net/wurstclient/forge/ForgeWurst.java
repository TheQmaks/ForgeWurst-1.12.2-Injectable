/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.forge.clickgui.ClickGui;
import net.wurstclient.forge.compatibility.WEventFactory;

//@Mod(modid = ForgeWurst.MODID)
public final class ForgeWurst
{
	public static final String MODID = "forgewurst";
	public static final String VERSION = "0.11";
	
//	@Instance(MODID)
	private static ForgeWurst forgeWurst;
	
	private boolean obfuscated;
	
	private Path configFolder;
	
	private HackList hax;
	private CommandList cmds;
	private KeybindList keybinds;
	private ClickGui gui;
	
	private IngameHUD hud;
	private CommandProcessor cmdProcessor;
	private KeybindProcessor keybindProcessor;

	private Method register;
	
//	@EventHandler
//	public void init(FMLInitializationEvent event)
//	{
//		init();
//	}

	public ForgeWurst() {
		forgeWurst = this;
		init();
	}

	public void init() {
		try {
			register = EventBus.class.getDeclaredMethod("register", Class.class, Object.class, Method.class, ModContainer.class);
			register.setAccessible(true);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		String mcClassName = Minecraft.class.getName().replace(".", "/");
		FMLDeobfuscatingRemapper remapper = FMLDeobfuscatingRemapper.INSTANCE;
		obfuscated = !mcClassName.equals(remapper.unmap(mcClassName));

		configFolder =
				Minecraft.getMinecraft().mcDataDir.toPath().resolve("wurst");
		try
		{
			Files.createDirectories(configFolder);
		}catch(IOException e)
		{
			throw new RuntimeException(e);
		}

		hax = new HackList(configFolder.resolve("enabled-hacks.json"),
				configFolder.resolve("settings.json"));
		hax.loadEnabledHacks();
		hax.loadSettings();

		cmds = new CommandList();

		keybinds = new KeybindList(configFolder.resolve("keybinds.json"));
		keybinds.init();

		gui = new ClickGui(configFolder.resolve("windows.json"));
		gui.init(hax);

		hud = new IngameHUD(hax, gui);
		register(hud);

		cmdProcessor = new CommandProcessor(cmds);
		register(cmdProcessor);

		keybindProcessor = new KeybindProcessor(hax, keybinds, cmdProcessor);
		register(keybindProcessor);

		register(keybindProcessor);

		register(new WEventFactory());
	}

	public static ForgeWurst getForgeWurst()
	{
		return forgeWurst;
	}
	
	public boolean isObfuscated()
	{
		return obfuscated;
	}
	
	public HackList getHax()
	{
		return hax;
	}
	
	public CommandList getCmds()
	{
		return cmds;
	}
	
	public KeybindList getKeybinds()
	{
		return keybinds;
	}
	
	public ClickGui getGui()
	{
		return gui;
	}

	public void register(Object target) {
		boolean isStatic = target.getClass() == Class.class;
		@SuppressWarnings("unchecked")
		Set<? extends Class<?>> supers = isStatic ? Sets.newHashSet((Class<?>)target) : TypeToken.of(target.getClass()).getTypes().rawTypes();
		for (Method method : (isStatic ? (Class<?>)target : target.getClass()).getMethods())
		{
			if (isStatic && !Modifier.isStatic(method.getModifiers()))
				continue;
			else if (!isStatic && Modifier.isStatic(method.getModifiers()))
				continue;

			for (Class<?> cls : supers)
			{
				try
				{
					Method real = cls.getDeclaredMethod(method.getName(), method.getParameterTypes());
					if (real.isAnnotationPresent(SubscribeEvent.class))
					{
						Class<?>[] parameterTypes = method.getParameterTypes();
						if (parameterTypes.length != 1)
						{
							throw new IllegalArgumentException(
									"Method " + method + " has @SubscribeEvent annotation, but requires " + parameterTypes.length +
											" arguments.  Event handler methods must require a single argument."
							);
						}

						Class<?> eventType = parameterTypes[0];

						if (!Event.class.isAssignableFrom(eventType))
						{
							throw new IllegalArgumentException("Method " + method + " has @SubscribeEvent annotation, but takes a argument that is not an Event " + eventType);
						}

						try {
							register.invoke(MinecraftForge.EVENT_BUS, eventType, target, real, Loader.instance().getMinecraftModContainer());
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						}
						break;
					}
				}
				catch (NoSuchMethodException e)
				{
					; // Eat the error, this is not unexpected
				}
			}
		}
	}
}
