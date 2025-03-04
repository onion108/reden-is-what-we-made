package com.github.zly2006.reden.malilib

import com.github.zly2006.reden.Reden
import fi.dy.masa.malilib.gui.GuiConfigsBase
import fi.dy.masa.malilib.gui.button.ButtonGeneric
import fi.dy.masa.malilib.util.StringUtils

class GuiConfigs: GuiConfigsBase(
    10, 50, Reden.MOD_ID, null, "reden.widget.config.title"
) {
    private var tab = ConfigGuiTab.GENERIC
    override fun initGui() {
        super.initGui()
        ConfigGuiTab.values().fold(10) { x, tab ->
            val button = ButtonGeneric(x, 26, -1, 20, tab.displayName)
            button.setEnabled(tab != this.tab)
            addButton(button) { _, _ ->
                if (tab != this.tab) {
                    this.tab = tab
                    initGui()
                }
            }
            button.width + x + 2
        }
    }
    override fun getConfigs(): MutableList<ConfigOptionWrapper> = when (tab) {
        ConfigGuiTab.GENERIC -> ConfigOptionWrapper.createFor(GENERIC_TAB)
        ConfigGuiTab.RVC -> ConfigOptionWrapper.createFor(RVC_TAB)
        ConfigGuiTab.MICRO_TICK -> ConfigOptionWrapper.createFor(MICRO_TICK_TAB)
        ConfigGuiTab.SUPER_RIGHT -> ConfigOptionWrapper.createFor(SUPER_RIGHT_TAB)
        ConfigGuiTab.DEBUG -> ConfigOptionWrapper.createFor(DEBUG_TAB)
    }
    override fun useKeybindSearch() = true
    override fun onCharTyped(charIn: Char, modifiers: Int): Boolean {
        return super.onCharTyped(charIn, modifiers)
    }
    enum class ConfigGuiTab(private val translationKey: String) {
        GENERIC("reden.widget.config.generic"),
        RVC("reden.widget.config.rvc"),
        MICRO_TICK("reden.widget.config.micro_tick"),
        SUPER_RIGHT("reden.widget.config.super_right"),
        DEBUG("reden.widget.config.debug"),
        ;

        val displayName: String
            get() = StringUtils.translate(translationKey)
    }
}
