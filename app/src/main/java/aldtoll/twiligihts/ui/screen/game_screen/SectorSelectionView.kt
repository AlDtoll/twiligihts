package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.R
import aldtoll.twiligihts.model.Sector
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SectorSelectionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val recyclerView: RecyclerView
    private val swordCursor: ImageView
    private lateinit var adapter: SectorAdapter
    private var onSectorSelectedListener: ((Sector) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.sector_selection_layout, this, true)
        recyclerView = findViewById(R.id.sectorsRecyclerView)
        swordCursor = findViewById(R.id.swordCursor)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.itemAnimator = null // Отключаем стандартные анимации
    }

    fun setupSectors(sectors: List<Sector>, initialSelection: Int = 0) {
        adapter = SectorAdapter(sectors) { position ->
            selectSector(position, true)
            onSectorSelectedListener?.invoke(sectors[position])
        }
        recyclerView.adapter = adapter
        selectSector(initialSelection, false)
    }

    fun selectSector(position: Int, withAnimation: Boolean = true) {
        if (position !in 0 until adapter.itemCount) return

        adapter.setSelectedPosition(position)
        recyclerView.smoothScrollToPosition(position)

        val selectedView = recyclerView.layoutManager?.findViewByPosition(position)
        selectedView?.let { view ->
            updateSwordPosition(view, withAnimation)
        }
    }

    private fun updateSwordPosition(selectedView: View, animate: Boolean) {
        if (animate) {
            // Анимация перемещения меча
            swordCursor.animate()
//                .x(selectedView.x - swordCursor.width)
                .y(selectedView.y + (selectedView.height - swordCursor.height) / 2)
                .setDuration(200)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

            // Анимация "удара" меча
            swordCursor.animate()
                .scaleX(1.3f)
                .scaleY(1.3f)
                .setDuration(100)
                .withEndAction {
                    swordCursor.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }
                .start()
        } else {
//            swordCursor.x = selectedView.x - swordCursor.width
            swordCursor.y = selectedView.y + (selectedView.height - swordCursor.height) / 2
        }

        swordCursor.visibility = VISIBLE
    }

    fun moveSelectionUp() {
        val current = adapter.getSelectedPosition()
        if (current > 0) {
            selectSector(current - 1)
        }
    }

    fun moveSelectionDown() {
        val current = adapter.getSelectedPosition()
        if (current < adapter.itemCount - 1) {
            selectSector(current + 1)
        }
    }

    fun setOnSectorSelectedListener(listener: (Sector) -> Unit) {
        onSectorSelectedListener = listener
    }

    fun getSelectedSector(): Sector? {
        return adapter.getSelectedPosition().takeIf { it != -1 }?.let {
            (recyclerView.adapter as SectorAdapter).let { adapter ->
                adapter.sectors[it]
            }
        }
    }
}