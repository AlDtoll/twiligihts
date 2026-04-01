package aldtoll.twiligihts.ui.screen.game_screen.effects

import aldtoll.twiligihts.model.CrushedCell
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.ui.screen.game_screen.adapter.StockAdapter
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.min
import kotlin.random.Random

data class CrushedGemVisualInfo(
    val row: Int,
    val col: Int,
    val gemType: Int,
)

class GemSparkEffectManager(
    private val effectsLayer: ViewGroup,
    private val gameBoardRecyclerView: RecyclerView,
    private val heroStockList: RecyclerView,
    private val enemyBlock: View?,
) {

    var enableGemSparkEffects: Boolean = true

    fun playSparksForCrushedGems(
        crushedGems: List<CrushedGemVisualInfo>,
        sourceCells: List<CrushedCell>,
        heroTurn: Boolean,
    ) {
        if (!enableGemSparkEffects) return
        if (crushedGems.isEmpty()) return

        val maxSparks = 8
        val limited = crushedGems.shuffled().take(min(maxSparks, crushedGems.size))

        limited.forEach { info ->
            val sourceView =
                gameBoardRecyclerView.findViewHolderForAdapterPosition(
                    info.row * getRowSize() + info.col,
                )?.itemView ?: return@forEach

            val targetView =
                if (heroTurn) {
                    findStockViewForGem(info.gemType)
                } else {
                    enemyBlock
                } ?: return@forEach

            val (startX, startY) = getCenterInEffectsLayer(sourceView)
            val (endX, endY) = getCenterInEffectsLayer(targetView)

            createAndAnimateSpark(info.gemType, startX, startY, endX, endY)
        }
    }

    private fun getRowSize(): Int {
        val adapter = gameBoardRecyclerView.adapter ?: return 1
        val itemCount = adapter.itemCount
        val layoutManager = gameBoardRecyclerView.layoutManager
        val spanCount =
            (layoutManager as? androidx.recyclerview.widget.GridLayoutManager)?.spanCount
        return spanCount ?: kotlin.math.sqrt(itemCount.toDouble()).toInt().coerceAtLeast(1)
    }

    private fun findStockViewForGem(gemType: Int): View? {
        val adapter = heroStockList.adapter as? StockAdapter ?: return null
        val position = adapter.findPositionForGemType(gemType) ?: return null
        return heroStockList.findViewHolderForAdapterPosition(position)?.itemView
    }

    private fun getCenterInEffectsLayer(view: View): Pair<Float, Float> {
        val location = IntArray(2)
        val rootLocation = IntArray(2)
        view.getLocationOnScreen(location)
        effectsLayer.getLocationOnScreen(rootLocation)
        val x = location[0] - rootLocation[0] + view.width / 2f
        val y = location[1] - rootLocation[1] + view.height / 2f
        return x to y
    }

    private fun createAndAnimateSpark(
        gemType: Int,
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
    ) {
        val spark = ImageView(effectsLayer.context)
        // Чуть крупнее искры
        val size = (26 * effectsLayer.resources.displayMetrics.density).toInt()
        val layoutParams = ViewGroup.LayoutParams(size, size)
        spark.layoutParams = layoutParams

        spark.setColorFilter(
            ContextCompat.getColor(
                effectsLayer.context,
                Gem.getColor(gemType),
            ),
            android.graphics.PorterDuff.Mode.SRC_IN,
        )

        // Используем простую круглую \"искорку\" вместо иконки гема
        spark.setImageResource(aldtoll.twiligihts.R.drawable.ic_circle_spark)

        spark.x = startX
        spark.y = startY
        spark.alpha = 1f
        spark.scaleX = 0.6f
        spark.scaleY = 0.6f

        effectsLayer.addView(spark)

        // Псевдослучайная более изогнутая траектория (квадратичная Bezier)
        val distance = kotlin.math.hypot((endX - startX), (endY - startY))
        val baseCurve = distance * 0.4f
        val controlOffsetX = (Random.nextFloat() * 2f - 1f) * baseCurve
        val controlOffsetY = -baseCurve - Random.nextFloat() * baseCurve
        val controlX = (startX + endX) / 2f + controlOffsetX
        val controlY = (startY + endY) / 2f + controlOffsetY

        val flightAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1650L
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { valueAnimator ->
                val t = valueAnimator.animatedValue as Float
                val oneMinusT = 1f - t
                val bx =
                    oneMinusT * oneMinusT * startX +
                            2f * oneMinusT * t * controlX +
                            t * t * endX
                val by =
                    oneMinusT * oneMinusT * startY +
                            2f * oneMinusT * t * controlY +
                            t * t * endY
                spark.x = bx
                spark.y = by
            }
        }

        val scaleX =
            ObjectAnimator.ofFloat(spark, View.SCALE_X, 0.6f, 1.15f)
        val scaleY =
            ObjectAnimator.ofFloat(spark, View.SCALE_Y, 0.6f, 1.15f)

        val animatorSet = AnimatorSet().apply {
            playTogether(flightAnimator, scaleX, scaleY)
            duration = 1650L
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    effectsLayer.removeView(spark)
                }

                override fun onAnimationCancel(animation: Animator) {
                    effectsLayer.removeView(spark)
                }

                override fun onAnimationRepeat(animation: Animator) {}
            })
        }
        animatorSet.start()
    }
}

