/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.compatibility;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.wurstclient.forge.events.*;
import net.wurstclient.forge.events.WUpdateEvent;

public class WEventFactory
{
	@SubscribeEvent
	public void onPlayerPreTick(TickEvent.PlayerTickEvent event)
	{
		if(event.phase != Phase.START)
			return;
		
		EntityPlayer player = event.player;
		if(player != WMinecraft.getPlayer())
			return;
		
		if(!player.world.isRemote)
			return;
		
		MinecraftForge.EVENT_BUS.post(new WUpdateEvent((EntityPlayerSP)player));
	}

	@SubscribeEvent
	public void onClientSentMessage(ClientChatEvent event)
	{
		WChatOutputEvent event2 =
				new WChatOutputEvent(event.getOriginalMessage());
		if(MinecraftForge.EVENT_BUS.post(event2))
			event.setCanceled(true);
		event.setMessage(event2.getMessage());
	}
}
