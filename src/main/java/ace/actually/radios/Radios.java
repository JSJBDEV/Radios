package ace.actually.radios;

import ace.actually.radios.api.RadioSpec;
import ace.actually.radios.blocks.Band5RadioBlock;
import ace.actually.radios.impl.RadioStorageImpl;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Radios.MODID)
@Mod.EventBusSubscriber
public class Radios
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "radios";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    // Creates a new Block with the id "examplemod:example_block", combining the namespace and path
    public static final RegistryObject<Block> BAND_5_RADIO = BLOCKS.register("band5radio", () -> new Band5RadioBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
    // Creates a new BlockItem with the id "examplemod:example_block", combining the namespace and path
    public static final RegistryObject<Item> B5R_ITEM = ITEMS.register("band5radio", () -> new BlockItem(BAND_5_RADIO.get(), new Item.Properties()));

    // Creates a new food item with the id "examplemod:example_id", nutrition 1 and saturation 2

    private static final RadioStorageImpl RADIO_STORAGE = new RadioStorageImpl();


    public Radios(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        RadioSpec.initialize(RADIO_STORAGE);
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        RADIO_STORAGE.load(event.getServer());
    }

    @SubscribeEvent
    public static void onServerSave(LevelEvent.Save levelSavingEvent) {
        LevelAccessor accessor = levelSavingEvent.getLevel();
        if (!accessor.isClientSide()) {
            RADIO_STORAGE.save(accessor.getServer());
        }
    }
}
