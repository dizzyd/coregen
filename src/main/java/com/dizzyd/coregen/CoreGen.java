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
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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

        // Add nashorn to the default class loader exclusion list so it doesn't
        // get mangled when trying to load the class (since it's part of the JDK)
        Launch.classLoader.addClassLoaderExclusion("jdk.nashorn.");
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.ORE_GEN_BUS.register(this);

        worldGen = new WorldGen();
        GameRegistry.registerWorldGenerator(worldGen, 1000);
    }

    @EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        installScripts(false);

        // Load the config AFTER we have installed included scripts
        config = new Config(configDirectory);
        System.out.println(config);
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new Command());
    }

    @SubscribeEvent
    public void onGenerateMineable(OreGenEvent.GenerateMinable event) {
        if (config.isDefaultOreDisabled(event.getType())) {
            event.setResult(Event.Result.DENY);
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
            for (URL u : listResources("scripts")) {
                String path = u.getPath();
                String targetName = path.substring(path.lastIndexOf('/') + 1);
                File target = new File(scriptDir, targetName);
                if (force || !target.exists()) {
                    FileUtils.copyURLToFile(u, target);
                    logger.info("Installed script {}", target.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private static List<URL> listResources(String path) throws IOException, URISyntaxException {
        List<URL> resources = new ArrayList<>();

        // Get the resource url via class loader
        URL dirUrl = CoreGen.class.getClassLoader().getResource(path);
        if (dirUrl == null) {
            // Couldn't be found, bail
            return null;
        }

        // If it's already in file form, life is easy...
        if (dirUrl.getProtocol().equals("file")) {
            for (File f : new File(dirUrl.toURI()).listFiles()) {
                resources.add(f.toURI().toURL());
            }
            return resources;
        }

        // It's in a JAR, we'll need to open the JAR and scan it to find matching paths
        if (dirUrl.getProtocol().equals("jar")) {

            // Pick out the filename from the original URL
            String filename = dirUrl.getPath().substring(5, dirUrl.getPath().indexOf("!"));
            JarFile jar = new JarFile(filename);

            // Traverse all the non-directory entries
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry curr = entries.nextElement();
                if (curr.getName().startsWith(path) && !curr.isDirectory()) {
                    resources.add(CoreGen.class.getClassLoader().getResource(curr.getName()));
                }
            }
            return resources;
        }

        return null;
    }
}
