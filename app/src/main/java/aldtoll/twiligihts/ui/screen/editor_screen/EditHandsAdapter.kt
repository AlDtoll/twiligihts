package aldtoll.twiligihts.ui.screen.editor_screen

import aldtoll.twiligihts.databinding.ItemEditHandBinding
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.characters.Person
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class EditHandsAdapter(
) : RecyclerView.Adapter<EditHandsAdapter.HandHolder>() {

    companion object {
        fun newInstance() = EditHandsAdapter()
    }

    private val differ = AsyncListDiffer(this, HandDiffUtilCallback())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HandHolder {
        return HandHolder(
            ItemEditHandBinding.inflate(
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
        val person = differ.currentList[position]
        holder.bind(person)
    }

    fun updateData(persons: ArrayList<Hand>) {
        differ.submitList(persons)
    }

    fun getData(): ArrayList<Hand> = ArrayList(differ.currentList)


    class HandDiffUtilCallback : DiffUtil.ItemCallback<Hand>() {

        override fun areItemsTheSame(oldItem: Hand, newItem: Hand): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: Hand, newItem: Hand): Boolean {
            return false
        }

    }

    inner class HandHolder(
        private val binding: ItemEditHandBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(hand: Hand) {
            binding.name.setText(hand.name)
            binding.description.setText(hand.description)
            binding.gemType.setText(hand.gemType.toString())
            binding.block.setBackgroundColor(binding.root.resources.getColor(Gem.getColor(hand.gemType)))
            val perksList = binding.perksList
            val editPerkAdapter = EditPerkAdapter()
            perksList.adapter = editPerkAdapter
            editPerkAdapter.updateData(hand.perks)
            binding.addPerk.setOnClickListener {
                editPerkAdapter.addData()
            }
            binding.save.setOnClickListener {
                val newHand = Hand(
                    binding.name.text.toString(),
                    binding.description.text.toString(),
                    binding.gemType.text.toString().toInt(),
                    arrayListOf()
                )
                updateData(arrayListOf(newHand))
            }
        }
    }

    interface Callback {
        fun savePerson(): Person
    }
}