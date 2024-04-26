package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Effect
import aldtoll.twiligihts.model.characters.Hero
import aldtoll.twiligihts.model.characters.Person
import aldtoll.twiligihts.storage.BattleLogListInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplyAttackExecutor @Inject constructor(
    private val battleLogListInteractor: BattleLogListInteractor,
    private val updateStockExecutor: UpdateStockExecutor
) {

    private lateinit var person: Person
    fun execute(
        personForAttack: Person?,
        attack: Effect.Attack,
        fromStatus: Boolean = false
    ) {
        personForAttack?.run {
            val who = if (personForAttack !is Hero) {
                "Герой"
            } else {
                "Противник"
            }
            battleLogListInteractor.add("$who ${attack.getDescription()}")
            this@ApplyAttackExecutor.person = personForAttack
            /**
             * повреждения от статусов не попадают в зачет попаданий
             */
            if (!fromStatus) {
                personForAttack.touch()
            }
            val damageForSp = countDamageForSp(attack)
            val damageBlockedByShield = damageShields(damageForSp)
            val damageForHp = countDamageForHp(attack, damageBlockedByShield)
            damageHp(damageForHp)
        }
    }

    private fun countDamageForSp(attack: Effect.Attack): Int {
        return when (attack.type) {
            Effect.Attack.Type.BOTH -> attack.value
            Effect.Attack.Type.HP -> 0
            Effect.Attack.Type.SP -> attack.value
        }
    }


    private fun countDamageForHp(
        attack: Effect.Attack,
        damageBlockedByShield: Int
    ): Int {
        return when (attack.type) {
            Effect.Attack.Type.BOTH -> attack.value - damageBlockedByShield
            Effect.Attack.Type.HP -> attack.value
            Effect.Attack.Type.SP -> 0
        }
    }

    private fun damageShields(
        damage: Int
    ): Int {
        var message = ""
        val damageForSp: Int = if (damage >= person.shield) {
            person.shield
        } else {
            damage
        }
        val shieldBeforeDamage = person.shield
        if (shieldBeforeDamage > 0) {
            if (damageForSp >= person.shield && damageForSp > 0) {
                person.shield = 0
            } else {
                person.shield = person.shield - damageForSp
            }
        }
        if (shieldBeforeDamage > 0) {
            val whose = if (person is Hero) {
                "героя"
            } else {
                "противника"
            }
            message += "Щиты $whose блокируют $damageForSp урона."
            message += if (damageForSp >= shieldBeforeDamage) {
                " Щиты $whose уничтожены."
            } else {
                "(${person.shield})"
            }
        }
        if (message.isNotEmpty()) {
            battleLogListInteractor.add(message)
        }
        return damageForSp
    }

    private fun damageHp(
        damage: Int,
        fromStatus: Boolean = false
    ) {
        val isHeroTarget = person is Hero
        var message = ""
        message += if (isHeroTarget) {
            "Герой "
        } else {
            "Противник "
        }
        message += "получает $damage урона. "
        person.decreaseHp(damage)
        if (person.hp != 0 && damage > 0) {
            /**
             * повреждения от статусов не попадают в зачет попаданий
             */
            if (!fromStatus) {
                person.hit()
            }
            message += "(${person.hp}/${person.maxHp})"
        }
        battleLogListInteractor.add(message)
        updateStockExecutor.updateStockAfterDamage()
    }
}