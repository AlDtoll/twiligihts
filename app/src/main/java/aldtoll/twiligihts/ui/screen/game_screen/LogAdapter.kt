package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.databinding.ItemBattleLogBinding
import aldtoll.twiligihts.model.BattleEvent
import aldtoll.twiligihts.model.Gem
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class LogAdapter(
    val callback: Callback
) : RecyclerView.Adapter<LogAdapter.LogHolder>() {

    companion object {
        fun newInstance(callback: Callback) = LogAdapter(callback)
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
            return oldItem.uuid == newItem.uuid
        }

        override fun areContentsTheSame(oldItem: BattleEvent, newItem: BattleEvent): Boolean {
            return oldItem == newItem
        }

    }

    inner class LogHolder(
        private val binding: ItemBattleLogBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: BattleEvent) {
            binding.logMessage.text = event.message
            if (event.gemType != 0) {
                binding.logMessage.setTextColor(binding.root.resources.getColor(Gem.getColor(event.gemType)))
            } else {
                binding.logMessage.setTextColor(
                    binding.root.resources.getColor(
                        Gem.getColor(
                            Gem.GRAY_LOG_COLOR
                        )
                    )
                )
            }
            binding.root.setOnClickListener {
                callback.clickLog()
            }
        }
    }

    interface Callback {
        fun clickLog()
    }
}