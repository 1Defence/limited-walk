package com.limitedwalk;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class LimitedWalkPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(LimitedWalkPlugin.class);
		RuneLite.main(args);
	}
}