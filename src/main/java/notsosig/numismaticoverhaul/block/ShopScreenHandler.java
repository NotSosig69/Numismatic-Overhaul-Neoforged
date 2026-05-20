package notsosig.numismaticoverhaul.block;

import io.wispforest.owo.client.screens.ScreenUtils;
import io.wispforest.owo.client.screens.SlotGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import notsosig.numismaticoverhaul.NumismaticOverhaul;
import notsosig.numismaticoverhaul.cap.CurrencyHolderAttacher;
import notsosig.numismaticoverhaul.client.gui.ShopScreen;
import notsosig.numismaticoverhaul.init.MenuInit;
import notsosig.numismaticoverhaul.network.ShopScreenHandlerRequestC2SPacket;
import notsosig.numismaticoverhaul.network.UpdateShopScreenS2CPacket;

import java.util.ArrayList;
import java.util.List;

public class ShopScreenHandler extends AbstractContainerMenu {

    private final Player owner;
    private final Container shopInventory;
    private final SimpleContainer bufferInventory = new SimpleContainer(1);
    public List<ShopOffer> offers;
    public ShopBlockEntity shop = null;
    public long storedCurrency;
    public boolean canTransfer;

    public ShopScreenHandler(int syncId, Inventory playerInventory, RegistryFriendlyByteBuf data) {
        this(syncId, playerInventory, getShop(playerInventory.player, data));
    }

    public ShopScreenHandler(int syncId, Inventory playerInventory, ShopBlockEntity shopInventory) {
        super(MenuInit.SHOP.get(), syncId);
        this.shopInventory = shopInventory;
        this.owner = playerInventory.player;
        this.shop = shopInventory;
        this.storedCurrency = this.shop.getStoredCurrency();
        this.canTransfer = this.shop.allowsTransfer;
        this.offers = this.shop.getOffers();
        SlotGenerator.begin(this::addSlot, 8, 17)
                .slotFactory((inv, index, x, y) -> new AutoHidingSlot(inv, index, x, y, 0, false))
                .grid(this.shopInventory, 0, 9, 3)
                .slotFactory(Slot::new)
                .moveTo(8, 85)
                .playerInventory(playerInventory);

        this.bufferInventory.addListener(this::onBufferChanged);
        this.addSlot(new AutoHidingSlot(bufferInventory, 0, 186, 14, 0, true) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                ItemStack shadow = stack.copy();
                this.set(shadow);
                return false;
            }

            @Override
            public boolean mayPickup(Player playerEntity) {
                this.set(ItemStack.EMPTY);
                return false;
            }
        });
    }

    private void onBufferChanged(Container inventory) {
        if (this.owner.level().isClientSide && Minecraft.getInstance().screen instanceof ShopScreen screen) {
            screen.afterDataUpdate();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.shopInventory.stillValid(player);
    }

    public void loadOffer(long index) {
        if (!this.owner.level().isClientSide) {
            if (index > this.offers.size() - 1) {
                NumismaticOverhaul.LOGGER.error("Player {} attempted to load invalid trade at index {}", owner.getName(), index);
                return;
            }
            this.bufferInventory.setItem(0, this.offers.get((int) index).getSellStack());
            this.updateClient();
        } else {
            PacketDistributor.sendToServer(new ShopScreenHandlerRequestC2SPacket(ShopScreenHandlerRequestC2SPacket.Action.LOAD_OFFER, index));
        }
    }

    public void createOffer(long price) {
        if (!this.owner.level().isClientSide) {
            final var stack = bufferInventory.getItem(0);
            if (stack.isEmpty()) return;
            this.shop.addOrReplaceOffer(new ShopOffer(stack, price));
            this.updateClient();
        } else {
            PacketDistributor.sendToServer(new ShopScreenHandlerRequestC2SPacket(ShopScreenHandlerRequestC2SPacket.Action.CREATE_OFFER, price));
        }
    }

    public void extractCurrency() {
        if (!this.owner.level().isClientSide) {
            CurrencyHolderAttacher.getCurrencyHolder(owner).modify(shop.getStoredCurrency(), owner);
            this.shop.setStoredCurrency(0);
            this.updateClient();
        } else {
            PacketDistributor.sendToServer(new ShopScreenHandlerRequestC2SPacket(ShopScreenHandlerRequestC2SPacket.Action.EXTRACT_CURRENCY));
        }
    }

    public void deleteOffer() {
        if (!this.owner.level().isClientSide) {
            this.shop.deleteOffer(bufferInventory.getItem(0));
            this.updateClient();
        } else {
            PacketDistributor.sendToServer(new ShopScreenHandlerRequestC2SPacket(ShopScreenHandlerRequestC2SPacket.Action.DELETE_OFFER));
        }
    }

    public void toggleTransfer() {
        if (!this.owner.level().isClientSide) {
            this.shop.toggleTransfer();
            this.updateClient();
        } else {
            PacketDistributor.sendToServer(new ShopScreenHandlerRequestC2SPacket(ShopScreenHandlerRequestC2SPacket.Action.TOGGLE_TRANSFER));
        }
    }

    public void updateClient() {
        PacketDistributor.sendToPlayer((ServerPlayer) owner, new UpdateShopScreenS2CPacket(shop));
    }

    public ItemStack getBufferStack() {
        return bufferInventory.getItem(0);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int invSlot) {
        return ScreenUtils.handleSlotTransfer(this, invSlot, this.shopInventory.getContainerSize());
    }

    public static ShopBlockEntity getShop(Player player, RegistryFriendlyByteBuf buf) {
        ShopBlockEntity shopBlockEntity = (ShopBlockEntity) player.level().getBlockEntity(buf.readBlockPos());
        shopBlockEntity.setStoredCurrency(buf.readLong());
        int offerCount = buf.readVarInt();
        List<ShopOffer> offers = new ArrayList<>(offerCount);
        for (int i = 0; i < offerCount; i++) {
            var item = ItemStack.STREAM_CODEC.decode(buf);
            long price = buf.readLong();
            if (!item.isEmpty() && price != 0) offers.add(new ShopOffer(item, price));
        }
        shopBlockEntity.getOffers().clear();
        shopBlockEntity.getOffers().addAll(offers);
        shopBlockEntity.allowsTransfer = buf.readBoolean();
        return shopBlockEntity;
    }

    private static class AutoHidingSlot extends Slot {

        private final int targetTab;
        private final boolean hide;

        public AutoHidingSlot(Container inventory, int index, int x, int y, int targetTab, boolean hide) {
            super(inventory, index, x, y);
            this.targetTab = targetTab;
            this.hide = hide;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean isActive() {
            if (!(Minecraft.getInstance().screen instanceof ShopScreen screen)) return true;
            return hide ? screen.tab() != targetTab : screen.tab() == targetTab;
        }
    }
}
