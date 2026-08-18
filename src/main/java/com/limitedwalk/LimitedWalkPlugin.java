package com.limitedwalk;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
        name = "Limited Walk",
        description = "Forces shift click to walk when on user-set tiles.",
        tags = {"walk", "limited", "alting"}
)
public class LimitedWalkPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private LimitedWalkConfig config;

    @Override
    protected void startUp() throws Exception
    {

    }

    @Override
    protected void shutDown() throws Exception
    {

    }

    @Provides
	LimitedWalkConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(LimitedWalkConfig.class);
    }
}
