package aldtoll.twiligihts.ui.screen.game_screen.adapter

import aldtoll.twiligihts.R
import aldtoll.twiligihts.databinding.ItemCellBinding
import aldtoll.twiligihts.model.GameBoard
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

/**
 * Адаптер для нижнего слоя – ячеек (зоны, множители).
 * Здесь нет анимаций и логики свайпов, только отображение свойств CellState.
 */
class CellBoardAdapter(
    private val context: Context,
    private val gameBoard: GameBoard,
) : RecyclerView.Adapter<CellBoardAdapter.CellHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellHolder {
        return CellHolder(
            ItemCellBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CellHolder, position: Int) {
        val row = position / gameBoard.rowSize
        val col = position % gameBoard.rowSize
        val cellState = gameBoard.getCell(row, col)

        if (cellState.scoreMultiplier != 1f) {
            holder.cellZoneOverlay.visibility = View.VISIBLE
            holder.cellZoneOverlay.setBackgroundColor(
                ContextCompat.getColor(context, R.color.good_cell)
            )
            holder.cellZoneLabel.visibility = View.VISIBLE
            holder.cellZoneLabel.text = "x${cellState.scoreMultiplier}"
        } else {
            holder.cellZoneOverlay.visibility = View.GONE
            holder.cellZoneLabel.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return gameBoard.columnSize * gameBoard.rowSize
    }

    inner class CellHolder(val binding: ItemCellBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val cellZoneOverlay = binding.cellZoneOverlay
        val cellZoneLabel = binding.cellZoneLabel
    }
}

