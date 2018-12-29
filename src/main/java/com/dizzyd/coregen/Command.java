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
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.server.command.CommandTreeBase;

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
        addSubcommand(new CommandScript());
        addSubcommand(new GenerateDeposit());
        addSubcommand(new ReloadConfig());
    }

    private static String getArg(ICommandSender sender, CommandBase cmd, String[] args, int id, String errorKey) {
        if (args.length > id) {
            return args[id];
        } else {
            Command.notifyCommandListener(sender, cmd, errorKey);
            return null;
        }
    }

    public static class CommandScript extends CommandBase {
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
            String scriptName = args[0];

            String status = CoreGen.scriptUtil.run(scriptName, sender.getEntityWorld(), sender.getPosition());
            Command.notifyCommandListener(sender, this, "cmd.cg.script", status);
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
            String depositId = getArg(sender, this, args, 0, "cmd.cg.deposit.missing.deposit");
            Deposit deposit = CoreGen.config.getDeposits().get(depositId);
            if (deposit == null) {
                Command.notifyCommandListener(sender, this, "cmd.cg.deposit.unknown.deposit", depositId);
            }

            World world = sender.getEntityWorld();
            int cx = sender.getPosition().getX() >> 4;
            int cz = sender.getPosition().getZ() >> 4;
            deposit.generate(world.rand, cx, cz, world, null, world.getChunkProvider(), true);
        }
    }

    public static class ReloadConfig extends CommandBase {

        @Override
        public String getName() {
            return "reload";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "return cmd.cg.reload";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            CoreGen.reloadConfig();
            Command.notifyCommandListener(sender, this, "cmd.cg.config.reload");
        }
    }

}