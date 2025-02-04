package tech.thatgravyboat.craftify.ui

import gg.essential.api.utils.GuiUtil
import gg.essential.elementa.components.*
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.vigilance.gui.VigilancePalette
import tech.thatgravyboat.craftify.config.Config
import tech.thatgravyboat.craftify.lib.SingleImageCache
import tech.thatgravyboat.craftify.platform.runOnMcThread
import tech.thatgravyboat.craftify.themes.ThemeConfig
import tech.thatgravyboat.craftify.types.PlayerState
import tech.thatgravyboat.craftify.ui.constraints.ConfigColorConstraint
import tech.thatgravyboat.craftify.ui.enums.Anchor
import java.net.URL

class UIPlayer : UIBlock(ConfigColorConstraint("background")) {

    init {
        constrain {
            height = 50.pixel()
            width = 150.pixel()
            y = 0.percent()
            x = 0.percent()
        }

        onMouseEnter {
            enableEffect(OutlineEffect(ThemeConfig.borderColor, 1F, drawInsideChildren = true))
            if (Config.premiumControl) {
                setHeight(63.pixel())
                val pos = Anchor.values()[Config.anchorPoint]
                if (pos.ordinal > 4) setY(pos.getY(this) - 13.pixels())
                this.addChild(controls)
            }
        }

        onMouseLeave {
            removeEffect<OutlineEffect>()
            setHeight(50.pixel())
            val pos = Anchor.values()[Config.anchorPoint]
            if (pos.ordinal > 4) setY(pos.getY(this))
            this.removeChild(controls)
        }
    }

    private var imageUrl = ""

    private val image = UIBlock(VigilancePalette.getHighlight()).constrain {
        height = 40.pixel()
        width = 40.pixel()
        x = 5.pixel()
        y = 5.pixel()
    } childOf this

    private val info = UIContainer().constrain {
        width = 95.pixel()
        height = 40.pixel()
        x = 50.pixel()
        y = 5.pixel()
    } childOf this

    private val title = UITextMarquee(text = "Song Title").constrain {
        width = 100.percent()
        height = 10.pixel()
        color = ConfigColorConstraint("title")
    } childOf info

    private val artist by lazy { UIWrappedText("Artists, here").constrain {
        width = 100.percent()
        height = 10.pixel()
        y = 12.pixel()
        textScale = 0.5.pixel()
        color = ConfigColorConstraint("artist")
    } childOf info }

    private val progress = UIProgressBar().constrain {
        width = 100.percent()
        height = 3.pixel()
        y = 40.pixel() - 3.pixel()
    } childOf info

    private val controls = UIControls().constrain {
        width = 140.pixels()
        height = 10.pixels()
        y = 50.pixel()
        x = 5.pixels()
    }

    fun clientStop() {
        progress.tempStop()
    }

    fun updateState(state: PlayerState) {
        runOnMcThread {
            progress.updateTime(state.getTime(), state.getEndTime())
            artist.setText(state.getArtists())
            title.updateText(state.getTitle())
            controls.updateState(state)

            try {
                if (imageUrl != state.getImage()) {
                    imageUrl = state.getImage()
                    image.clearChildren()
                    image.addChild(
                        UIImage.ofURL(URL(state.getImage()), SingleImageCache).constrain {
                            width = 100.percent()
                            height = 100.percent()
                        }
                    )
                }
            } catch (ignored: Exception) {
                // Ignoring exception due that it would be that Spotify sent a broken url.
            }
        }
    }

    fun updateTheme() {
        progress.updateTheme()
        controls.updateTheme()
    }

    override fun isHovered(): Boolean {
        return GuiUtil.getOpenedScreen() != null && super.isHovered()
    }
}
