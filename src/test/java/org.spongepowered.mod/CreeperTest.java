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
package org.spongepowered.mod;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.monster.Creeper;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.mctester.internal.TestUtils;
import org.spongepowered.mctester.internal.BaseTest;
import org.spongepowered.mctester.junit.MinecraftRunner;
import org.spongepowered.mctester.junit.WorldOptions;

@RunWith(MinecraftRunner.class)
@WorldOptions(deleteWorldOnSuccess = true)
public class CreeperTest extends BaseTest {

    public CreeperTest(TestUtils testUtils) {
        super(testUtils);
    }

    @Test
    public void explodeCreeper() throws Throwable {
        Player player = this.testUtils.getThePlayer();
        player.offer(Keys.GAME_MODE, GameModes.CREATIVE);

        Creeper creeper = (Creeper) player.getWorld().createEntity(EntityTypes.CREEPER, player.getPosition().add(2, 0, 1));
        creeper.setCreator(player.getUniqueId());


        EventListener<MoveEntityEvent> moveListener = this.testUtils.listen(MoveEntityEvent.class, new EventListener<MoveEntityEvent>() {

            @Override
            public void handle(MoveEntityEvent event) throws Exception {
                if (event.getTargetEntity().getUniqueId() == creeper.getUniqueId()) {
                    event.setCancelled(true);
                }
            }
        });

        testUtils.listen(MoveEntityEvent.class, moveListener);

        player.getWorld().spawnEntity(creeper);
        ((Hotbar) player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class))).set(new SlotIndex(0), ItemStack.of(
                ItemTypes.FLINT_AND_STEEL, 1));
        testUtils.waitForAll();

        this.client.lookAt(creeper);
        this.client.rightClick();

        this.testUtils.listenTimeout(ChangeBlockEvent.Break.class, new EventListener<ChangeBlockEvent.Break>() {

            @Override
            public void handle(ChangeBlockEvent.Break event) throws Exception {
                assertThat(event.getCause().getContext().get(EventContextKeys.OWNER).get().getUniqueId(), equalTo(player.getUniqueId()));

                for (Transaction<BlockSnapshot> transaction: event.getTransactions()) {
                    assertThat(transaction.getFinal().getState().getType(), equalTo(BlockTypes.AIR));
                }
            }
        }, 2 * 20);

        throw new AssertionError("Dummy assertion failure!");
    }
}
