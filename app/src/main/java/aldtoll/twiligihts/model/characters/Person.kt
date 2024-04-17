package aldtoll.twiligihts.model.characters

import aldtoll.twiligihts.logic.UpdateStockExecutor
import aldtoll.twiligihts.model.Condition
import aldtoll.twiligihts.model.Effect
import aldtoll.twiligihts.model.Status
import aldtoll.twiligihts.storage.BattleLogListInteractor
import aldtoll.twiligihts.storage.TurnNumberInteractor

interface Person {

    val name: String?
    var hp: Int
    val maxHp: Int
    var shield: Int
    var wounds: Int?
    var statuses: ArrayList<Status>
    var touches: Int
    var hits: Int
    var blocks: Int

    fun recreate(): Person

    fun decreaseHp(damage: Int) {
        if (damage > this.hp) {
            this.hp = 0
        } else {
            this.hp = this.hp - damage
        }
    }

    fun increaseHp(value: Int) {
        if (this.hp + value > this.maxHp) {
            this.hp = this.maxHp
        } else {
            this.hp = this.hp + value
        }
    }

    fun setHpValue(value: Int) {
        if (value > this.maxHp) {
            this.hp = this.maxHp
        } else {
            this.hp = value
        }
    }

    fun hit() {
        this.hits = this.hits + 1
    }

    fun touch() {
        this.touches = this.touches + 1
    }

    fun applyAttack(
        attack: Effect.Attack,
        battleLogListInteractor: BattleLogListInteractor,
        updateStockExecutor: UpdateStockExecutor,
        fromStatus: Boolean = false
    ) {
        /**
         * повреждения от статусов не попадают в зачет попаданий
         */
        if (!fromStatus) {
            this.touch()
        }
        val damageForSp = countDamageForSp(attack)
        val damageBlockedByShield = damageShields(damageForSp, battleLogListInteractor)
        val damageForHp = countDamageForHp(attack, damageBlockedByShield)
        damageHp(damageForHp, battleLogListInteractor, updateStockExecutor)
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
        damage: Int,
        battleLogListInteractor: BattleLogListInteractor
    ): Int {
        var message = ""
        val damageForSp: Int = if (damage >= this.shield) {
            this.shield
        } else {
            damage
        }
        val shieldBeforeDamage = this.shield
        if (shieldBeforeDamage > 0) {
            if (damageForSp >= this.shield && damageForSp > 0) {
                this.shield = 0
            } else {
                this.shield = this.shield - damageForSp
            }
        }
        if (shieldBeforeDamage > 0) {
            message += "Щиты блокируют $damageForSp урона."
            message += if (damageForSp >= shieldBeforeDamage) {
                " Щиты уничтожены."
            } else {
                "(${this.shield})"
            }
        }
        if (message.isNotEmpty()) {
            battleLogListInteractor.add(message)
        }
        return damageForSp
    }

    private fun damageHp(
        damage: Int,
        battleLogListInteractor: BattleLogListInteractor,
        updateStockExecutor: UpdateStockExecutor,
        fromStatus: Boolean = false
    ) {
        val isHeroTarget = this is Hero
        var message = ""
        message += if (isHeroTarget) {
            "Герой "
        } else {
            "Противник "
        }
        message += "получает $damage урона. "
        this.decreaseHp(damage)
        if (this.hp != 0 && damage > 0) {
            /**
             * повреждения от статусов не попадают в зачет попаданий
             */
            if (!fromStatus) {
                this.hit()
            }
            message += "(${this.hp}/${this.maxHp})"
        }
        battleLogListInteractor.add(message)
        updateStockExecutor.updateStockAfterDamage()
        if (damage > 0) {
            //inflictWound(damageForHp, isHeroTarget)
        }
    }

    private fun Person.inflictWound(damageForHp: Int, isHeroTarget: Boolean) {
//        val message = if (isHeroTarget) {
//            "Герой получает рану"
//        } else {
//            "Противник получает рану"
//        }
//        if (damageForHp > this.hp) {
//            battleLogListInteractor.add(message)
//            this.wounds = this.wounds + 1
//        } else {
//            val percentOfDamage = 100 * damageForHp / this.hp
//            val r = Random.nextInt(1, 100)
//            if (r < percentOfDamage) {
//                battleLogListInteractor.add(message)
//                this.wounds = this.wounds + 1
//            }
//        }
    }

    fun clearHitsAndTouches() {
        this.hits = 0
        this.touches = 0
    }

    fun checkConditionForPerson(
        condition: Condition,
        turnNumberInteractor: TurnNumberInteractor
    ): Boolean {
        val valueForCompare = when (condition.parameter) {
            Condition.Parameter.HP -> this.hp
            Condition.Parameter.SP -> this.shield
            //todo почему то здесь статус оказывается зануленым
            Condition.Parameter.STATUS -> this.statuses.find { it.name == condition.name }?.value
                ?: 0

            Condition.Parameter.TURN -> turnNumberInteractor.value() ?: 0
            Condition.Parameter.HP_P -> this.hp * 100 / maxHp
            Condition.Parameter.HITS -> this.hits
            Condition.Parameter.TOUCHES -> this.touches
        }
        return when (condition.symbol) {
            Condition.Symbol.MORE -> valueForCompare > condition.value
            Condition.Symbol.LESS -> valueForCompare < condition.value
            Condition.Symbol.EQUALS -> valueForCompare == condition.value
            Condition.Symbol.HAVE -> valueForCompare > 0
            Condition.Symbol.EMPTY -> valueForCompare == 0
        }
    }
}