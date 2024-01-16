package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.databinding.ItemBattleLogBinding
import aldtoll.twiligihts.model.BattleEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class LogAdapter : RecyclerView.Adapter<LogAdapter.LogHolder>() {

    companion object {
        fun newInstance() = LogAdapter()
    }

    private val differ = AsyncListDiffer(this, LogDiffUtilCallback())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogHolder {
        return LogHolder(
            ItemBattleLogBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: LogHolder, position: Int) {
        val stock = differ.currentList[position]
        holder.bind(stock)
    }

    fun updateData(events: ArrayList<BattleEvent>) {
        differ.submitList(events)
    }

    class LogDiffUtilCallback : DiffUtil.ItemCallback<BattleEvent>() {

        override fun areItemsTheSame(oldItem: BattleEvent, newItem: BattleEvent): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: BattleEvent, newItem: BattleEvent): Boolean {
            return false
        }

    }

    inner class LogHolder(
        private val binding: ItemBattleLogBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: BattleEvent) {
            binding.logMessage.text = event.message
        }
    }
}