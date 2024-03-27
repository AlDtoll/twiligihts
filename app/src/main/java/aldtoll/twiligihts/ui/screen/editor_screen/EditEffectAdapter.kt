package aldtoll.twiligihts.ui.screen.editor_screen

import aldtoll.twiligihts.databinding.ItemEditEffectBinding
import aldtoll.twiligihts.model.Effect
import android.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class EditEffectAdapter(
) : RecyclerView.Adapter<EditEffectAdapter.EffectHolder>() {

    companion object {
        fun newInstance() = EditPerkAdapter()
    }

    private val differ = AsyncListDiffer(this, EffectDiffUtilCallback())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EffectHolder {
        return EffectHolder(
            ItemEditEffectBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: EffectHolder, position: Int) {
        val person = differ.currentList[position]
        holder.bind(person, position)
    }

    fun addEmptyData() {
        val data = getData()
        data.add(Effect.Attack())
        updateData(data)
    }

    fun addData(perk: Effect) {
        val data = getData()
        data.add(perk)
        updateData(data)
    }

    fun replaceData(hand: Effect, position: Int) {
        val data = getData()
        data[position] = hand
        updateData(data)
    }

    fun updateData(perks: ArrayList<Effect>) {
        differ.submitList(perks)
    }

    fun getData(): ArrayList<Effect> = ArrayList(differ.currentList)


    class EffectDiffUtilCallback : DiffUtil.ItemCallback<Effect>() {

        override fun areItemsTheSame(oldItem: Effect, newItem: Effect): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: Effect, newItem: Effect): Boolean {
            return false
        }

    }

    inner class EffectHolder(
        private val binding: ItemEditEffectBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(effect: Effect, position: Int) {
            val effectNames = Effect.EffectName.values().map { it.name }
            val adapter =
                ArrayAdapter(binding.root.context, R.layout.simple_spinner_item, effectNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.effectSpinner.adapter = adapter

            binding.effectSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedEffectName = effectNames[position]
                        // Now you can use the selectedEffect as needed
                        // For example, you can pass it to a function to process further
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Do nothing when nothing is selected
                    }
                }
            binding.save.setOnClickListener {
                val selectedItem = binding.effectSpinner.selectedItem
                val effectForReplace = Effect.Attack()
                replaceData(effectForReplace, position)
            }
        }
    }
}