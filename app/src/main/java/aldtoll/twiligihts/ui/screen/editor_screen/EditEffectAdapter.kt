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
                        showAdditionalFields(selectedEffectName)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Do nothing when nothing is selected
                    }
                }
            val targetNames = Effect.EffectTarget.values().map { it.name }
            val targetAdapter =
                ArrayAdapter(binding.root.context, R.layout.simple_spinner_item, targetNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.targetSpinner.adapter = targetAdapter
            binding.targetSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedTargetName = targetNames[position]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Do nothing when nothing is selected
                    }
                }
            val indexOf = effectNames.indexOf(effect.name.name)
            binding.effectSpinner.setSelection(indexOf)
            binding.save.setOnClickListener {
                val selectedItem = binding.effectSpinner.selectedItem
                val effectName = enumValueOf<Effect.EffectName>(selectedItem as String)
                val effectForReplace = createEffectForReplace(effectName)
                replaceData(effectForReplace, position)
            }
        }

        private fun createEffectForReplace(effectName: Effect.EffectName): Effect {
            return when (effectName) {
                Effect.EffectName.ATTACK -> {
                    Effect.Attack(
                        binding.value.text.toString().toInt(),
                        type = Effect.Attack.Type.BOTH,
                        name = Effect.EffectName.ATTACK,
                        target = enumValueOf(binding.targetSpinner.selectedItem as String)
                    )
                }

                Effect.EffectName.DEFEND -> {
                    Effect.Defend(
                        value = binding.value.text.toString().toInt()
                    )
                }

                Effect.EffectName.EDIT_STATUS -> {
                    Effect.EditStatus()
                }

                Effect.EffectName.CHANGE_STOCK -> {
                    Effect.SetStock(
                        value = binding.value.text.toString().toInt(),
                        gemType = 1
                    )
                }

                Effect.EffectName.SET_STOCK -> {
                    Effect.SetStock(
                        value = binding.value.text.toString().toInt(),
                        gemType = 1
                    )
                }

                Effect.EffectName.HEAL -> {
                    Effect.Heal(
                        value = binding.value.text.toString().toInt()
                    )
                }

                Effect.EffectName.FINISH -> {
                    Effect.FinishBattle()
                }

                Effect.EffectName.INFO -> {
                    Effect.Info()
                }
            }
        }

        private fun showAdditionalFields(selectedEffectName: String) {
            val effectName = enumValueOf<Effect.EffectName>(selectedEffectName)
            when (effectName) {
                Effect.EffectName.ATTACK -> {
                    binding.value.visibility = View.VISIBLE
                }

                Effect.EffectName.DEFEND -> {
                    binding.value.visibility = View.VISIBLE
                }

                Effect.EffectName.EDIT_STATUS -> {
                    binding.value.visibility = View.GONE
                }

                Effect.EffectName.CHANGE_STOCK -> {
                    binding.value.visibility = View.VISIBLE
                }

                Effect.EffectName.SET_STOCK -> {
                    binding.value.visibility = View.VISIBLE
                }

                Effect.EffectName.HEAL -> {
                    binding.value.visibility = View.VISIBLE
                }

                Effect.EffectName.FINISH -> {
                    binding.value.visibility = View.GONE
                }

                Effect.EffectName.INFO -> {
                    binding.value.visibility = View.GONE
                }
            }
        }
    }
}