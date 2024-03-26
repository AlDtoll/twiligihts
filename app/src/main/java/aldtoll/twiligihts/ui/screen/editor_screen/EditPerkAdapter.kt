package aldtoll.twiligihts.ui.screen.editor_screen

import aldtoll.twiligihts.databinding.ItemEditPerkBinding
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.characters.Person
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class EditPerkAdapter(
) : RecyclerView.Adapter<EditPerkAdapter.PerkHolder>() {

    companion object {
        fun newInstance() = EditPerkAdapter()
    }

    private val differ = AsyncListDiffer(this, PerkDiffUtilCallback())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PerkHolder {
        return PerkHolder(
            ItemEditPerkBinding.inflate(
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
        val person = differ.currentList[position]
        holder.bind(person)
    }

    fun addData() {
        val data = getData()
        data.add(Perk())
        updateData(data)
    }

    fun updateData(perks: ArrayList<Perk>) {
        differ.submitList(perks)
    }

    fun getData(): ArrayList<Perk> = ArrayList(differ.currentList)


    class PerkDiffUtilCallback : DiffUtil.ItemCallback<Perk>() {

        override fun areItemsTheSame(oldItem: Perk, newItem: Perk): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: Perk, newItem: Perk): Boolean {
            return false
        }

    }

    inner class PerkHolder(
        private val binding: ItemEditPerkBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(perk: Perk) {
            binding.name.setText(perk.name)
            binding.description.setText(perk.description)
            val priceList = binding.priceList
            val editPriceAdapter = EditPriceAdapter()
            priceList.adapter = editPriceAdapter
            editPriceAdapter.updateData(perk.prices)
            binding.addPrice.setOnClickListener {
                editPriceAdapter.addData()
            }
            binding.save.setOnClickListener {
            }
        }
    }

    interface Callback {
        fun savePerson(): Person
    }
}