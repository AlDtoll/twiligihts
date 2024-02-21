package aldtoll.twiligihts.model

interface Person {

    var hp: Int
    val maxHp: Int
    var shield: Int
    var wounds: Int
    var statuses: ArrayList<Status>
    val debuffes: ArrayList<Debuff>

    fun recreate(): Person
}