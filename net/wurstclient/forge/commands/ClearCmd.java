/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.commands;

import net.minecraft.client.Minecraft;
import net.wurstclient.forge.Command;

public final class ClearCmd extends Command
{
	public ClearCmd()
	{
		super("clear", "Clears the chat completely.", "Syntax: .clear");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length > 0)
			throw new CmdSyntaxError();

		Minecraft.getMinecraft().ingameGUI.getChatGUI().clearChatMessages(true);
	}
}
