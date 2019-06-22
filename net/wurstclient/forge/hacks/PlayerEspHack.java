/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.forge.events.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.compatibility.WVec3d;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.utils.EntityFakePlayer;
import net.wurstclient.forge.utils.RenderUtils;
import net.wurstclient.forge.utils.RotationUtils;

public final class PlayerEspHack extends Hack
{
	private final EnumSetting<Style> style =
		new EnumSetting<>("Style", Style.values(), Style.BOXES);
	private final CheckboxSetting filterSleeping = new CheckboxSetting(
		"Filter sleeping", "Won't show sleeping players.", false);
	private final CheckboxSetting filterInvisible = new CheckboxSetting(
		"Filter invisible", "Won't show invisible players.", false);
	
	private int playerBox;
	private final ArrayList<EntityPlayer> players = new ArrayList<>();
	
	public PlayerEspHack()
	{
		super("PlayerESP", "Highlights nearby players.");
		setCategory(Category.RENDER);
		addSetting(style);
		addSetting(filterSleeping);
		addSetting(filterInvisible);
	}
	
	@Override
	protected void onEnable()
	{
		MinecraftForge.EVENT_BUS.register(this);
		
		playerBox = GL11.glGenLists(1);
		GL11.glNewList(playerBox, GL11.GL_COMPILE);
		GL11.glBegin(GL11.GL_LINES);
		AxisAlignedBB bb = new AxisAlignedBB(-0.5, 0, -0.5, 0.5, 1, 0.5);
		RenderUtils.drawOutlinedBox(bb);
		GL11.glEnd();
		GL11.glEndList();
	}
	
	@Override
	protected void onDisable()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
		
		GL11.glDeleteLists(playerBox, 1);
		playerBox = 0;
	}
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
		EntityPlayerSP player = event.getPlayer();
		
		players.clear();
		Stream<EntityPlayer> stream = player.world.playerEntities.parallelStream()
			.filter(e -> !e.isDead && e.getHealth() > 0)
			.filter(e -> e != player)
			.filter(e -> !(e instanceof EntityFakePlayer))
			.filter(e -> Math.abs(e.posY - WMinecraft.getPlayer().posY) <= 1e6);
		
		if(filterSleeping.isChecked())
			stream = stream.filter(e -> !e.isPlayerSleeping());
		
		if(filterInvisible.isChecked())
			stream = stream.filter(e -> !e.isInvisible());
		
		players.addAll(stream.collect(Collectors.toList()));
	}

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event)
	{
		// GL settings
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glLineWidth(2);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		GL11.glPushMatrix();
		GL11.glTranslated(-TileEntityRendererDispatcher.staticPlayerX,
			-TileEntityRendererDispatcher.staticPlayerY,
			-TileEntityRendererDispatcher.staticPlayerZ);
		
		double partialTicks = event.getPartialTicks();
		
		if(style.getSelected().boxes)
			renderBoxes(partialTicks);
		
		if(style.getSelected().lines)
			renderLines(partialTicks);
		
		GL11.glPopMatrix();
		
		// GL resets
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}
	
	private void renderBoxes(double partialTicks)
	{
		for(EntityPlayer e : players)
		{
			GL11.glPushMatrix();
			GL11.glTranslated(e.prevPosX + (e.posX - e.prevPosX) * partialTicks,
				e.prevPosY + (e.posY - e.prevPosY) * partialTicks,
				e.prevPosZ + (e.posZ - e.prevPosZ) * partialTicks);
			GL11.glScaled(e.width + 0.1, e.height + 0.1, e.width + 0.1);
			
			float f = WMinecraft.getPlayer().getDistance(e) / 20F;
			GL11.glColor4f(2 - f, f, 0, 0.5F);
			
			GL11.glCallList(playerBox);
			
			GL11.glPopMatrix();
		}
	}
	
	private void renderLines(double partialTicks)
	{
		Vec3d start = RotationUtils.getClientLookVec()
			.addVector(0, WMinecraft.getPlayer().getEyeHeight(), 0)
			.addVector(TileEntityRendererDispatcher.staticPlayerX,
				TileEntityRendererDispatcher.staticPlayerY,
				TileEntityRendererDispatcher.staticPlayerZ);
		
		GL11.glBegin(GL11.GL_LINES);
		for(EntityPlayer e : players)
		{
			Vec3d end = e.getEntityBoundingBox().getCenter()
				.subtract(new Vec3d(e.posX, e.posY, e.posZ)
					.subtract(e.prevPosX, e.prevPosY, e.prevPosZ)
					.scale(1 - partialTicks));
			
			float f = WMinecraft.getPlayer().getDistance(e) / 20F;
			GL11.glColor4f(2 - f, f, 0, 0.5F);
			
			GL11.glVertex3d(WVec3d.getX(start), WVec3d.getY(start),
				WVec3d.getZ(start));
			GL11.glVertex3d(WVec3d.getX(end), WVec3d.getY(end),
				WVec3d.getZ(end));
		}
		GL11.glEnd();
	}
	
	private enum Style
	{
		BOXES("Boxes only", true, false),
		LINES("Lines only", false, true),
		LINES_AND_BOXES("Lines and boxes", true, true);
		
		private final String name;
		private final boolean boxes;
		private final boolean lines;
		
		private Style(String name, boolean boxes, boolean lines)
		{
			this.name = name;
			this.boxes = boxes;
			this.lines = lines;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
