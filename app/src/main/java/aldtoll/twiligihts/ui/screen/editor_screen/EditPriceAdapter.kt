package aldtoll.twiligihts.ui.screen.editor_screen

import aldtoll.twiligihts.databinding.ItemEditPriceBinding
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.characters.Person
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class EditPriceAdapter : RecyclerView.Adapter<EditPriceAdapter.PriceHolder>() {

    companion object {
        fun newInstance() = EditPriceAdapter()
    }

    private val differ = AsyncListDiffer(this, PriceDiffUtilCallback())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PriceHolder {
        return PriceHolder(
            ItemEditPriceBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: PriceHolder, position: Int) {
        val person = differ.currentList[position]
        holder.bind(person)
    }

    fun addData() {
        val data = getData()
        data.add(Perk.Price())
        updateData(data)
    }

    fun updateData(perks: ArrayList<Perk.Price>) {
        differ.submitList(perks)
    }

    fun getData(): ArrayList<Perk.Price> = ArrayList(differ.currentList)


    class PriceDiffUtilCallback : DiffUtil.ItemCallback<Perk.Price>() {

        override fun areItemsTheSame(oldItem: Perk.Price, newItem: Perk.Price): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: Perk.Price, newItem: Perk.Price): Boolean {
            return false
        }

    }

    inner class PriceHolder(
        private val binding: ItemEditPriceBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(price: Perk.Price) {
            binding.priceValue.setText(price.value.toString())
            binding.priceType.setText(price.gemType.toString())
        }
    }

    interface Callback {
        fun savePerson(): Person
    }
}