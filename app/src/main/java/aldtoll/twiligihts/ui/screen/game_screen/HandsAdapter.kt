package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.databinding.ItemHandBinding
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Hand
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class HandsAdapter : RecyclerView.Adapter<HandsAdapter.HandHolder>() {

    companion object {
        fun newInstance(callback: Callback): HandsAdapter {
            val handsAdapter = HandsAdapter()
            handsAdapter.callback = callback
            return handsAdapter
        }
    }

    lateinit var callback: Callback

    interface Callback {

        fun clickPerk(perk: Hand.Perk)
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
            binding.perkPrice.text = hand.perks[0].prices[0].value.toString()
            binding.handPerk.setCardBackgroundColor(binding.root.resources.getColor(getGemColor(hand.gemType)))
            binding.perkDescription.text = hand.perks[0].description
            binding.root.setOnClickListener {
                callback.clickPerk(hand.perks[0])
            }
        }
    }

    @ColorRes
    private fun getGemColor(gemType: Int): Int {
        return Gem.getColor(gemType)
    }
}