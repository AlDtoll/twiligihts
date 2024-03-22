package aldtoll.twiligihts.ui.screen.editor_screen

import aldtoll.twiligihts.databinding.ItemEditPersonBinding
import aldtoll.twiligihts.model.BattleEvent
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

    private val differ = AsyncListDiffer(this, LogDiffUtilCallback())

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

    fun updateData(events: ArrayList<BattleEvent>) {
        differ.submitList(events)
    }

    class LogDiffUtilCallback : DiffUtil.ItemCallback<BattleEvent>() {

        override fun areItemsTheSame(oldItem: BattleEvent, newItem: BattleEvent): Boolean {
            return oldItem.uuid == newItem.uuid
        }

        override fun areContentsTheSame(oldItem: BattleEvent, newItem: BattleEvent): Boolean {
            return oldItem == newItem
        }

    }

    inner class PersonHolder(
        private val binding: ItemEditPersonBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: BattleEvent) {

        }
    }

    interface Callback {
        fun clickLog()
    }
}