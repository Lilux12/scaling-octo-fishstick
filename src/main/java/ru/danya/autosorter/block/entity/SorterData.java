package ru.danya.autosorter.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;

import java.util.List;

/** Всё состояние сортировщика (буфер + привязки), сохраняется одним блоком через Codec. */
public record SorterData(List<ChestLink> links, List<ItemStack> buffer) {
	public static final Codec<SorterData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ChestLink.CODEC.listOf().fieldOf("links").forGetter(SorterData::links),
			ItemStack.CODEC.listOf().fieldOf("buffer").forGetter(SorterData::buffer)
	).apply(instance, SorterData::new));
}
