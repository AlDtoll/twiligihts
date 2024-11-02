package aldtoll.twiligihts.ui.screen.game_screen.adapter

import aldtoll.twiligihts.databinding.ItemPerkBinding
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
import com.bumptech.glide.Glide

class HandsAdapter : RecyclerView.Adapter<HandsAdapter.HandHolder>() {

    companion object {
        fun newInstance(
            callback: Callback,
            context: Context,
            recyclerView: RecyclerView
        ): HandsAdapter {
            val handsAdapter = HandsAdapter()
            handsAdapter.callback = callback
            handsAdapter.context = context
            handsAdapter.recyclerView = recyclerView
            return handsAdapter
        }
    }

    lateinit var callback: Callback
    lateinit var context: Context
    lateinit var recyclerView: RecyclerView
    var savedPerks: ArrayList<Perk>? = null

    interface Callback {

        fun showOrHidePerksForHand(perks: ArrayList<Perk>, notChangeVisibility: Boolean = false) {}
    }

    fun findHolder(hand: Hand): Pair<HandHolder, Int>? {
        // Iterate through the currently bound view holders in the RecyclerView
        for (i in 0 until itemCount) {
            // Get the data associated with the view holder at the current position
            val currentHand = differ.currentList[i]

            // Check if the current perk matches the perk you're looking for
            if (currentHand.name == hand.name) {
                // Get the view holder associated with the current position
                val viewHolder = recyclerView.findViewHolderForAdapterPosition(i) as HandHolder
                return Pair(viewHolder, i)
            }
        }
        return null // If no matching view holder is found, return null
    }

    fun findHandPosition(hand: Hand): Int {
        // Iterate through the currently bound view holders in the RecyclerView
        for (i in 0 until itemCount) {
            // Get the data associated with the view holder at the current position
            val currentHand = differ.currentList[i]

            // Check if the current perk matches the perk you're looking for
            if (currentHand.name == hand.name) {
                // Get the view holder associated with the current position
                return i
            }
        }
        return 0
    }


    fun refreshPerks() {
        savedPerks?.run {
            callback.showOrHidePerksForHand(this, true)
        }
    }

    private val differ = AsyncListDiffer(this, HandDiffUtilCallback())

    fun updateData(hands: ArrayList<Hand>) {
        differ.submitList(hands)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HandHolder {
        return HandHolder(
            ItemPerkBinding.inflate(
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
            return oldItem == newItem
        }

    }

    inner class HandHolder(
        val binding: ItemPerkBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(hand: Hand) {
            val perksForDisplay = hand.perks.filter { it.show }
            if (hand.show && perksForDisplay.isNotEmpty()) {
                binding.perkBlock.visibility = View.VISIBLE
                val color = Gem.getColor(
                    hand.gemType
                )
                binding.perkBlock.setCardBackgroundColor(
                    binding.root.resources.getColor(
                        color
                    )
                )
                binding.perkPriceList.visibility = View.GONE
                binding.perkReload.visibility = View.GONE
                binding.perkDescription.visibility = View.GONE
                binding.perkEnable.visibility = View.GONE
                binding.perkCharges.visibility = View.GONE
                binding.perkResources.visibility = View.GONE
                binding.perkName.text = hand.name
                hand.description?.run {
                    binding.perkDescription.text = this
                    binding.perkDescription.visibility = View.VISIBLE
                }
                binding.perkIcon.visibility = View.VISIBLE
                Glide.with(binding.root.context)
                    .load(Gem.getIconUri(hand.gemType))
                    .placeholder(Gem.getPlaceHolder(hand.gemType))
                    .timeout(60000)
                    .into(binding.perkIcon)
                binding.root.setOnClickListener {
                    savedPerks = hand.perks
                    callback.showOrHidePerksForHand(hand.perks)
                }
            } else {
                binding.perkBlock.visibility = View.GONE
            }
        }
    }

}