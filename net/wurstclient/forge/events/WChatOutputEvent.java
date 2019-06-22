/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.events;

import com.google.common.base.Strings;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public final class WChatOutputEvent extends Event
{
	private String message;
	private final String originalMessage;
	
	public WChatOutputEvent(String message)
	{
		setMessage(message);
		originalMessage = Strings.nullToEmpty(message);
	}
	
	public String getMessage()
	{
		return message;
	}
	
	public void setMessage(String message)
	{
		this.message = Strings.nullToEmpty(message);
	}
	
	public String getOriginalMessage()
	{
		return originalMessage;
	}
}
