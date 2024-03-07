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
import com.bumptech.glide.Glide

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
    var savedPerks: ArrayList<Perk>? = null

    interface Callback {

        fun clickPerk(perk: Perk) {}

        fun showOrHidePerksForHand(perks: ArrayList<Perk>, notChangeVisibility: Boolean = false) {}
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
            return oldItem == newItem
        }

    }

    inner class HandHolder(
        private val binding: ItemHandBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(hand: Hand) {
            val perksForDisplay = hand.perks.filter { it.show }
            if (perksForDisplay.isNotEmpty()) {
                binding.handBlock.visibility = View.VISIBLE
                val color = Gem.getColor(
                    hand.gemType
                )
                binding.handBlock.setCardBackgroundColor(
                    binding.root.resources.getColor(
                        color
                    )
                )
                if (perksForDisplay.size == 1) {
                    binding.handIcon.visibility = View.GONE
                    binding.perkPriceList.visibility = View.VISIBLE
                    val perkPriceList = binding.perkPriceList
                    val priceAdapter = PriceAdapter()
                    perkPriceList.adapter = priceAdapter
                    val perk = perksForDisplay[0]
                    priceAdapter.updateData(perk.prices)
                    binding.perkEnable.setBackgroundColor(
                        binding.root.resources.getColor(
                            color
                        )
                    )
                    binding.handName.text = perk.name
                    binding.handDescription.text = perk.description
                    binding.handDescription.visibility = View.VISIBLE
                    if (perk.currentCharges != null) {
                        binding.perkCharges.text = "Зарядов: ${perk.currentCharges}"
                        binding.perkCharges.visibility = View.VISIBLE
                    } else {
                        binding.perkCharges.visibility = View.GONE
                    }
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
                } else {
                    binding.perkPriceList.visibility = View.GONE
                    binding.handDescription.visibility = View.GONE
                    binding.perkEnable.visibility = View.GONE
                    binding.perkCharges.visibility = View.GONE
                    binding.handName.text = hand.name
                    hand.description?.run {
                        binding.handDescription.text = this
                        binding.handDescription.visibility = View.VISIBLE
                    }
                    binding.handIcon.visibility = View.VISIBLE
                    Glide.with(binding.root.context)
                        .load(Gem.getIconUri(hand.gemType))
                        .timeout(60000)
                        .into(binding.handIcon)
                    binding.root.setOnClickListener {
                        savedPerks = hand.perks
                        callback.showOrHidePerksForHand(hand.perks)
                    }
                }
            } else {
                binding.handBlock.visibility = View.GONE
            }
        }
    }

}