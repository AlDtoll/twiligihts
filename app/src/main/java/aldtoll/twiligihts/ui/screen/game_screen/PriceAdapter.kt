package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.databinding.ItemPriceBinding
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Perk
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class PriceAdapter : RecyclerView.Adapter<PriceAdapter.PriceHolder>() {

    private val differ = AsyncListDiffer(this, PriceDiffUtilCallback())

    fun updateData(prices: ArrayList<Perk.Price>) {
        differ.submitList(prices)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PriceAdapter.PriceHolder {
        return PriceHolder(
            ItemPriceBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: PriceAdapter.PriceHolder, position: Int) {
        val price = differ.currentList[position]
        holder.bind(price)
    }

    class PriceDiffUtilCallback : DiffUtil.ItemCallback<Perk.Price>() {

        override fun areItemsTheSame(oldItem: Perk.Price, newItem: Perk.Price): Boolean {
            return false
        }

        override fun areContentsTheSame(
            oldItem: Perk.Price,
            newItem: Perk.Price
        ): Boolean {
            return false
        }

    }

    inner class PriceHolder(
        private val binding: ItemPriceBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(price: Perk.Price) {
            binding.priceValue.text = price.value.toString()
            binding.priceType.setBackgroundColor(binding.root.resources.getColor(Gem.getColor(price.gemType)))
        }
    }
}