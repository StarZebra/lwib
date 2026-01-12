package me.starzebra.lwib.buttons;

import me.starzebra.lwib.Lwib;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ItemSelectionScreen extends Screen {

    private final Screen parent;
    private final ItemSelectionCallback callback;
    private EditBox searchBox;

    private final List<Item> allItems;
    private List<Item> filteredItems;

    private int scrollOffset = 0;
    private final int itemsPerRow = 13;
    private int rows;
    private final int itemSize = 18;
    private int gridStartX;
    private int gridStartY;
    private int gridWidth;
    private int gridHeight;

    private static final int TOP_PADDING = 60;
    private static final int BOTTOM_PADDING = 50;

    public ItemSelectionScreen(Screen parent, ItemSelectionCallback callback) {
        super(Component.literal("Select Item"));
        this.parent = parent;
        this.callback = callback;

        // Get all items from the registry
        this.allItems = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item != Items.AIR) {
                this.allItems.add(item);
            }
        }
        this.filteredItems = new ArrayList<>(this.allItems);
    }

    @Override
    protected void init() {
        super.init();

        // Calculate how many rows can fit on screen
        int availableHeight = this.height - TOP_PADDING - BOTTOM_PADDING;
        this.rows = Math.max(3, availableHeight / itemSize);

        // Calculate grid dimensions
        this.gridWidth = itemsPerRow * itemSize;
        this.gridHeight = rows * itemSize;
        this.gridStartX = (this.width - gridWidth) / 2;
        this.gridStartY = TOP_PADDING;

        // Search box
        this.searchBox = new EditBox(
                Lwib.mc.font,
                this.width / 2 - 100,
                30,
                200,
                20,
                Component.literal("Search...")
        );
        this.searchBox.setHint(Component.literal("Search items..."));
        this.searchBox.setResponder(this::onSearchChanged);
    }

    private void onSearchChanged(String search) {
        this.scrollOffset = 0;

        if (search.trim().isEmpty()) {
            this.filteredItems = new ArrayList<>(this.allItems);
        } else {
            this.filteredItems = new ArrayList<>();
            String lowerSearch = search.toLowerCase(Locale.ROOT);

            for (Item item : this.allItems) {
                String itemName = item.getName(item.getDefaultInstance()).getString().toLowerCase(Locale.ROOT);
                String itemId = BuiltInRegistries.ITEM.getKey(item).toString().toLowerCase(Locale.ROOT);

                if (itemName.contains(lowerSearch) || itemId.contains(lowerSearch)) {
                    this.filteredItems.add(item);
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Title
        guiGraphics.drawCenteredString(Lwib.mc.font, this.title, this.width / 2, 10, 0xFFFFFF);

        // Search box
        this.searchBox.render(guiGraphics, mouseX, mouseY, partialTick);

        // Grid background
        guiGraphics.fill(
                gridStartX - 2,
                gridStartY - 2,
                gridStartX + gridWidth + 2,
                gridStartY + gridHeight + 2,
                0xFF222222
        );

        // Render items
        int maxScroll = Math.max(0, (int) Math.ceil(filteredItems.size() / (double) itemsPerRow) - rows);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        int startIndex = scrollOffset * itemsPerRow;
        int endIndex = Math.min(startIndex + (itemsPerRow * rows), filteredItems.size());

        for (int i = startIndex; i < endIndex; i++) {
            int localIndex = i - startIndex;
            int row = localIndex / itemsPerRow;
            int col = localIndex % itemsPerRow;

            int x = gridStartX + col * itemSize;
            int y = gridStartY + row * itemSize;

            Item item = filteredItems.get(i);
            ItemStack stack = item.getDefaultInstance();

            // Highlight on hover
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                guiGraphics.fill(RenderPipelines.GUI, x, y, x + 16, y + 16, 0x80FFFFFF);
            }

            // Render item
            guiGraphics.renderItem(stack, x, y);
        }

        // Scroll indicator
        if (maxScroll > 0) {
            String scrollText = String.format("Page %d / %d (Scroll to navigate)", scrollOffset + 1, maxScroll + 1);
            guiGraphics.drawCenteredString(
                    Lwib.mc.font,
                    scrollText,
                    this.width / 2,
                    gridStartY + gridHeight + 10,
                    0xFFAAAAAA
            );
        }

        // Item count
        String countText = String.format("Showing %d items", filteredItems.size());
        guiGraphics.drawString(
                Lwib.mc.font,
                countText,
                gridStartX,
                gridStartY + gridHeight + 25,
                0xFF888888
        );
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseEvent, boolean isDoubleClick) {
        if (this.searchBox.mouseClicked(mouseEvent, isDoubleClick)) {
            this.searchBox.setFocused(true);
            return true;
        }

        this.searchBox.setFocused(false);

        if (mouseEvent.button() == 0) {
            int mouseX = (int) mouseEvent.x();
            int mouseY = (int) mouseEvent.y();

            // Check if clicked in grid
            if (mouseX >= gridStartX && mouseX < gridStartX + gridWidth &&
                    mouseY >= gridStartY && mouseY < gridStartY + gridHeight) {

                int col = (mouseX - gridStartX) / itemSize;
                int row = (mouseY - gridStartY) / itemSize;

                int index = (scrollOffset * itemsPerRow) + (row * itemsPerRow) + col;

                if (index >= 0 && index < filteredItems.size()) {
                    Item selectedItem = filteredItems.get(index);
                    callback.onItemSelected(selectedItem.getDefaultInstance());
                    this.onClose();
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseEvent, isDoubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {

        scrollOffset -= (int) scrollY;

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (this.searchBox.keyPressed(keyEvent)) {
            return true;
        }

        if (keyEvent.input() == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyEvent);
    }

    @Override
    public boolean charTyped(CharacterEvent characterEvent) {
        // If search box isn't focused and user types a character, focus it and type
        if (!this.searchBox.isFocused()) {
            this.searchBox.setFocused(true);
            this.searchBox.charTyped(characterEvent);
            return true;
        }

        if (this.searchBox.charTyped(characterEvent)) {
            return true;
        }
        return super.charTyped(characterEvent);
    }

    @Override
    public void onClose() {
        if (Lwib.mc != null) {
            Lwib.mc.setScreen(parent);
        }
    }

    @FunctionalInterface
    public interface ItemSelectionCallback {
        void onItemSelected(ItemStack item);
    }
}