/*
 * Copyright (c) 2026, 1Defence https://github.com/1Defence
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.limitedwalk;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Range;

import java.awt.*;

@ConfigGroup("limitedwalk")
public interface LimitedWalkConfig extends Config
{
	String CONFIG_GROUP = "limitedwalk";
	String REGION_PREFIX = "region_";

	@ConfigItem(
			keyName = "disableHotkey",
			name = "Disable Hotkey",
			description = "Temporarily disables plugin functionality.",
			position = 1
	)
	default Keybind hotkeyToggle() { return Keybind.SHIFT; }

	@ConfigItem(
			keyName = "clickUnder",
			name = "Click Under",
			description = "Allows clicking under yourself if on a limited tile.",
			position = 2
	)
	default boolean clickUnder() { return false; }

	@ConfigItem(
			keyName = "allowTraversal",
			name = "Allow Traversal",
			description = "Allows clicking other limited tiles, while on a limited tile (can endure stale-frame issue)",
			position = 3
	)
	default boolean allowTraversal() { return false; }

	@Alpha
	@ConfigItem(
			position = 4,
			keyName = "tileOutlineColor",
			name = "Tile outline color",
			description = "Outline color of the limited tile (0 opacity to disable)"
	)
	default Color tileOutlineColor()
	{
		return new Color(255,122,0,122);
	}

	@Alpha
	@ConfigItem(
			position = 5,
			keyName = "tileFillColor",
			name = "Tile fill color",
			description = "Fill color of the limited tile (0 opacity to disable)"
	)
	default Color tileFillColor()
	{
		return new Color(255,255,255,25);
	}
	@Range(min = 0, max = 8)
	@ConfigItem(
			position = 6,
			keyName = "borderWidth",
			name = "Border Width",
			description = "Width of the limited tile border"
	)
	default double borderWidth()
	{
		return 2;
	}
}
