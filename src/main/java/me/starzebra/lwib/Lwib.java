package me.starzebra.lwib;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import me.starzebra.lwib.buttons.InventoryButton;
import me.starzebra.lwib.commands.EditCommand;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Lwib implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("lwib");
    private static Screen newScreen = null;
    public static IEventBus EVENT_BUS = new EventBus();
    public static List<InventoryButton> inventoryButtons = new ArrayList<>();

    public static Minecraft mc;
    public static File config;

    @Override
    public void onInitialize() {
        mc = Minecraft.getInstance();

        EVENT_BUS.registerLambdaFactory(this.getClass().getPackageName(), (lookupInMethod, glass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, glass, MethodHandles.lookup()));

        config = new File(FabricLoader.getInstance().getConfigDir().toFile(), "lwib-invbuttons.json");

        deserializeButtons();

        ClientCommandRegistrationCallback.EVENT.register(EditCommand::init);
        ClientTickEvents.END_CLIENT_TICK.register((mc -> {
            if(newScreen != null){
                mc.setScreen(newScreen);
                newScreen = null;
            }
        }));

    }

    public static void setScreen(Screen screen){
        newScreen = screen;
    }

    public static void serializeButtons(){
        JsonArray array = new JsonArray(inventoryButtons.size());

        try {

            for (InventoryButton button : inventoryButtons){
                DataResult<JsonElement> result = InventoryButton.CODEC.encodeStart(JsonOps.INSTANCE, button);
                array.add(result.resultOrPartial(LOGGER::error).orElseThrow());
            }

            Files.writeString(config.toPath(), new GsonBuilder().setPrettyPrinting().create().toJson(array));
        }catch (Exception e){
            LOGGER.error("Exception while serializing buttons: {}", e.getMessage());
        }

    }

    private void deserializeButtons(){
        List<InventoryButton> loadedButtons = new ArrayList<>();

        if(!config.exists()){
            LOGGER.warn("Config not found while deserializing, perhaps first launch?");
            return;
        }

        try {
            String content = Files.readString(config.toPath());
            JsonArray jsonArray = (JsonArray) JsonParser.parseString(content);

            for (JsonElement json : jsonArray) {
                DataResult<InventoryButton> button = InventoryButton.CODEC.parse(JsonOps.INSTANCE, json);
                loadedButtons.add(button.resultOrPartial().orElseThrow());
            }

        }catch (Exception e){
            LOGGER.error("Exception while deserializing buttons: {}", e.getMessage());
        }

        inventoryButtons.addAll(loadedButtons);

    }

}
