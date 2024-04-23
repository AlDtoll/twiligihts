package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.R
import aldtoll.twiligihts.databinding.ItemPerkBinding
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Perk
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage

class PerksAdapter : RecyclerView.Adapter<PerksAdapter.PerkHolder>() {

    companion object {
        fun newInstance(
            callback: Callback,
            context: Context,
            perkRecyclerView: RecyclerView
        ): PerksAdapter {
            val perksAdapter = PerksAdapter()
            perksAdapter.callback = callback
            perksAdapter.context = context
            perksAdapter.perkRecyclerView = perkRecyclerView
            return perksAdapter
        }
    }

    lateinit var callback: Callback
    lateinit var context: Context
    lateinit var perkRecyclerView: RecyclerView

    interface Callback {

        fun clickPerk(perk: Perk)
    }

    val differ = AsyncListDiffer(this, PerkDiffUtilCallback())

    fun updateData(perks: ArrayList<Perk>) {
        differ.submitList(perks)
    }

    fun findHolder(perk: Perk): Pair<PerkHolder, Int>? {
        // Iterate through the currently bound view holders in the RecyclerView
        for (i in 0 until itemCount) {
            // Get the data associated with the view holder at the current position
            val currentPerk = differ.currentList[i]

            // Check if the current perk matches the perk you're looking for
            if (currentPerk.isSame(perk)) {
                // Get the view holder associated with the current position
                val viewHolder = perkRecyclerView.findViewHolderForAdapterPosition(i) as PerkHolder
                return Pair(viewHolder, i)
            }
        }
        return null // If no matching view holder is found, return null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PerkHolder {
        return PerkHolder(
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

    override fun onBindViewHolder(holder: PerkHolder, position: Int) {
        val hand = differ.currentList[position]
        holder.bind(hand)
    }

    class PerkDiffUtilCallback : DiffUtil.ItemCallback<Perk>() {

        override fun areItemsTheSame(oldItem: Perk, newItem: Perk): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Perk, newItem: Perk): Boolean {
            return oldItem == newItem
        }

    }

    inner class PerkHolder(
        val binding: ItemPerkBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(perk: Perk) {
            if (perk.show) {
                binding.perkBlock.visibility = View.VISIBLE
                val perkPriceList = binding.perkPriceList
                val priceAdapter = PriceAdapter()
                perkPriceList.adapter = priceAdapter
                priceAdapter.updateData(perk.prices)
                val color = if (perk.prices.isEmpty()) {
                    1
                } else {
                    Gem.getColor(
                        perk.prices[0].gemType
                    )
                }
                if (perk.coolDown != null) {
                    binding.perkReload.visibility = View.VISIBLE
                    binding.perkReload.text = "${perk.reload}/${perk.coolDown}"
                    val drawableRes = if (perk.reloadType == Perk.ReloadType.TURN) {
                        R.drawable.hourglass
                    } else {
                        0
                    }
                    binding.perkReload.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0,
                        drawableRes, 0
                    )
                } else {
                    binding.perkReload.visibility = View.GONE
                }
                binding.perkBlock.setCardBackgroundColor(
                    binding.root.resources.getColor(
                        color
                    )
                )
                binding.perkEnable.setBackgroundColor(
                    binding.root.resources.getColor(
                        color
                    )
                )
                binding.perkName.text = perk.nameForDisplay()
                binding.perkDescription.text = perk.description
                if (perk.currentCharges != null) {
                    binding.perkCharges.text = "Использований: ${perk.currentCharges}"
                    binding.perkCharges.visibility = View.VISIBLE
                } else {
                    binding.perkCharges.visibility = View.GONE
                }
                if (perk.resources.isNotEmpty()) {
                    var text = "Требует: "
                    perk.resources.forEach {
                        text += "${it.name} ${it.amount};"
                    }
                    binding.perkResources.text = text
                    binding.perkResources.visibility = View.VISIBLE
                } else {
                    binding.perkResources.visibility = View.GONE
                }
                val storage = FirebaseStorage.getInstance()
                perk.icon?.run {
                    val s = Perk.PERK_MAP[perk.icon]
                    if (s.isNullOrEmpty()) {
                        Glide.with(binding.root.context)
                            .load(s)
                            .placeholder(Gem.getPlaceHolder(color))
                            .timeout(60000)
                            .into(binding.perkIcon)
                        val gsReference = storage.reference.child("${perk.icon}.png")
                        gsReference.downloadUrl
                            .addOnSuccessListener { uri ->
                                Perk.PERK_MAP[perk.icon] = uri.toString()
                                Glide.with(binding.root.context)
                                    .load(s)
                                    .placeholder(Gem.getPlaceHolder(color))
                                    .timeout(60000)
                                    .into(binding.perkIcon)
                            }
                    } else {
                        Glide.with(binding.root.context)
                            .load(s)
                            .placeholder(Gem.getPlaceHolder(color))
                            .timeout(60000)
                            .into(binding.perkIcon)
                    }
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
                binding.perkBlock.visibility = View.GONE
            }
        }
    }

}