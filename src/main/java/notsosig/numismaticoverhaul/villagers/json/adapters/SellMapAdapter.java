package notsosig.numismaticoverhaul.villagers.json.adapters;

import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import notsosig.numismaticoverhaul.NumismaticOverhaul;
import notsosig.numismaticoverhaul.currency.CurrencyHelper;
import notsosig.numismaticoverhaul.villagers.exceptions.DeserializationException;
import notsosig.numismaticoverhaul.villagers.json.TradeJsonAdapter;
import notsosig.numismaticoverhaul.villagers.json.VillagerJsonHelper;

import java.util.Locale;
import java.util.Optional;

public class SellMapAdapter extends TradeJsonAdapter {

    @Override
    @NotNull
    public VillagerTrades.ItemListing deserialize(JsonObject json) {

        loadDefaultStats(json, true);

        VillagerJsonHelper.assertString(json, "structure");
        int price = json.get("price").getAsInt();

        final String structureStr = GsonHelper.getAsString(json, "structure");
        final ResourceLocation structure = ResourceLocation.tryParse(structureStr);
        if (structure == null) throw new DeserializationException("Invalid structure resource location: " + structureStr);
        return new Factory(price, structure, max_uses, villager_experience, price_multiplier);
    }

    private static class Factory implements VillagerTrades.ItemListing, NumOTrade {
        private final int price;
        private final ResourceLocation structureId;
        private final int maxUses;
        private final int experience;
        private final float multiplier;

        public Factory(int price, ResourceLocation feature, int maxUses, int experience, float multiplier) {
            this.price = price;
            this.structureId = feature;
            this.maxUses = maxUses;
            this.experience = experience;
            this.multiplier = multiplier;
        }

        @Nullable
        public MerchantOffer getOffer(Entity entity, RandomSource random) {
            if (!(entity.level() instanceof ServerLevel serverWorld)) return null;

            final var registry = serverWorld.registryAccess().registryOrThrow(Registries.STRUCTURE);
            final Holder<Structure> feature = registry.getHolderOrThrow(ResourceKey.create(Registries.STRUCTURE, structureId));

            if (feature.unwrapKey().isEmpty()) {
                NumismaticOverhaul.LOGGER.error("Tried to create map to invalid structure " + this.structureId);
                return null;
            }

            final var result = serverWorld.getChunkSource().getGenerator().findNearestMapStructure(serverWorld, HolderSet.direct(feature),
                    entity.blockPosition(), 1500, true);

            if (result == null) return null;
            final var blockPos = result.getFirst();

            var iconType = MapDecorationTypes.TARGET_X;
            if (feature.is(StructureTags.ON_OCEAN_EXPLORER_MAPS))
                iconType = MapDecorationTypes.OCEAN_MONUMENT;
            if (feature.is(StructureTags.ON_WOODLAND_EXPLORER_MAPS))
                iconType = MapDecorationTypes.WOODLAND_MANSION;
            if (feature.is(StructureTags.ON_TREASURE_MAPS))
                iconType = MapDecorationTypes.RED_X;

            ItemStack itemStack = MapItem.create(serverWorld, blockPos.getX(), blockPos.getZ(), (byte) 2, true, true);
            MapItem.renderBiomePreviewMap(serverWorld, itemStack);
            MapItemSavedData.addTargetDecoration(itemStack, blockPos, "+", iconType);
            itemStack.set(DataComponents.CUSTOM_NAME, Component.translatable("filled_map." + feature.unwrapKey().get().location().getPath().toLowerCase(Locale.ROOT)));

            return new MerchantOffer(CurrencyHelper.getClosestItemCost(price), Optional.of(new ItemCost(Items.MAP, 1)), itemStack, this.maxUses, this.experience, multiplier);
        }
    }
}
