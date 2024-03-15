package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.databinding.ItemStatusBinding
import aldtoll.twiligihts.model.Status
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(
            oldItem: Status,
            newItem: Status
        ): Boolean {
            return oldItem == newItem
        }

    }

    inner class StatusHolder(
        private val binding: ItemStatusBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(status: Status) {
            if (status.isActive()) {
                binding.statusBlock.visibility = View.VISIBLE
            } else {
                binding.statusBlock.visibility = View.GONE
            }
            binding.statusBlock.setBackgroundColor(
                ContextCompat.getColor(
                    binding.root.context,
                    status.type.color
                )
            )
            binding.statusName.text = "${status.name}: ${status.value}"
            val duration = if (status.isInfinity()) {
                "вечно"
            } else {
                status.duration.toString()
            }
            val textForDuration = "Ходов: $duration"
            binding.statusDuration.text = textForDuration
            if (status.times != null) {
                binding.statusDuration.text = "Зарядов: ${status.times}"
                binding.statusDuration.visibility = View.VISIBLE
            } else {
                binding.statusDuration.visibility = View.GONE
            }
            if (status.description != null) {
                binding.statusDescription.text = status.description
                binding.statusDescription.visibility = View.VISIBLE
            } else {
                binding.statusDescription.visibility = View.GONE
            }
            binding.statusIcon.setImageResource(status.type.image)
        }
    }
}