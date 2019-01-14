package com.dizzyd.coregen.scripting;


import com.dizzyd.coregen.feature.Feature;
import com.typesafe.config.Config;
import net.minecraft.block.state.IBlockState;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public interface Context {
    Feature getFeature();

    Random getRandom();

    Config getConfig();

    Logger getLogger();

    Position randomPos(int cx, int cz);

    void placeBlock(double x, double y, double z);

    IBlockState blockFromString(String blockString);

    void placeBlock(double x, double y, double z, IBlockState block);

    void chatLog(String msg);

}
