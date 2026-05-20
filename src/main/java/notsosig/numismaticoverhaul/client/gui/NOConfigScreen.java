package notsosig.numismaticoverhaul.client.gui;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import notsosig.numismaticoverhaul.config.NOClientConfig;
import notsosig.numismaticoverhaul.config.NOConfig;

public class NOConfigScreen {

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.numismaticoverhaul.title"));

        ConfigEntryBuilder entries = builder.entryBuilder();

        // ── Client ───────────────────────────────────────────────────────────
        ConfigCategory client = builder.getOrCreateCategory(Component.translatable("config.numismaticoverhaul.category.client"));

        var purseHud = entries.startSubCategory(Component.translatable("config.numismaticoverhaul.subcategory.purseHud"));
        purseHud.add(entries.startIntSlider(
                        Component.translatable("config.numismaticoverhaul.pursePositionX"),
                        NOClientConfig.CLIENT.pursePositionX.get(), 0, 400)
                .setDefaultValue(129)
                .setSaveConsumer(val -> NOClientConfig.CLIENT.pursePositionX.set(val))
                .build());
        purseHud.add(entries.startIntSlider(
                        Component.translatable("config.numismaticoverhaul.pursePositionY"),
                        NOClientConfig.CLIENT.pursePositionY.get(), 0, 400)
                .setDefaultValue(20)
                .setSaveConsumer(val -> NOClientConfig.CLIENT.pursePositionY.set(val))
                .build());
        client.addEntry(purseHud.build());

        // ── Common ───────────────────────────────────────────────────────────
        ConfigCategory common = builder.getOrCreateCategory(Component.translatable("config.numismaticoverhaul.category.common"));

        var deathPenalty = entries.startSubCategory(Component.translatable("config.numismaticoverhaul.subcategory.deathPenalty"));
        deathPenalty.add(entries.startDoubleField(
                        Component.translatable("config.numismaticoverhaul.moneyDropChance.peaceful"),
                        NOConfig.INSTANCE.moneyDropChancePeaceful.get())
                .setDefaultValue(0.0).setMin(0.0).setMax(100.0)
                .setSaveConsumer(val -> NOConfig.INSTANCE.moneyDropChancePeaceful.set(val))
                .build());
        deathPenalty.add(entries.startDoubleField(
                        Component.translatable("config.numismaticoverhaul.moneyDropChance.easy"),
                        NOConfig.INSTANCE.moneyDropChanceEasy.get())
                .setDefaultValue(25.0).setMin(0.0).setMax(100.0)
                .setSaveConsumer(val -> NOConfig.INSTANCE.moneyDropChanceEasy.set(val))
                .build());
        deathPenalty.add(entries.startDoubleField(
                        Component.translatable("config.numismaticoverhaul.moneyDropChance.normal"),
                        NOConfig.INSTANCE.moneyDropChanceNormal.get())
                .setDefaultValue(50.0).setMin(0.0).setMax(100.0)
                .setSaveConsumer(val -> NOConfig.INSTANCE.moneyDropChanceNormal.set(val))
                .build());
        deathPenalty.add(entries.startDoubleField(
                        Component.translatable("config.numismaticoverhaul.moneyDropChance.hard"),
                        NOConfig.INSTANCE.moneyDropChanceHard.get())
                .setDefaultValue(75.0).setMin(0.0).setMax(100.0)
                .setSaveConsumer(val -> NOConfig.INSTANCE.moneyDropChanceHard.set(val))
                .build());
        common.addEntry(deathPenalty.build());

        var trading = entries.startSubCategory(Component.translatable("config.numismaticoverhaul.subcategory.trading"));
        trading.add(entries.startBooleanToggle(
                        Component.translatable("config.numismaticoverhaul.wrapModTrades"),
                        NOConfig.INSTANCE.wrapModTrades.get())
                .setDefaultValue(true)
                .setTooltip(
                        t("config.numismaticoverhaul.wrapModTrades.tooltip"),
                        t("config.numismaticoverhaul.wrapModTrades.tooltip2"),
                        t("config.numismaticoverhaul.wrapModTrades.tooltip3"),
                        t("config.numismaticoverhaul.wrapModTrades.tooltip4"),
                        t("config.numismaticoverhaul.wrapModTrades.tooltip5"))
                .setSaveConsumer(val -> NOConfig.INSTANCE.wrapModTrades.set(val))
                .build());
        trading.add(entries.startBooleanToggle(
                        Component.translatable("config.numismaticoverhaul.enableDenominationExchange"),
                        NOConfig.INSTANCE.enableDenominationExchange.get())
                .setDefaultValue(true)
                .setTooltip(
                        t("config.numismaticoverhaul.enableDenominationExchange.tooltip"),
                        t("config.numismaticoverhaul.enableDenominationExchange.tooltip2"),
                        t("config.numismaticoverhaul.enableDenominationExchange.tooltip3"))
                .setSaveConsumer(val -> NOConfig.INSTANCE.enableDenominationExchange.set(val))
                .build());
        trading.add(entries.startBooleanToggle(
                        Component.translatable("config.numismaticoverhaul.enableSmartAutofill"),
                        NOConfig.INSTANCE.enableSmartAutofill.get())
                .setDefaultValue(true)
                .setTooltip(
                        t("config.numismaticoverhaul.enableSmartAutofill.tooltip"),
                        t("config.numismaticoverhaul.enableSmartAutofill.tooltip2"),
                        t("config.numismaticoverhaul.enableSmartAutofill.tooltip3"))
                .setSaveConsumer(val -> NOConfig.INSTANCE.enableSmartAutofill.set(val))
                .build());
        trading.add(entries.startBooleanToggle(
                        Component.translatable("config.numismaticoverhaul.showCurrencyActionBar"),
                        NOConfig.INSTANCE.showCurrencyActionBar.get())
                .setDefaultValue(true)
                .setTooltip(
                        t("config.numismaticoverhaul.showCurrencyActionBar.tooltip"),
                        t("config.numismaticoverhaul.showCurrencyActionBar.tooltip2"))
                .setSaveConsumer(val -> NOConfig.INSTANCE.showCurrencyActionBar.set(val))
                .build());
        common.addEntry(trading.build());

        var economy = entries.startSubCategory(Component.translatable("config.numismaticoverhaul.subcategory.economy"));
        economy.add(entries.startLongField(
                        Component.translatable("config.numismaticoverhaul.maxPurseValue"),
                        NOConfig.INSTANCE.maxPurseValue.get())
                .setDefaultValue(-1L)
                .setMin(-1L)
                .setTooltip(
                        t("config.numismaticoverhaul.maxPurseValue.tooltip"),
                        t("config.numismaticoverhaul.maxPurseValue.tooltip2"),
                        t("config.numismaticoverhaul.maxPurseValue.tooltip3"),
                        t("config.numismaticoverhaul.maxPurseValue.tooltip4"))
                .setSaveConsumer(val -> NOConfig.INSTANCE.maxPurseValue.set(val))
                .build());
        common.addEntry(economy.build());

        return builder.build();
    }

    private static Component t(String key) {
        return Component.translatable(key);
    }
}
