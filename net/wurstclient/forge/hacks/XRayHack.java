/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.init.Blocks;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.compatibility.WForgeBlockModelRenderer;
import net.wurstclient.forge.settings.BlockListSetting;

public final class XRayHack extends Hack {
    public static final BlockListSetting blocks = new BlockListSetting("Blocks", Blocks.COAL_ORE, Blocks.IRON_ORE, Blocks.GOLD_ORE, Blocks.DIAMOND_ORE);

    public XRayHack() {
        super("X-Ray", "Allows you to see ores through walls.");
        setCategory(Category.RENDER);
        addSetting(blocks);

        try {
            BlockRendererDispatcher renderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
            if(renderer.getClass().getName().equals("codechicken.lib.render.block.CCBlockRendererDispatcher")) {
                Field parentDispatcher = renderer.getClass().getDeclaredField("parentDispatcher");
                parentDispatcher.setAccessible(true);
                renderer = (BlockRendererDispatcher) parentDispatcher.get(renderer);
            }
            Field blockModelRenderer = renderer.getClass().getDeclaredField(ForgeWurst.getForgeWurst().isObfuscated() ? "field_175027_c" : "blockModelRenderer");
            blockModelRenderer.setAccessible(true);
            blockModelRenderer.set(renderer, new WForgeBlockModelRenderer(mc.getBlockColors()));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getRenderName() {
        return "X-Wurst";
    }

    @Override
    protected void onEnable() {
        mc.renderGlobal.loadRenderers();
    }

    @Override
    protected void onDisable() {
        mc.renderGlobal.loadRenderers();
    }
}
