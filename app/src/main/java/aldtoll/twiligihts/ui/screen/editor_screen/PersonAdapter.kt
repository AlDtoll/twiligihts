package aldtoll.twiligihts.ui.screen.editor_screen

import aldtoll.twiligihts.databinding.ItemEditPersonBinding
import aldtoll.twiligihts.model.characters.Hero
import aldtoll.twiligihts.model.characters.Person
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class PersonAdapter(
) : RecyclerView.Adapter<PersonAdapter.PersonHolder>() {

    companion object {
        fun newInstance() = PersonAdapter()
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
        val person = differ.currentList[position]
        holder.bind(person)
    }

    fun updateData(persons: ArrayList<Person>) {
        differ.submitList(persons)
    }

    fun getData(): ArrayList<Person> = ArrayList(differ.currentList)


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
            binding.name.setText(person.name)
            binding.hp.setText(person.hp.toString())
            binding.maxHp.setText(person.maxHp.toString())
            binding.sp.setText(person.shield.toString())
            binding.savePerson.setOnClickListener {
                val hero = Hero(
                    binding.name.text.toString(),
                    binding.hp.text.toString().toInt(),
                    binding.maxHp.text.toString().toInt(),
                    binding.sp.text.toString().toInt(),
                    0,
                    0,
                    arrayListOf()
                )
                updateData(arrayListOf(hero))
            }
        }
    }

    interface Callback {
        fun savePerson(): Person
    }
}