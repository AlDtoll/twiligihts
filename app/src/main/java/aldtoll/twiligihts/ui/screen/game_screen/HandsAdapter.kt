package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.databinding.ItemHandBinding
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Perk
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class HandsAdapter : RecyclerView.Adapter<HandsAdapter.HandHolder>() {

    companion object {
        fun newInstance(callback: Callback, context: Context): HandsAdapter {
            val handsAdapter = HandsAdapter()
            handsAdapter.callback = callback
            handsAdapter.context = context
            return handsAdapter
        }
    }

    lateinit var callback: Callback
    lateinit var context: Context

    interface Callback {

        fun clickPerk(perk: Perk)
    }

    private val differ = AsyncListDiffer(this, HandDiffUtilCallback())

    fun updateData(hands: ArrayList<Hand>) {
        differ.submitList(hands)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HandHolder {
        return HandHolder(
            ItemHandBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: HandHolder, position: Int) {
        val hand = differ.currentList[position]
        holder.bind(hand)
    }

    class HandDiffUtilCallback : DiffUtil.ItemCallback<Hand>() {

        override fun areItemsTheSame(oldItem: Hand, newItem: Hand): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: Hand, newItem: Hand): Boolean {
            return false
        }

    }

    inner class HandHolder(
        private val binding: ItemHandBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(hand: Hand) {
            val perkPriceList = binding.perkPriceList
            val priceAdapter = PriceAdapter()
            perkPriceList.adapter = priceAdapter
            val perk = hand.perks[0]
            priceAdapter.updateData(perk.prices)
            val color = Gem.getColor(
                hand.gemType
            )
            binding.handPerk.setCardBackgroundColor(
                binding.root.resources.getColor(
                    color
                )
            )
            binding.perkEnable.setBackgroundColor(
                binding.root.resources.getColor(
                    color
                )
            )
            binding.perkDescription.text = perk.description
            binding.perkEnable.visibility = if (perk.enable) {
                View.VISIBLE
            } else {
                View.GONE
            }
            binding.root.setOnClickListener {
                if (binding.perkEnable.visibility == View.VISIBLE) {
                    callback.clickPerk(perk)
                }
            }
        }
    }

}