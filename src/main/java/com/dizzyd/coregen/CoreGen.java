// ***************************************************************************
//
//  Copyright 2018-2019 David (Dizzy) Smith, dizzyd@dizzyd.com
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
// ***************************************************************************

package com.dizzyd.coregen;

import com.dizzyd.coregen.util.ScriptUtil;
import com.dizzyd.coregen.world.WorldGen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;

@Mod(modid = CoreGen.MODID, name = CoreGen.NAME, version = CoreGen.VERSION)
public class CoreGen
{
    public static final String MODID = "coregen";
    public static final String NAME = "CoreGen";
    public static final String VERSION = "1.0.0";

    public static Logger logger;
    public static Config config;
    public static ScriptUtil scriptUtil;
    public static WorldGen worldGen;

    private static File configDirectory;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        scriptUtil = new ScriptUtil();
        configDirectory = event.getModConfigurationDirectory();
        config = new Config(configDirectory);
        worldGen = new WorldGen();

        installScripts(false);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.ORE_GEN_BUS.register(this);
        GameRegistry.registerWorldGenerator(worldGen, 1000);
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new Command());
    }

    @SubscribeEvent
    public void onGenerateMineable(OreGenEvent.GenerateMinable event) {
        OreGenEvent.GenerateMinable.EventType type = event.getType();
        switch (event.getType()) {
            case COAL:
            case DIAMOND:
            case GOLD:
            case IRON:
            case LAPIS:
            case REDSTONE:
            case QUARTZ:
            case EMERALD:
                event.setResult(Event.Result.DENY);
            default:
                break;
        }
    }

    public static void reloadConfig() {
        config = new Config(configDirectory);
    }

    static public void installScripts(boolean force) {
        // Create the coregen directory which will hold our scripts
        File scriptDir = new File(configDirectory.getParentFile(), "coregen");
        scriptDir.mkdirs();

        // For each of the directories in the classpath; copy into the scriptDir
        // IIF it doesn't exist
        try {
            Enumeration<URL> scripts = CoreGen.class.getClassLoader().getResources("scripts");
            if (scripts.hasMoreElements()) {
                File scriptResources = new File(scripts.nextElement().toURI());
                for (File f : scriptResources.listFiles()) {
                    File target = new File(scriptDir, f.getName());
                    if (force || !target.exists()) {
                        FileUtils.copyFile(f, target);
                        logger.info("Installed script {}", target.toString());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
