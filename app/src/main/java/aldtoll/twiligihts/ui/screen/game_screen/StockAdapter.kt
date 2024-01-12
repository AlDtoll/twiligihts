package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.R
import aldtoll.twiligihts.databinding.ItemStockBinding
import aldtoll.twiligihts.model.Stock
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class StockAdapter : RecyclerView.Adapter<StockAdapter.StockHolder>() {

    companion object {
        fun newInstance() = StockAdapter()
    }

    private val differ = AsyncListDiffer(this, StockDiffUtilCallback())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockHolder {
        return StockHolder(
            ItemStockBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: StockHolder, position: Int) {
        val stock = differ.currentList[position]
        holder.bind(stock)
    }

    fun updateData(stocks: ArrayList<Stock>) {
        differ.submitList(stocks)
    }

    class StockDiffUtilCallback : DiffUtil.ItemCallback<Stock>() {

        override fun areItemsTheSame(oldItem: Stock, newItem: Stock): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: Stock, newItem: Stock): Boolean {
            return false
        }

    }

    inner class StockHolder(
        private val binding: ItemStockBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(stock: Stock) {
            binding.stockValue.text = stock.value.toString()
            binding.stockType.setBackgroundColor(binding.root.resources.getColor(getGemColor(stock.gemType)))
        }
    }

    @ColorRes
    private fun getGemColor(gemType: Int): Int {
        return when (gemType) {
            1 -> R.color.gem_color_1
            2 -> R.color.gem_color_2
            3 -> R.color.gem_color_3
            4 -> R.color.gem_color_4
            else -> R.color.default_color
        }
    }
}