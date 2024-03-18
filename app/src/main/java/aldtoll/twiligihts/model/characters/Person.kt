package aldtoll.twiligihts.model.characters

import aldtoll.twiligihts.model.Status

interface Person {

    var hp: Int
    val maxHp: Int
    var shield: Int
    var wounds: Int
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

    fun hit() {
        this.hits = this.hits + 1
    }

    fun touch() {
        this.touches = this.touches + 1
    }
}