package aldtoll.twiligihts.model

interface Person {

    var hp: Int
    val maxHp: Int
    var shield: Int
    var wounds: Int
    var statuses: ArrayList<Status>
    val debuffes: ArrayList<Debuff>

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
}