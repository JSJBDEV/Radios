package ace.actually.radios;

import ace.actually.radios.api.RadioSpec;
import ace.actually.radios.blocks.Band5RadioBlock;
import ace.actually.radios.impl.RadioStorage;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Radios.MODID)
public class Radios
{
    public static final String MODID = "radios";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredBlock<Band5RadioBlock> BAND_5_RADIO = BLOCKS.register(
        "band5radio",
        () -> new Band5RadioBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE))
    );
    public static final DeferredItem<BlockItem> B5R_ITEM = ITEMS.registerSimpleBlockItem("band5radio", BAND_5_RADIO);

    private static final RadioStorage RADIO_STORAGE = new RadioStorage();

    public Radios(IEventBus modEventBus)
    {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        NeoForge.EVENT_BUS.register(this);

        RadioSpec.initialize(RADIO_STORAGE);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        RADIO_STORAGE.load(event.getServer());
    }

    @SubscribeEvent
    public void onServerSave(LevelEvent.Save levelSavingEvent) {
        LevelAccessor accessor = levelSavingEvent.getLevel();
        if (!accessor.isClientSide()) {
            RADIO_STORAGE.save(accessor.getServer());
        }
    }
}
