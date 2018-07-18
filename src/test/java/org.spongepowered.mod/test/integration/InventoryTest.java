package org.spongepowered.mod.test.integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.mctester.api.junit.MinecraftRunner;
import org.spongepowered.mctester.internal.BaseTest;
import org.spongepowered.mctester.junit.TestUtils;

@RunWith(MinecraftRunner.class)
public class InventoryTest extends BaseTest {

    public InventoryTest(TestUtils testUtils) {
        super(testUtils);
    }

    @Test
    public void chatTest() throws Throwable {
        int x = 2;
        int y = 2;

        ItemStack serverStack = testUtils.runOnMainThread(() -> {
            ItemStack stack = ItemStack.of(ItemTypes.GOLD_INGOT, 5);

            Hotbar hotbar = (Hotbar) this.testUtils.getThePlayer().getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class));
            hotbar.set(new SlotIndex(hotbar.getSelectedSlotIndex()), stack);

            PlayerInventory playerInventory = (PlayerInventory) this.testUtils.getThePlayer().getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(PlayerInventory.class));
            playerInventory.getMainGrid().set(x, y, stack);

            return stack;
        });


        // We sleep two ticks to guarantee that the client has been updated.
        // During the next tick, the server will send our inventory changes to the client.
        // However, we don't want to rely on this happening at any particular point during the tick,
        // so we wait two ticks to guarantee that the update packets have been sent by the time
        // our code runs.
        //testUtils.sleepTicks(2);
        testUtils.waitForInventoryPropagation();

        PlayerInventory clientInventory = client.getClientInventory();
        ItemStack mainGridStack = clientInventory.getMainGrid().getSlot(x, y).get().peek().get();
        ItemStack clientStack = client.getItemInHand(HandTypes.MAIN_HAND);

        this.testUtils.assertStacksEqual(serverStack, clientStack);
        this.testUtils.assertStacksEqual(serverStack, mainGridStack);

    }
}
