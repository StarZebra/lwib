package me.starzebra.lwib.commands;

import com.mojang.brigadier.CommandDispatcher;
import me.starzebra.lwib.Lwib;
import me.starzebra.lwib.buttons.InvButtonEditorScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;

public class EditCommand {

    public static void init(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext ignored){
        dispatcher.register(ClientCommandManager.literal("lwibedit").executes(commandContext -> {
            Lwib.setScreen(new InvButtonEditorScreen());
            return 1;
        }));
    }

}
