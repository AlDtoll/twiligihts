package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Effect
import aldtoll.twiligihts.model.Status
import aldtoll.twiligihts.model.characters.Hero
import aldtoll.twiligihts.model.characters.Person
import aldtoll.twiligihts.model.findActiveStatuses
import aldtoll.twiligihts.storage.BattleLogListInteractor
import aldtoll.twiligihts.storage.EffectValueForDescriptionInteractor
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import aldtoll.twiligihts.storage.hero.HeroInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplyAttackExecutor @Inject constructor(
    private val battleLogListInteractor: BattleLogListInteractor,
    private val editStockExecutor: EditStockExecutor,
    private val enemyInteractor: EnemyInteractor,
    private val heroInteractor: HeroInteractor,
    private val effectValueForDescriptionInteractor: EffectValueForDescriptionInteractor
) {

    private lateinit var person: Person
    fun execute(
        personForAttack: Person?,
        attack: Effect.Attack,
        fromStatusOrSelfAttack: Boolean = false
    ) {
        personForAttack?.run {
            val who = if (personForAttack !is Hero) {
                "Герой"
            } else {
                "Противник"
            }
            if (!fromStatusOrSelfAttack) {
                battleLogListInteractor.add(
                    "$who ${
                        attack.getDescription(
                            effectValueForDescriptionInteractor.item
                        )
                    }"
                )
            }
            effectValueForDescriptionInteractor.item = ""
            this@ApplyAttackExecutor.person = personForAttack
            /**
             * повреждения от статусов не попадают в зачет попаданий
             */
            if (!fromStatusOrSelfAttack) {
                personForAttack.touch()
            }
            val damageForSp = countDamageForSp(attack)
            val damageBlockedByShield = damageShields(damageForSp)
            val damageForHp = countDamageForHp(attack, damageBlockedByShield)
            damageHp(damageForHp, fromStatusOrSelfAttack, attack.help)
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
        fromStatus: Boolean = false,
        help: Boolean
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
        editStockExecutor.updatePersonStocksAfterDamage()
        /**
         * повреждения от статусов не дают восполнения здоровья
         */
        if (!fromStatus && !help) {
            restoreHpByVamp(damage)
        }
    }

    /**
     * восстанавливается здоровье из-за эффекта вампризма
     */
    private fun restoreHpByVamp(damage: Int) {
        val sourceOfAttack = if (person is Hero) {
            heroInteractor.value()
        } else {
            enemyInteractor.value()
        }
        val isHeroTarget = sourceOfAttack is Hero
        val statusList = sourceOfAttack?.statuses?.findActiveStatuses(Status.EffectType.VAMP)
        statusList?.forEach {
            val hpByVamp = damage * it.value / 100
            if (hpByVamp > 0) {
                sourceOfAttack.increaseHp(hpByVamp)
            }
            it.decreaseTimes()
            var message = ""
            message += if (isHeroTarget) {
                "Герой "
            } else {
                "Противник "
            }
            message += " высасывает $hpByVamp здоровья (${it.name}). "
            message += if (sourceOfAttack.hp + hpByVamp > sourceOfAttack.maxHp) {
                "Здоровье полностью восстановлено"
            } else {
                "(${sourceOfAttack.hp}/${sourceOfAttack.maxHp})"
            }
            battleLogListInteractor.add(message)
        }
    }
}