package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.R
import aldtoll.twiligihts.model.Sector
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class SectorAdapter(
    val sectors: List<Sector>,
    private val onSectorSelected: (Int) -> Unit
) : RecyclerView.Adapter<SectorAdapter.SectorViewHolder>() {

    private var selectedPosition = 0

    inner class SectorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.sectorIcon)
        private val name: TextView = itemView.findViewById(R.id.sectorName)
        private val container: LinearLayout = itemView.findViewById(R.id.sectorItem)

        fun bind(sector: Sector, isSelected: Boolean) {
            icon.setImageResource(sector.iconRes)
            name.text = sector.name

            if (sector.backgroundRes != -1) {
                container.setBackgroundResource(sector.backgroundRes)
            }

            // Визуальное выделение выбранного элемента
            container.alpha = if (isSelected) 1f else 0.7f
            name.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    if (isSelected) android.R.color.white else android.R.color.darker_gray
                )
            )

            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onSectorSelected(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sector, parent, false)
        return SectorViewHolder(view)
    }

    override fun onBindViewHolder(holder: SectorViewHolder, position: Int) {
        holder.bind(sectors[position], position == selectedPosition)
    }

    override fun getItemCount() = sectors.size

    fun getSelectedPosition() = selectedPosition

    fun setSelectedPosition(position: Int) {
        val prevSelected = selectedPosition
        selectedPosition = position
        notifyItemChanged(prevSelected)
        notifyItemChanged(selectedPosition)
    }
}