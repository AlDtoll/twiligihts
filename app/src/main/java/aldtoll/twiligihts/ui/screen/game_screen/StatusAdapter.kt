package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.R
import aldtoll.twiligihts.databinding.ItemStatusBinding
import aldtoll.twiligihts.model.Status
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
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
                    getStatusColor(status.type)
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
            if (status.description != null) {
                binding.statusDescription.text = status.description
                binding.statusDescription.visibility = View.VISIBLE
            } else {
                binding.statusDescription.visibility = View.GONE
            }
            binding.statusIcon.setImageResource(getStatusIcon(status.type))
        }
    }

    @DrawableRes
    private fun getStatusIcon(type: Status.EffectType): Int {
        return when (type) {
            Status.EffectType.DODGE -> R.drawable.ic_dodge
            Status.EffectType.SMART_DODGE -> R.drawable.ic_dodge
            Status.EffectType.GAIN -> R.drawable.ic_gain
            Status.EffectType.REDUCE -> R.drawable.ic_reduce
            Status.EffectType.WEAK -> R.drawable.ic_weak
            Status.EffectType.STRONG -> R.drawable.ic_strong
            Status.EffectType.VULNERABLE -> R.drawable.ic_vul
            Status.EffectType.ARMOR -> R.drawable.ic_armor
            Status.EffectType.COUNTERATTACK -> R.drawable.ic_counterattack
            Status.EffectType.HARM -> R.drawable.ic_spikes
            Status.EffectType.INFO -> R.drawable.ic_info
            Status.EffectType.DAMAGE -> R.drawable.ic_damage
            Status.EffectType.HEAL -> R.drawable.ic_heal
            Status.EffectType.GENERATE -> R.drawable.ic_generate
        }
    }

    @ColorRes
    private fun getStatusColor(type: Status.EffectType): Int {
        return when (type) {
            Status.EffectType.DODGE -> R.color.light_green_background_color
            Status.EffectType.SMART_DODGE -> R.color.light_green_background_color
            Status.EffectType.GAIN -> R.color.light_green_background_color
            Status.EffectType.REDUCE -> R.color.light_red_background_color
            Status.EffectType.WEAK -> R.color.light_red_background_color
            Status.EffectType.STRONG -> R.color.light_green_background_color
            Status.EffectType.VULNERABLE -> R.color.light_red_background_color
            Status.EffectType.ARMOR -> R.color.light_green_background_color
            Status.EffectType.COUNTERATTACK -> R.color.light_green_background_color
            Status.EffectType.HARM -> R.color.light_green_background_color
            Status.EffectType.INFO -> R.color.light_blue_background_color
            Status.EffectType.DAMAGE -> R.color.light_red_background_color
            Status.EffectType.HEAL -> R.color.light_green_background_color
            Status.EffectType.GENERATE -> R.color.light_blue_background_color
        }
    }
}