package notsosig.numismaticoverhaul.init;

import notsosig.numismaticoverhaul.NumismaticOverhaul;
import notsosig.numismaticoverhaul.block.PiggyBankBlock;
import notsosig.numismaticoverhaul.block.PiggyBankBlockEntity;
import notsosig.numismaticoverhaul.block.ShopBlock;
import notsosig.numismaticoverhaul.block.ShopBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

import static notsosig.numismaticoverhaul.init.CreativeTabInit.addToTab;

public class BlockInit {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(NumismaticOverhaul.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NumismaticOverhaul.MODID);

    public static final DeferredBlock<PiggyBankBlock> PIGGY_BANK = registerBlock("piggy_bank", PiggyBankBlock::new);
    public static final DeferredBlock<ShopBlock> SHOP = registerBlock("shop", () -> new ShopBlock(false));
    public static final DeferredBlock<ShopBlock> INEXHAUSTIBLE_SHOP = registerBlock("inexhaustible_shop", () -> new ShopBlock(true));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PiggyBankBlockEntity>> PIGGY_BANK_BE =
            BLOCK_ENTITIES.register("piggy_bank", () -> BlockEntityType.Builder.of(PiggyBankBlockEntity::new, PIGGY_BANK.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShopBlockEntity>> SHOP_BE =
            BLOCK_ENTITIES.register("shop", () -> BlockEntityType.Builder.of(ShopBlockEntity::new, SHOP.get(), INEXHAUSTIBLE_SHOP.get()).build(null));

    @SuppressWarnings("unchecked")
    protected static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        return (DeferredBlock<T>) addToTab(registerBlock(name, block, b -> () -> new BlockItem(b.get(), new Item.Properties())));
    }

    protected static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block, Function<DeferredBlock<T>, Supplier<? extends BlockItem>> item) {
        DeferredBlock<T> reg = BLOCKS.registerBlock(name, p -> block.get());
        ItemInit.ITEMS.register(name, () -> item.apply(reg).get());
        return reg;
    }
}
