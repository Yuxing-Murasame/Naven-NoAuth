package moe.ichinomiya.naven.modules;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import moe.ichinomiya.naven.Naven;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.impl.EventKey;
import moe.ichinomiya.naven.events.impl.EventMouseClick;
import moe.ichinomiya.naven.exceptions.NoSuchModuleException;
import moe.ichinomiya.naven.modules.impl.combat.*;
import moe.ichinomiya.naven.modules.impl.misc.*;
import moe.ichinomiya.naven.modules.impl.move.*;
import moe.ichinomiya.naven.modules.impl.render.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class ModuleManager {
    @Getter
    private final List<Module> modules = new ArrayList<>();

    private final Map<Class<? extends Module>, Module> classMap = new HashMap<>();
    private final Map<String, Module> nameMap = new HashMap<>();

    public ModuleManager() {
        try {
            initModules();
            modules.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        } catch (Exception e) {
            log.error("Failed to initialize modules", e);
            throw new RuntimeException(e);
        }

        Naven.getInstance().getEventManager().register(this);
    }

    private void initModules() {
        // Combat
        registerModule(new Aura(), new KeepSprint(), new AntiBot(), new NoRecoil(), new ShieldHelper(), new RageBot(),
                new ThrowableAimAssist(), new AutoThrow());

        // Movement
        registerModule(new Sprint(), new Velocity(), new Scaffold(), new ChestAura(), new NoSlow(), new Blink(),
                new Stuck(), new AutoStuck(), new FastWeb(), new NoLiquid(), new Spider(), new CollisionSpeed(),
                new TargetStrafe(), new FlyMeToTheMoon());

        // Render
        registerModule(new ClickGUIModule(), new ArrayListMod(), new WaterMark(), new NameTags(), new PlayerTags(),
                new InventoryMove(), new Projectile(), new NoHurtCam(), new Scoreboard(), new ViewClip(),
                new EffectDisplay(), new TimeChanger(), new NoTitle(), new XRay(), new NoLiquidFog(),
                new BlockAnimation(), new FullBright(), new FreeLook(), new Widget(), new ItemTags(),
                new ItemPhysics(), new BedESP(), new DamageParticle(), new NoRender());

        // Misc
        registerModule(new AutoArmor(), new InventoryManager(), new Teams(), new ContainerStealer(), new FastPlace(),
                new AutoTools(), new KillSay(), new HackerDetector(), new MouseTweaker(), new ClientFriend(),
                new NoJumpDelay(), new FastMine(), new Disabler(), new AntiFireball(), new AutoHeal(),
                new OffhandFeatures(), new AutoProfession(), new PreferWeapon(), new Spammer(), new TimeBalanceAbuse(),
                new WallShop(), new FastThrow(), new AutoHub(), new NoSmoothCamera(), new AntiStaff(), new AutoReport(),
                new Fireman());
    }

    private void registerModule(Module... modules) {
        for (Module module : modules) {
            registerModule(module);
        }
    }

    private void registerModule(Module module) {
        module.initModule();

        modules.add(module);
        classMap.put(module.getClass(), module);
        nameMap.put(module.getName().toLowerCase(), module);
    }

    public List<Module> getModulesByCategory(Category category) {
        List<Module> modules = new ArrayList<>();

        for (Module module : this.modules) {
            if (module.getCategory() == category) {
                modules.add(module);
            }
        }

        return modules;
    }

    public Module getModule(Class<? extends Module> clazz) {
        Module module = classMap.get(clazz);

        if (module == null) {
            throw new NoSuchModuleException();
        }

        return module;
    }

    public Module getModule(String name) {
        Module module = nameMap.get(name.toLowerCase());

        if (module == null) {
            throw new NoSuchModuleException();
        }

        return module;
    }

    @EventTarget
    public void onKey(EventKey e) {
        if (e.isState()) {
            for (Module module : modules) {
                if (module.getKey() == e.getKey()) {
                    module.toggle();
                }
            }
        }
    }

    @EventTarget
    public void onKey(EventMouseClick e) {
        if (!e.isState()) {
            if (e.getKey() == 3 || e.getKey() == 4) {
                for (Module module : modules) {
                    if (module.getKey() == -e.getKey()) {
                        module.toggle();
                    }
                }
            }
        }
    }
}
