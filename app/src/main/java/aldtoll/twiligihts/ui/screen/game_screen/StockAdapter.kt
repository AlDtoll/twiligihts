package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.databinding.ItemStockBinding
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Stock
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class StockAdapter : RecyclerView.Adapter<StockAdapter.StockHolder>() {

    companion object {
        fun newInstance(callback: Callback): StockAdapter {
            val stockAdapter = StockAdapter()
            stockAdapter.callback = callback
            return stockAdapter
        }
    }

    lateinit var callback: Callback

    interface Callback {

        fun clickStock()
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
            return oldItem.gemType == newItem.gemType
        }

        override fun areContentsTheSame(oldItem: Stock, newItem: Stock): Boolean {
            return oldItem.value == newItem.value
        }

    }

    inner class StockHolder(
        private val binding: ItemStockBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(stock: Stock) {
            binding.root.setOnClickListener {
                callback.clickStock()
            }

            var stockPointsText = "${stock.value}"
            stock.maxValue?.run {
                stockPointsText += "/${stock.maxValue}"
            }
            binding.stockValue.text = stockPointsText
            binding.stockType.setBackgroundColor(binding.root.resources.getColor(Gem.getColor(stock.gemType)))
            Glide.with(binding.root.context)
                .load(Gem.getIconUri(stock.gemType))
                .placeholder(Gem.getPlaceHolder(stock.gemType))
                .timeout(60000)
                .into(binding.stockType)
        }
    }
}