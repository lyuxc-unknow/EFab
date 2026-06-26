package mcjty.efab.compat.jade;

import mcjty.efab.block.HorizontalEFabEntityBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class JadeCompatibility implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(EFabServerDataProvider.INSTANCE, HorizontalEFabEntityBlock.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(EFabBlockComponentProvider.INSTANCE, HorizontalEFabEntityBlock.class);
    }
}
