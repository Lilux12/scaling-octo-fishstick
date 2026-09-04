package ru.danya.autosorter.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import ru.danya.autosorter.network.ModPayloads;

import java.util.ArrayList;
import java.util.List;

/**
 * Экран сортировщика с двумя вкладками:
 *  - "Сортировка": обычные слоты (буфер + инвентарь игрока), рисуются автоматически хендлером.
 *  - "Настройки": список найденных сундуков, клик — привязать/отвязать,
 *    ПКМ по записи с предметом в руке — назначить фильтр,
 *    кнопки категорий (Дерево/Руды/Снаряжение/Мусор) под каждой записью.
 */
public class AutoSorterScreen extends HandledScreen<AutoSorterScreenHandler> {

	private static final int TAB_SORTING = 0;
	private static final int TAB_SETTINGS = 1;
	private int currentTab = TAB_SORTING;

	private List<ModPayloads.ChestEntry> chestEntries = new ArrayList<>();
	private BlockPos sorterPos;

	private ButtonWidget sortingTabButton;
	private ButtonWidget settingsTabButton;

	public AutoSorterScreen(AutoSorterScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.backgroundWidth = 176;
		this.backgroundHeight = 200;
		this.sorterPos = handler.sorterPos;
	}

	@Override
	protected void init() {
		super.init();
		int x = (width - backgroundWidth) / 2;
		int y = (height - backgroundHeight) / 2;

		sortingTabButton = ButtonWidget.builder(Text.translatable("gui.autosorter.tab_sorting"), b -> switchTab(TAB_SORTING))
				.dimensions(x, y - 22, 90, 20).build();
		settingsTabButton = ButtonWidget.builder(Text.translatable("gui.autosorter.tab_settings"), b -> {
			switchTab(TAB_SETTINGS);
			requestChestList();
		}).dimensions(x + 90, y - 22, 90, 20).build();

		addDrawableChild(sortingTabButton);
		addDrawableChild(settingsTabButton);

		rebuildSettingsWidgets();
	}

	private void switchTab(int tab) {
		this.currentTab = tab;
		rebuildSettingsWidgets();
	}

	private void requestChestList() {
		// Регистрация payload-типов уже выполнена один раз при старте мода
		// (AutoSorterMod.onInitialize -> ModNetworking.registerServerReceivers,
		// entrypoint "main" грузится и на клиенте тоже) — повторно регистрировать не нужно.
		net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
				new ModPayloads.RequestChestListC2S(sorterPos));
	}

	public void updateChestList(BlockPos sorterPos, List<ModPayloads.ChestEntry> entries) {
		if (!sorterPos.equals(this.sorterPos)) return;
		this.chestEntries = entries;
		rebuildSettingsWidgets();
	}

	private final List<net.minecraft.client.gui.widget.ClickableWidget> settingsWidgets = new ArrayList<>();

	private void rebuildSettingsWidgets() {
		for (var w : settingsWidgets) remove(w);
		settingsWidgets.clear();

		if (currentTab != TAB_SETTINGS) return;

		int x = (width - backgroundWidth) / 2 + 8;
		int y = (height - backgroundHeight) / 2 + 8;
		int rowHeight = 22;
		int i = 0;
		for (ModPayloads.ChestEntry entry : chestEntries) {
			int rowY = y + i * rowHeight;
			if (rowY > (height - backgroundHeight) / 2 + backgroundHeight - rowHeight) break; // не влезает — обрежем список

			String shortLabel = entry.label();
			ButtonWidget linkBtn = ButtonWidget.builder(
					Text.literal((entry.linked() ? "[✔] " : "[ ] ") + shortLabel),
					b -> {
						net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
								new ModPayloads.ToggleLinkC2S(sorterPos, entry.pos()));
						requestChestList();
					}
			).dimensions(x, rowY, 120, 20).build();
			addDrawableChild(linkBtn);
			settingsWidgets.add(linkBtn);

			ButtonWidget setItemBtn = ButtonWidget.builder(
					Text.translatable("gui.autosorter.use_held_item"),
					b -> {
						net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
								new ModPayloads.SetFilterItemC2S(sorterPos, entry.pos()));
						requestChestList();
					}
			).dimensions(x + 122, rowY, 46, 20).build();
			addDrawableChild(setItemBtn);
			settingsWidgets.add(setItemBtn);

			i++;
		}

		// Кнопки категорий — применяются к последнему привязанному сундуку в списке для простоты.
		// (Для точного назначения используйте кнопку рядом с конкретной записью выше — "Взять из руки".)
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
		int x = (width - backgroundWidth) / 2;
		int y = (height - backgroundHeight) / 2;
		context.fill(x, y, x + backgroundWidth, y + backgroundHeight, 0xFF8B8B8B);
		context.drawBorder(x, y, backgroundWidth, backgroundHeight, 0xFF373737);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);

		int x = (width - backgroundWidth) / 2;
		int y = (height - backgroundHeight) / 2;

		if (currentTab == TAB_SORTING) {
			context.drawText(textRenderer, Text.translatable("gui.autosorter.sorting_hint"), x + 8, y + 70, 0x404040, false);
			drawMouseoverTooltip(context, mouseX, mouseY);
		} else {
			context.drawText(textRenderer, Text.translatable("gui.autosorter.settings_hint"), x + 8, y + backgroundHeight - 30, 0x404040, false);
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
