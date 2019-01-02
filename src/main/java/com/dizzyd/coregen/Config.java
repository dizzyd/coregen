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
import com.dizzyd.coregen.feature.ClusterFeature;
import com.dizzyd.coregen.feature.ScriptFeature;
import com.dizzyd.coregen.util.WeightedBlockList;
import com.dizzyd.coregen.ylevel.YLevelDistribution;
import com.dizzyd.coregen.ylevel.YLevelGaussian;
import com.dizzyd.coregen.ylevel.YLevelUniform;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

    static com.typesafe.config.Config defaultDeposit;
    static com.typesafe.config.Config defaultRoot;

    static {
        HashMap<String, Object> defaults = new HashMap<String, Object>();
        defaults.put("enabled", true);
        defaults.put("restrictions.min-deposit-distance", 0);
        defaults.put("restrictions.biomes", new ArrayList<String>());
        defaults.put("restrictions.dimensions", new ArrayList<Integer>());
        defaultDeposit = ConfigFactory.parseMap(defaults);

        defaults = new HashMap<String, Object>();
        defaults.put("deposits", new ArrayList<ConfigValue>());
        defaultRoot = ConfigFactory.parseMap(defaults);
    }

    private Map<String, Deposit> deposits = new HashMap<String, Deposit>();

    public Config(File configDir) {
        com.typesafe.config.Config cfg = ConfigFactory.parseFile(new File(configDir, "coregen.conf"));
        cfg = cfg.withFallback(defaultRoot);
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
                        ScriptFeature script = ConfigBeanFactory.create(featureCfg, ScriptFeature.class);
                        script.init(featureCfg, blocks, dist);
                        deposit.addFeature(script);
                        break;
                    case "cluster":
                        ClusterFeature cluster = ConfigBeanFactory.create(featureCfg, ClusterFeature.class);
                        cluster.init(featureCfg, blocks, dist);
                        deposit.addFeature(cluster);
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
        switch(cfg.getString("type")) {
            case "gaussian":
                return ConfigBeanFactory.create(cfg, YLevelGaussian.class);
            case "uniform":
                return ConfigBeanFactory.create(cfg, YLevelUniform.class);
        }
        return null;
    }
}
