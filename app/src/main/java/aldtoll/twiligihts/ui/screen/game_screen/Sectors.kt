package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.R
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Sector
import aldtoll.twiligihts.model.Status
import aldtoll.twiligihts.model.effects.Effect

fun sectors() =
    listOf(
        Sector(
            1,
            "Верх",
            R.drawable.ic_helmet,
            R.drawable.selected_tile_background,
            Perk(
                name = "Целить в верх",
                effects = arrayListOf(
                    Effect.EditStatus(
                        status = Status(
                            name = "Цель: Верх",
                            type = Status.StatusType.INFO,
                            value = 1
                        ),
                        target = Effect.EffectTarget.ENEMY,
                        value = 1
                    ),
                    Effect.EditStatus(
                        status = Status(
                            name = "Цель: Торс",
                            type = Status.StatusType.INFO,
                            value = 0
                        ),
                        target = Effect.EffectTarget.ENEMY,
                        value = 0
                    ),
                    Effect.EditStatus(
                        status = Status(
                            name = "Цель: Руки",
                            type = Status.StatusType.INFO,
                            value = 0
                        ),
                        target = Effect.EffectTarget.ENEMY,
                        value = 0
                    ),
                    Effect.EditStatus(
                        status = Status(
                            name = "Цель: Ноги",
                            type = Status.StatusType.INFO,
                            value = 0
                        ),
                        target = Effect.EffectTarget.ENEMY,
                        value = 0
                    )
                )
            )
        ),
        Sector(
            2,
            "Руки",
            R.drawable.ic_hands,
            R.drawable.selected_tile_background,
            Perk(
                name = "Целить в руки",
                effects = arrayListOf(
                    Effect.EditStatus(
                        status = Status(
                            name = "Цель: Верх",
                            type = Status.StatusType.INFO,
                            value = 0
                        ),
                        target = Effect.EffectTarget.ENEMY,
                        value = 0
                    ),
                    Effect.EditStatus(
                        status = Status(
                            name = "Цель: Торс",
                            type = Status.StatusType.INFO,
                            value = 0
                        ),
                        target = Effect.EffectTarget.ENEMY,
                        value = 0
                    ),
                    Effect.EditStatus(
                        status = Status(
                            name = "Цель: Руки",
                            type = Status.StatusType.INFO,
                            value = 1
                        ),
                        target = Effect.EffectTarget.ENEMY,
                        value = 1
                    ),
                    Effect.EditStatus(
                        status = Status(
                            name = "Цель: Ноги",
                            type = Status.StatusType.INFO,
                            value = 0
                        ),
                        target = Effect.EffectTarget.ENEMY,
                        value = 0
                    )
                )
            ),
        ),
        Sector(
            3,
            "Торс",
            R.drawable.ic_armor,
            R.drawable.selected_tile_background,
            Perk(
                name = "Целить в торс",
                effects = arrayListOf(
                    Effect.EditStatus(
                        status = Status(
                            name = "Цель: Верх",
                            type = Status.StatusType.INFO,
                            value = 1
                        ),
                        target = Effect.EffectTarget.ENEMY,
                        value = 0
                    ),
                    Effect.EditStatus(
                        status = Status(
                            name = "Цель: Торс",
                            type = Status.StatusType.INFO,
                            value = 1
                        ),
                        target = Effect.EffectTarget.ENEMY,
                        value = 1
                    ),
                    Effect.EditStatus(
                        status = Status(
                            name = "Цель: Руки",
                            type = Status.StatusType.INFO,
                            value = 0
                        ),
                        target = Effect.EffectTarget.ENEMY,
                        value = 0
                    ),
                    Effect.EditStatus(
                        status = Status(
                            name = "Цель: Ноги",
                            type = Status.StatusType.INFO,
                            value = 0
                        ),
                        target = Effect.EffectTarget.ENEMY,
                        value = 0
                    )
                )
            ),
        ),
        Sector(
            4,
            "Ноги",
            R.drawable.ic_legs,
            R.drawable.selected_tile_background,
            Perk(
                name = "Целить в ноги",
                effects = arrayListOf(
                    Effect.EditStatus(
                        status = Status(
                            name = "Цель: Верх",
                            type = Status.StatusType.INFO,
                            value = 0
                        ),
                        target = Effect.EffectTarget.ENEMY,
                        value = 0
                    ),
                    Effect.EditStatus(
                        status = Status(
                            name = "Цель: Торс",
                            type = Status.StatusType.INFO,
                            value = 0
                        ),
                        target = Effect.EffectTarget.ENEMY,
                        value = 0
                    ),
                    Effect.EditStatus(
                        status = Status(
                            name = "Цель: Руки",
                            type = Status.StatusType.INFO,
                            value = 0
                        ),
                        target = Effect.EffectTarget.ENEMY,
                        value = 0
                    ),
                    Effect.EditStatus(
                        status = Status(
                            name = "Цель: Ноги",
                            type = Status.StatusType.INFO,
                            value = 1
                        ),
                        target = Effect.EffectTarget.ENEMY,
                        value = 1
                    )
                )
            ),
        )
    )