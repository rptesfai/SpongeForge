/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.mod.test.integration;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.mctester.internal.BaseTest;
import org.spongepowered.mctester.internal.event.StandaloneEventListener;
import org.spongepowered.mctester.api.junit.MinecraftRunner;
import org.spongepowered.mctester.api.ScreenshotOptions;
import org.spongepowered.mctester.junit.TestUtils;
import org.spongepowered.mctester.api.UseSeparateWorld;
import org.spongepowered.mctester.api.WorldOptions;

@RunWith(MinecraftRunner.class)
@WorldOptions(deleteWorldOnSuccess = true)
public class SimpleMinecraftTester extends BaseTest {

    public SimpleMinecraftTester(TestUtils testUtils) {
        super(testUtils);
    }

    @Test
    @ScreenshotOptions(takeScreenshotOnSuccess = true)
    public void helpHelp() throws Throwable {
        String message = "Hello, world!";

        testUtils.listenOneShot(() -> client.sendMessage(message),
                new StandaloneEventListener<>(MessageChannelEvent.Chat.class, (MessageChannelEvent.Chat event) -> {
                    Assert.assertEquals(message, event.getRawMessage().toPlain());
                }));
    }

    @Test(expected = AssertionError.class)
    public void deliberateFailure() throws Throwable {
        testUtils.listenOneShot(() -> { client.sendMessage("blah"); }, new StandaloneEventListener<>(MessageChannelEvent.Chat.class, (MessageChannelEvent.Chat event) -> {
            Assert.assertEquals(1, 2);
        }));
    }


    @Test
    @UseSeparateWorld
    @WorldOptions(deleteWorldOnSuccess = true)
    public void chatTest() throws Throwable {
        final Text[] recievedMessage = new Text[1];

        testUtils.listenOneShot(() -> {
                    client.sendMessage("Hello, world!");

                },

                new StandaloneEventListener<MessageChannelEvent.Chat>(MessageChannelEvent.Chat.class, (MessageChannelEvent.Chat event) -> {
                    recievedMessage[0] = event.getRawMessage();
                }));

        int x = 2;
        int y = 2;

        ItemStack serverStack = testUtils.runOnMainThread(() -> {
            Sponge.getServer().getBroadcastChannel().send(Text.of("From a different thread!"), ChatTypes.SYSTEM);
            Sponge.getServer().getBroadcastChannel().send(Text.of("Success: ", recievedMessage[0]), ChatTypes.SYSTEM);

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
