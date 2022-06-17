package net.silentchaos512.hpbar.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.silentchaos512.hpbar.gui.GuiConfigHealthBar;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return GuiConfigHealthBar::new;
    }
}
