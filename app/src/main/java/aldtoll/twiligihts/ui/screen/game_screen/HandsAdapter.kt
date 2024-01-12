package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.R
import aldtoll.twiligihts.databinding.ItemHandBinding
import aldtoll.twiligihts.model.Hand
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class HandsAdapter : RecyclerView.Adapter<HandsAdapter.HandHolder>() {

    companion object {
        fun newInstance() = HandsAdapter()
    }

    private val differ = AsyncListDiffer(this, HandDiffUtilCallback())

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