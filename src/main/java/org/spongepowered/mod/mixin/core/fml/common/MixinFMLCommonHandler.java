package org.spongepowered.mod.mixin.core.fml.common;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.IFMLSidedHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.CountDownLatch;

@Mixin(value = FMLCommonHandler.class, remap = false)
public abstract class MixinFMLCommonHandler {

    @Shadow private IFMLSidedHandler sidedDelegate;

    @Shadow public abstract MinecraftServer getMinecraftServerInstance();

    @Shadow private volatile CountDownLatch exitLatch;

    @Overwrite
    public void handleServerStopped()
    {
        System.err.println("Stopping sided delegate!");
        sidedDelegate.serverStopped();
        System.err.println("Getting server instance!");
        MinecraftServer server = getMinecraftServerInstance();
        System.err.println("Calling loader stop!");
        Loader.instance().serverStopped();
        System.err.println("Clearing reflection: " + server);
        // FORCE the internal server to stop: hello optifine workaround!
        if (server!=null) ObfuscationReflectionHelper.setPrivateValue(MinecraftServer.class, server, false, "field_71316"+"_v", "u", "serverStopped");

        // allow any pending exit to continue, clear exitLatch
        System.err.println("Checking latch");
        CountDownLatch latch = exitLatch;

        if (latch != null)
        {
            System.err.println("Counting down latch!");
            latch.countDown();
            exitLatch = null;
        }
        System.err.println("handleServerStopped done!");
    }


}
