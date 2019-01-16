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

import com.dizzyd.coregen.config.Deposit;
import com.dizzyd.coregen.feature.ScriptFeature;
import com.dizzyd.coregen.util.WeightedBlockList;
import com.dizzyd.coregen.ylevel.*;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import net.minecraft.init.Blocks;
import net.minecraftforge.event.terraingen.OreGenEvent;

import java.io.File;
import java.util.*;

public class Config {

    static com.typesafe.config.Config defaultDeposit;
    static com.typesafe.config.Config defaultRoot;
    static com.typesafe.config.Config defaultYLevels;

    static com.typesafe.config.Config defaultScriptFeature;

    static {
        HashMap<String, Object> defaults = new HashMap<String, Object>();
        defaults.put("enabled", true);
        defaults.put("restrictions.min-deposit-distance", 0);
        defaults.put("restrictions.biomes", new ArrayList<String>());
        defaults.put("restrictions.dimensions", Arrays.asList(0));
        defaultDeposit = ConfigFactory.parseMap(defaults);

        defaults = new HashMap<String, Object>();
        defaults.put("deposits", new ArrayList<ConfigValue>());
        defaults.put("disableDefaultGeneration", Arrays.asList("COAL", "DIAMOND", "GOLD", "IRON", "LAPIS", "REDSTONE"));
        defaultRoot = ConfigFactory.parseMap(defaults);

        defaults = new HashMap<String, Object>();
        defaults.put("liquids", Arrays.asList(Blocks.WATER.getRegistryName().toString()));
        defaults.put("max", 128);
        defaults.put("min", 60);
        defaultYLevels = ConfigFactory.parseMap(defaults);

        defaults = new HashMap<String, Object>();
        defaults.put("targets", new ArrayList<String>());
        defaults.put("sparse", false);
        defaultScriptFeature = ConfigFactory.parseMap(defaults);
    }

    private Map<String, Deposit> deposits = new HashMap<String, Deposit>();
    private Set<OreGenEvent.GenerateMinable.EventType> disabledOreGen = new HashSet<>();

    public Config(File configDir) {
        com.typesafe.config.Config cfg = ConfigFactory.parseFile(new File(configDir, "coregen.conf"));
        cfg = cfg.withFallback(defaultRoot);

        // Load list of disabled ore gen events
        disabledOreGen.addAll(cfg.getEnumList(OreGenEvent.GenerateMinable.EventType.class, "disableDefaultGeneration"));

        for (ConfigValue v : cfg.getList("deposits")) {

            // Construct base deposit object from config
            com.typesafe.config.Config depositCfg = ((ConfigObject)v).toConfig();
            Deposit deposit = ConfigBeanFactory.create(depositCfg.withFallback(defaultDeposit), Deposit.class);
            deposits.put(deposit.getId(), deposit);

            // Walk over the features, turning them into specific instances of Feature and adding to the deposit
            for (com.typesafe.config.Config featureCfg : depositCfg.getConfigList("features")) {
                // Get the list of blocks to generate and turn it into a weighted list
                WeightedBlockList blocks = getWeightedBlocks(featureCfg);

                // Get the y-level controller
                YLevelDistribution dist = getYLevelDist(featureCfg.getConfig("y-level"));

                switch (featureCfg.getString("type")) {
                    case "script":
                        featureCfg = featureCfg.withFallback(defaultScriptFeature);
                        ScriptFeature script = ConfigBeanFactory.create(featureCfg, ScriptFeature.class);
                        script.init(featureCfg, blocks, dist);
                        deposit.addFeature(script);
                        break;
                }
            }

            // Make sure restrictions have had a chance to initialize
            deposit.getRestrictions().prepare();
        }
    }

    public Map<String, Deposit> getDeposits() {
        return deposits;
    }

    public boolean isDefaultOreDisabled(OreGenEvent.GenerateMinable.EventType e) {
        return disabledOreGen.contains(e);
    }

    private WeightedBlockList getWeightedBlocks(com.typesafe.config.Config cfg) {
        WeightedBlockList result = new WeightedBlockList();

        // There are three possible formats for the list of blocks:
        // 1. String - there is only a single block to generate and we are provided the ID
        // 2. List<String> - there are an equally weighted number of blocks to generate and we are provided the IDs
        // 3. Map<String, Integer> - there are a series of blocks to generate, each with a weight associated
        ConfigValue blocksValue = cfg.getValue("blocks");
        switch (blocksValue.valueType()) {
            case STRING:
                result.setBlock((String)blocksValue.unwrapped());
                break;
            case LIST:
                result.setBlocks((List<String>)blocksValue.unwrapped());
                break;
            default: // OBJECT
                result.setBlocks(((ConfigObject)blocksValue).unwrapped());
        }

        return result;
    }

    private YLevelDistribution getYLevelDist(com.typesafe.config.Config cfg) {
        cfg = cfg.withFallback(defaultYLevels);
        switch(cfg.getString("type")) {
            case "gaussian":
                return ConfigBeanFactory.create(cfg, YLevelGaussian.class);
            case "uniform":
                return ConfigBeanFactory.create(cfg, YLevelUniform.class);
            case "surface":
                return ConfigBeanFactory.create(cfg, YLevelSurface.class);
            case "underwater":
                return ConfigBeanFactory.create(cfg, YLevelUnderwater.class);
        }
        return null;
    }
}
