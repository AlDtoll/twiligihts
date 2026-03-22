package aldtoll.twiligihts.ui.screen.game_screen.adapter

import aldtoll.twiligihts.R
import aldtoll.twiligihts.databinding.ItemCellBinding
import aldtoll.twiligihts.model.CellType
import aldtoll.twiligihts.model.GameBoard
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

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

        fun formatModifierValue(value: Float): String {
            // Убираем хвост ".0" для целых значений, но оставляем дроби типа 1.5
            return if (abs(value % 1f) < 0.0001f) {
                value.toInt().toString()
            } else {
                value.toString()
            }
        }

        val hasTrigger = cellState.cellType == CellType.TRIGGER
        holder.cellTriggerLabel.visibility = if (hasTrigger) View.VISIBLE else View.GONE
        if (hasTrigger) {
            holder.cellTriggerLabel.setBackgroundColor(
                ContextCompat.getColor(context, R.color.log_color)
            )
        }

        when (cellState.cellType) {
            CellType.MULTIPLIER -> {
                val isNegative = cellState.modifierValue < 1f
                holder.cellZoneOverlay.visibility = View.VISIBLE
                holder.cellZoneOverlay.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        if (isNegative) R.color.bad_cell else R.color.good_cell
                    )
                )
                holder.cellZoneLabel.visibility = View.VISIBLE
                holder.cellZoneLabel.text = "x${formatModifierValue(cellState.modifierValue)}"
            }

            CellType.ADDITIVE -> {
                val isNegative = cellState.modifierValue < 0f
                holder.cellZoneOverlay.visibility = View.VISIBLE
                holder.cellZoneOverlay.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        if (isNegative) R.color.bad_cell else R.color.good_cell
                    )
                )
                holder.cellZoneLabel.visibility = View.VISIBLE
                val sign = if (cellState.modifierValue >= 0f) "+" else ""
                holder.cellZoneLabel.text = "$sign${formatModifierValue(cellState.modifierValue)}"
            }

            CellType.TRIGGER -> {
                holder.cellZoneOverlay.visibility = View.VISIBLE
                val isNegative = cellState.modifierValue < 0f
                holder.cellZoneOverlay.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        if (isNegative) R.color.light_red_background_color else R.color.light_green_background_color
                    )
                )
                holder.cellZoneLabel.visibility = View.VISIBLE
                if (cellState.modifierValue == 0f) {
                    holder.cellZoneLabel.text = "TRG"
                } else {
                    val sign = if (cellState.modifierValue >= 0f) "+" else ""
                    holder.cellZoneLabel.text =
                        "$sign${formatModifierValue(cellState.modifierValue)}"
                }
            }

            CellType.NONE -> {
                holder.cellZoneOverlay.visibility = View.GONE
                holder.cellZoneLabel.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return gameBoard.columnSize * gameBoard.rowSize
    }

    inner class CellHolder(val binding: ItemCellBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val cellZoneOverlay = binding.cellZoneOverlay
        val cellZoneLabel = binding.cellZoneLabel
        val cellTriggerLabel = binding.cellTriggerLabel
    }
}

