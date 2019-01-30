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
import com.dizzyd.coregen.world.WorldData;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.geometry.Point;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.server.command.CommandTreeBase;
import rx.Observable;

import java.io.*;
import java.util.HashSet;
import java.util.LongSummaryStatistics;
import java.util.Set;
import java.util.regex.Pattern;

public class Command extends CommandTreeBase {
    @Override
    public String getName() {
        return "cg";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "cmd.cg.usage";
    }

    public Command() {
        addSubcommand(new GenerateDeposit());
        addSubcommand(new ReloadConfig());
        addSubcommand(new ClearBlocks());
        addSubcommand(new RegenScripts());
        addSubcommand(new GetStats());
        addSubcommand(new ResetStats());
        addSubcommand(new FindDeposit());
        addSubcommand(new DumpDeposits());
        addSubcommand(new RunScript());
        addSubcommand(new ListBlocks());
        addSubcommand(new ListBiomes());
    }

    private static String getArg(String[] args, int id) {
        if (args.length > id) {
            return args[id];
        } else {
            return null;
        }
    }

    public static class GenerateDeposit extends CommandBase {

        @Override
        public String getName() {
            return "deposit";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "cmd.cg.deposit.usage";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            String depositId = getArg(args, 0);
            String reload = getArg(args, 1);

            if (reload != null) {
                CoreGen.reloadConfig();
            }

            Deposit deposit = CoreGen.config.getDeposits().get(depositId);
            if (deposit == null) {
                Command.notifyCommandListener(sender, this, "cmd.cg.deposit.unknown.deposit", depositId);
            }

            World world = sender.getEntityWorld();
            int cx = sender.getPosition().getX() >> 4;
            int cz = sender.getPosition().getZ() >> 4;
            deposit.generate(world.rand, cx, cz, world, null, world.getChunkProvider());
        }
    }

    public static class ReloadConfig extends CommandBase {

        @Override
        public String getName() {
            return "reload";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "return cmd.cg.reload.usage";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            CoreGen.reloadConfig();
            Command.notifyCommandListener(sender, this, "cmd.cg.config.reload");
        }
    }

    public static class ClearBlocks extends CommandBase {

        @Override
        public String getName() {
            return "clear";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "cmd.cg.clear.usage";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            Block blockType = CommandBase.getBlockByText(sender, args[0]);
            int radius = CommandBase.parseInt(args[1]);

            BlockPos playerPos = sender.getPosition();

            World world = sender.getEntityWorld();
            if (world.isRemote) {
                return;
            }

            int count = 0;
            Set<Chunk> chunks = new HashSet<Chunk>();

            // Identify all block positions in the provided radius
            Iterable<BlockPos> blockPositions = BlockPos.getAllInBox(playerPos.getX() - radius, playerPos.getY() - radius, playerPos.getZ() - radius,
                    playerPos.getX() + radius, playerPos.getY() + radius, playerPos.getZ() + radius);

            // For each block in the radius, get the associated chunk, change the block state
            // directly in chunk and save chunk for further processing. This side steps any
            // neighbor notifications and allow us to avoid generating a large cascade of block updates
            for (BlockPos p : blockPositions) {
                IBlockState b = sender.getEntityWorld().getBlockState(p);
                if (blockType.equals(b.getBlock())) {
                    Chunk chunk = world.getChunkFromBlockCoords(p);
                    chunk.setBlockState(p, Blocks.AIR.getDefaultState());
                    chunks.add(chunk);
                    count++;
                }
            }

            // Walk over the set of chunks and generate a chunk refresh manually; this batches all
            // the updates and ensures no block update notification
            PlayerChunkMap manager = ((WorldServer) world).getPlayerChunkMap();
            for (Chunk c : chunks) {
                PlayerChunkMapEntry watcher = manager.getEntry(c.x, c.z);
                if (watcher != null) {
                    watcher.sendPacket(new SPacketChunkData(c, -1));
                }
            }

            Command.notifyCommandListener(sender, this, "cmd.cg.clear.ok", count);

        }
    }

    public static class RegenScripts extends CommandBase {

        @Override
        public String getName() {
            return "regen-scripts";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "cmd.cg.regen.scripts.usage";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            CoreGen.installScripts(true);
            Command.notifyCommandListener(sender, this, "cmd.cg.regen.scripts.ok");
        }
    }

    public static class GetStats extends CommandBase {

        @Override
        public String getName() {
            return "stats";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "cmd.cg.get.stats";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            String depositId = getArg(args, 0);
            if (depositId == null) {
                LongSummaryStatistics stats = CoreGen.worldGen.getStats();
                String avgus = String.format("%4.0f", stats.getAverage() * 1000);
                CoreGen.logger.info("Global stats: {} chunks, {} / {} / {}", stats.getCount(),
                                    stats.getMin(), avgus, stats.getMax());
                Command.notifyCommandListener(sender, this, "cmd.cg.get.stats.global", avgus);
                return;
            }

            Deposit deposit = CoreGen.config.getDeposits().getOrDefault(depositId, null);
            if (deposit == null) {
                Command.notifyCommandListener(sender, this, "cmd.cg.get.stats.unknown.deposit", depositId);
                return;
            }

            LongSummaryStatistics stats = deposit.getStats();
            String avgus = String.format("%4.0f", stats.getAverage() * 1000);
            CoreGen.logger.info("Deposit {} stats: {} chunks, {} / {} / {}", depositId, stats.getCount(),
                                stats.getMin(), avgus, stats.getMax());
            Command.notifyCommandListener(sender, this, "cmd.cg.get.stats.deposit", depositId, avgus);
        }
    }

    public static class ResetStats extends CommandBase {

        @Override
        public String getName() {
            return "reset-stats";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "cmd.cg.reset.stats";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            CoreGen.worldGen.resetStats();
            for (Deposit d : CoreGen.config.getDeposits().values()) {
                d.resetStats();
            }
            Command.notifyCommandListener(sender, this, "cmd.cg.reset.stats.ok");
        }
    }

    public static class FindDeposit extends CommandBase {

        @Override
        public String getName() {
            return "find";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "cmd.cg.find.usage";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            String depositId = getArg(args, 0);
            ChunkPos pos = new ChunkPos(sender.getPosition());
            Entry<String, Point> entry;
            if (depositId != null) {
                entry = WorldData.nearestDeposit(sender.getEntityWorld(), depositId, pos.x, pos.z);
            } else {
                entry = WorldData.nearestDeposit(sender.getEntityWorld(), pos.x, pos.z);
            }

            if (entry == null) {
                Command.notifyCommandListener(sender, this, "cmd.cg.find.not.found", depositId);
                return;
            }

            ChunkPos depositPos = new ChunkPos((int)entry.geometry().x(), (int)entry.geometry().y());
            int distance = (int)Math.sqrt(depositPos.getDistanceSq(sender.getCommandSenderEntity()));
            Command.notifyCommandListener(sender, this, "cmd.cg.find.ok",
                                          entry.value(), depositPos.getXStart(), depositPos.getZStart(), distance);

        }
    }

    public static class DumpDeposits extends CommandBase {

        @Override
        public String getName() {
            return "dump-deposits";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "cmd.cg.dump.deposits.usage";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            try {
                int count = WorldData.dumpDeposits(sender.getEntityWorld());
                Command.notifyCommandListener(sender, this, "cmd.cg.dump.deposits.ok", count);
            } catch (IOException e) {
                Command.notifyCommandListener(sender, this, "cmd.cg.dump.deposits.error");
                e.printStackTrace();
            }
        }
    }

    public static class RunScript extends CommandBase {
        @Override
        public String getName() {
            return "script";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "cmd.cg.script.usage";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            String scriptName = getArg(args, 0);
            if (scriptName != null) {
                String status = CoreGen.scriptUtil.run(scriptName, sender.getEntityWorld(), sender.getPosition());
                Command.notifyCommandListener(sender, this, "cmd.cg.script", status);
            } else {
                Command.notifyCommandListener(sender, this, "cmd.cg.script.missing.arg");
            }
        }
    }

    public static class ListBlocks extends CommandBase {

        @Override
        public String getName() {
            return "list-blocks";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "cmd.cg.list.blocks.usage";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            String regex = getArg(args, 0);
            if (regex != null) {
                // Compile the regex
                Pattern p = Pattern.compile(regex);

                // Traverse the block registry, printing out any blocks that match our regex
                int count = 0;
                CoreGen.logger.info("Starting listing of blocks matching: {}", regex);
                for(Block b : ForgeRegistries.BLOCKS) {
                    if (p.matcher(b.getRegistryName().toString()).matches()) {
                        CoreGen.logger.info("* {}", b.getRegistryName().toString());
                        count++;
                    }
                }
                CoreGen.logger.info("----");
                Command.notifyCommandListener(sender, this, "cmd.cg.list.blocks.ok", count);
            }
        }
    }

    public static class ListBiomes extends CommandBase {

        @Override
        public String getName() {
            return "list-biomes";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "cmd.cg.list.biomes.usage";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            int count = 0;
            CoreGen.logger.info("Starting list of biomes:");
            for (Biome biome: ForgeRegistries.BIOMES) {
                Set<BiomeDictionary.Type> biomeTypes = BiomeDictionary.getTypes(biome);
                CoreGen.logger.info("* {}: {}", biome.getRegistryName().getResourcePath(), biomeTypes.toString());
                count++;
            }
            CoreGen.logger.info("----");
            Command.notifyCommandListener(sender, this, "cmd.cg.list.biomes.ok", count);
        }
    }

}