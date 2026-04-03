package aldtoll.twiligihts.ui.screen.game_screen.adapter

import aldtoll.twiligihts.databinding.ItemBattleLogBinding
import aldtoll.twiligihts.model.BattleEvent
import aldtoll.twiligihts.model.Gem
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class LogAdapter(
    private val callback: Callback,
    private val textSelectable: Boolean = false,
) : RecyclerView.Adapter<LogAdapter.LogHolder>() {

    companion object {
        fun newInstance(callback: Callback, textSelectable: Boolean = false) =
            LogAdapter(callback, textSelectable)
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
            binding.logMessage.setTextIsSelectable(textSelectable)
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
            if (textSelectable) {
                binding.root.setOnClickListener(null)
                binding.root.isClickable = false
            } else {
                binding.root.isClickable = true
                binding.root.setOnClickListener {
                    callback.clickLog()
                }
            }
        }
    }

    interface Callback {
        fun clickLog()
    }
}