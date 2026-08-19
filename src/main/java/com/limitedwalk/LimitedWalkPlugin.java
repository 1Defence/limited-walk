/*
 * Copyright (c) 2026, 1Defence https://github.com/1Defence
 * Copyright (c) 2022, WhatATopic <https://github.com/WhatATopic>
 * Copyright (c) 2018, TheLonelyDev <https://github.com/TheLonelyDev>
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
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

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.Tile;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    @Inject
    private ConfigManager configManager;

    @Inject
    private Gson gson;

    @Inject
    private KeyManager keyManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private TileOverlay overlay;

    boolean limited = true;

    boolean configClickUnder,configAllowTraversal;

    @Getter(AccessLevel.PACKAGE)
    private final List<WorldPoint> limitedPoints = new ArrayList<>();

    private final HotkeyListener hotkeyListener = new HotkeyListener(() -> config.hotkeyToggle())
    {
        @Override
        public void hotkeyPressed()
        {
            limited = false;
        }

        @Override
        public void hotkeyReleased()
        {
            limited = true;
        }
    };

    @Override
    protected void startUp() throws Exception
    {
        CacheConfigs();
        limited = true;
        keyManager.registerKeyListener(hotkeyListener);
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception
    {
        keyManager.unregisterKeyListener(hotkeyListener);
        overlayManager.remove(overlay);
    }

    public void CacheConfigs(){
        configClickUnder = config.clickUnder();
        configAllowTraversal = config.allowTraversal();
        overlay.updateConfigs();
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged configChanged)
    {
        if (!configChanged.getGroup().equals(LimitedWalkConfig.CONFIG_GROUP))
        {
            return;
        }

        CacheConfigs();
    }

    @Provides
	LimitedWalkConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(LimitedWalkConfig.class);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged)
    {
        if (gameStateChanged.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }
        loadTiles();
    }

    void saveTiles(int regionId, Collection<LimitedTile> tiles)
    {
        if (tiles == null || tiles.isEmpty())
        {
            configManager.unsetConfiguration(LimitedWalkConfig.CONFIG_GROUP, LimitedWalkConfig.REGION_PREFIX + regionId);
            return;
        }

        String json = gson.toJson(tiles);
        configManager.setConfiguration(LimitedWalkConfig.CONFIG_GROUP, LimitedWalkConfig.REGION_PREFIX + regionId, json);
    }

    void loadTiles()
    {
        limitedPoints.clear();

        int[] regions = client.getMapRegions();

        if (regions == null)
        {
            return;
        }

        for (int regionId : regions)
        {
            Collection<LimitedTile> regionTiles = getLimitedTiles(regionId);
            Collection<WorldPoint> points = translateToWorldPoint(regionTiles);
            limitedPoints.addAll(points);
        }
    }

    private Collection<WorldPoint> translateToWorldPoint(Collection<LimitedTile> tiles)
    {
        if (tiles.isEmpty())
        {
            return Collections.emptyList();
        }

        return tiles.stream()
                .map(tile -> WorldPoint.fromRegion(tile.getRegionId(), tile.getRegionX(), tile.getRegionY(), tile.getZ()))
                .flatMap(wp ->
                {
                    final Collection<WorldPoint> localWorldPoints = WorldPoint.toLocalInstance(client, wp);
                    return localWorldPoints.stream();
                })
                .collect(Collectors.toList());
    }


    Collection<LimitedTile> getLimitedTiles(int regionId)
    {
        String json = configManager.getConfiguration(LimitedWalkConfig.CONFIG_GROUP, LimitedWalkConfig.REGION_PREFIX + regionId);
        if (Strings.isNullOrEmpty(json))
        {
            return Collections.emptyList();
        }
        return gson.fromJson(json, new TypeToken<List<LimitedTile>>(){}.getType());
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        if (!limited && event.getOption().equals("Walk here"))
        {
            final Tile selectedSceneTile = client.getSelectedSceneTile();

            if (selectedSceneTile == null)
            {
                return;
            }

            final WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, selectedSceneTile.getLocalLocation());
            final int regionId = worldPoint.getRegionID();
            final LimitedTile limitedTile = new LimitedTile(regionId, worldPoint.getRegionX(), worldPoint.getRegionY(), worldPoint.getPlane());
            final boolean exists = getLimitedTiles(regionId).contains(limitedTile);

            client.createMenuEntry(-1)
                    .setOption(exists ? "Remove Limit" : "Limit walk here")
                    .setTarget(event.getTarget())
                    .setType(MenuAction.RUNELITE)
                    .onClick(e ->
                    {
                        Tile target = client.getSelectedSceneTile();
                        if (target != null)
                        {
                            toggleTile(target);
                        }
                    });
        }
    }
    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (!limited) {
            return;
        }

        if (event.getMenuOption().contains("Walk here"))
        {
            final Tile tile = client.getSelectedSceneTile();
            if (tile == null)
            {
                return;
            }

            WorldPoint localPlayerWorldPoint = client.getLocalPlayer().getWorldLocation();

            if(localPlayerWorldPoint == null)
                return;

            WorldView wv = client.getTopLevelWorldView();
            if(wv == null)
                return;

            LocalPoint playerLP = LocalPoint.fromWorld(wv,localPlayerWorldPoint);
            if(playerLP == null)
                return;

            final WorldPoint localPlayerPointInstance = WorldPoint.fromLocalInstance(client, playerLP);

            final int regionId = localPlayerPointInstance.getRegionID();
            final LimitedTile playerTile = new LimitedTile(regionId, localPlayerPointInstance.getRegionX(), localPlayerPointInstance.getRegionY(), localPlayerPointInstance.getPlane());

            Collection<LimitedTile> limitedTiles = getLimitedTiles(regionId);
            if (limitedTiles.contains(playerTile))
            {
                //player is standing on a limited tile.
                final WorldPoint clickedWorldPoint = WorldPoint.fromLocalInstance(client, tile.getLocalLocation());
                final LimitedTile clickedTile = new LimitedTile(regionId, clickedWorldPoint.getRegionX(), clickedWorldPoint.getRegionY(), clickedWorldPoint.getPlane());

                boolean clickedUnder = clickedTile.equals(playerTile);

                if(clickedUnder){
                    //allow walk here if tile player presently on
                    if(configClickUnder)
                        return;
                }else{
                    //allow walk here if it's another limited tile only.
                    if(configAllowTraversal && limitedTiles.contains(clickedTile))
                        return;
                }

                //prevent the event
                event.consume();
            }
        }
    }


    private void toggleTile(Tile tile)
    {
        if (tile == null)
        {
            return;
        }

        final WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, tile.getLocalLocation());
        final int regionId = worldPoint.getRegionID();
        final LimitedTile limitedTile = new LimitedTile(regionId, worldPoint.getRegionX(), worldPoint.getRegionY(), worldPoint.getPlane());
        List<LimitedTile> tiles = new ArrayList<>(getLimitedTiles(regionId));
        if (tiles.contains(limitedTile))
        {
            tiles.remove(limitedTile);
        }
        else
        {
            tiles.add(limitedTile);
        }

        saveTiles(regionId, tiles);

        loadTiles();
    }

}
