package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.databinding.ItemStatusBinding
import aldtoll.twiligihts.model.Status
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class StatusAdapter : RecyclerView.Adapter<StatusAdapter.StatusHolder>() {

    companion object {
        fun newInstance() = StatusAdapter()
    }

    private val differ = AsyncListDiffer(this, StatusDiffUtilCallback())

    fun updateData(statuses: ArrayList<Status>) {
        differ.submitList(statuses)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusAdapter.StatusHolder {
        return StatusHolder(
            ItemStatusBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: StatusAdapter.StatusHolder, position: Int) {
        val price = differ.currentList[position]
        holder.bind(price)
    }

    class StatusDiffUtilCallback : DiffUtil.ItemCallback<Status>() {

        override fun areItemsTheSame(oldItem: Status, newItem: Status): Boolean {
            return false
        }

        override fun areContentsTheSame(
            oldItem: Status,
            newItem: Status
        ): Boolean {
            return false
        }

    }

    inner class StatusHolder(
        private val binding: ItemStatusBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(status: Status) {
            binding.statusName.text = status.name
            binding.statusValue.text = status.value.toString()
        }
    }
}