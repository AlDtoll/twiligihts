package aldtoll.twiligihts.ui.screen.editor_screen

import aldtoll.twiligihts.databinding.ItemEditPersonBinding
import aldtoll.twiligihts.model.characters.Person
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class PersonAdapter(
    val callback: Callback
) : RecyclerView.Adapter<PersonAdapter.PersonHolder>() {

    companion object {
        fun newInstance(callback: Callback) = PersonAdapter(callback)
    }

    private val differ = AsyncListDiffer(this, PersonDiffUtilCallback())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonHolder {
        return PersonHolder(
            ItemEditPersonBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: PersonHolder, position: Int) {
        val stock = differ.currentList[position]
        holder.bind(stock)
    }

    fun updateData(events: ArrayList<Person>) {
        differ.submitList(events)
    }

    class PersonDiffUtilCallback : DiffUtil.ItemCallback<Person>() {

        override fun areItemsTheSame(oldItem: Person, newItem: Person): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: Person, newItem: Person): Boolean {
            return false
        }

    }

    inner class PersonHolder(
        private val binding: ItemEditPersonBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(person: Person) {

        }
    }

    interface Callback {
        fun clickLog()
    }
}