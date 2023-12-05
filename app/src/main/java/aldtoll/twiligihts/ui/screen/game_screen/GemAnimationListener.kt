package aldtoll.twiligihts.ui.screen.game_screen

import android.view.View

interface GemAnimationListener {
    fun animateGemDown(fromPosition: Pair<Int, Int>, toPosition: Pair<Int, Int>)
    fun animateNewGem(position: Pair<Int, Int>, newGemType: Int)
}