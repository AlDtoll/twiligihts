package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.model.MatchOrientation
import aldtoll.twiligihts.model.MatchRule
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Status
import aldtoll.twiligihts.model.effects.Effect

/**
 * Жестко заданные правила совпадений (аналогично sectors()).
 * Здесь можно описывать эффекты перков, которые будут вызываться из MatchPerkExecutor.
 */
fun matchRules() =
    listOf(
        // Пример: вертикальное совпадение любого цвета от 3
        MatchRule(
            name = "vertical_any_3+",
            orientation = MatchOrientation.VERTICAL,
            minSize = 3,
            perk = Perk(
                name = "Вертикальный удар",
                effects = arrayListOf(
                    Effect.EditStatus(
                        status = Status(
                            name = "Вертикальный бафф",
                            type = Status.StatusType.INFO,
                            value = 1
                        ),
                        target = Effect.EffectTarget.ENEMY,
                        value = 1
                    )
                )
            )
        ),
        // Пример: горизонтальное совпадение цвета 2 больше 4
        MatchRule(
            name = "horizontal_color2_5+",
            orientation = MatchOrientation.HORIZONTAL,
            gemType = 2,
            minSize = 5,
            perk = Perk(
                name = "Сильная защита",
                effects = arrayListOf(
                    Effect.EditStatus(
                        status = Status(
                            name = "Щит усилен",
                            type = Status.StatusType.DEFEND,
                            value = 2
                        ),
                        target = Effect.EffectTarget.SELF,
                        value = 2
                    )
                )
            )
        )
    )


