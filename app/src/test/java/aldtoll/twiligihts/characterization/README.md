# Characterization tests

Эти тесты фиксируют поведение игровой логики (`logic/`, `model/`) перед переводом UI с XML на Compose. Сначала они фиксировали СТАРОЕ поведение as-is (127 тестов). После решений Данила (`DECISIONS.md`, решения 1–30) баги исправлены, и тесты исправленных пунктов переписаны на НОВОЕ поведение (в комментарии `решение N` / `SUSPECT-N`); всего 154 теста. Пункты, оставленные как есть («ОСТАВИТЬ» / «НЕ БАГ» / «ПРОПУЩЕНО»), по-прежнему фиксируют старое поведение и помечены `// SUSPECT-N`. Список и описание — в `SUSPECTS.md` пилота modernize.

- `RULE-NNN` в комментариях ссылается на карточку правила в `BUSINESS_RULES.md`.
- `Fixture.kt` собирает граф объектов вручную (вместо Hilt), как в `@Inject`-конструкторах.
- Вероятности в тестах 100% (или −1 / 0 для гарантированного провала), поэтому `Random` в коде не влияет на результат. Бросок вероятности теперь `nextInt(0, 100)` со строгим `<` (решение 15): 0 = никогда, 100 = всегда; граничные случаи проверяются повторами.

## Запуск

### Gradle — настроен, но НЕ проверен запуском

`./gradlew :app:testDebugUnitTest --tests 'aldtoll.twiligihts.characterization.*'`

Что для этого добавлено в `app/build.gradle`:
- `testOptions { unitTests.returnDefaultValues = true }` — чтобы `android.util.Log.d` и прочие заглушки android.jar не бросали исключения;
- `androidx.arch.core:core-testing:2.1.0` — `InstantTaskExecutorRule` делает `LiveData.setValue/postValue` синхронными. Версия 2.1.0 совпадает с `androidx.arch.core`, которую тянет lifecycle 2.5.1 (через navigation 2.5.3);
- `org.mockito:mockito-inline:4.11.0` — mock финального `WriteTemporaryLogExecutor`, который в конструкторе обращается к Firebase.

В каждом тест-классе есть `@get:Rule InstantTaskExecutorRule`. `Fixture` создаётся в `@Before`, то есть уже под правилом.

**Честно:** на VPS этот путь не запускался (Gradle там запрещён: вешает сервер). Подтверждён только прогон на стенде kotlinc (ниже). Первый запуск через Gradle на машине разработчика — отдельная проверка.

### Стенд без Gradle (так тесты гонялись на VPS)

Скрипт `run-tests.sh` из пилота modernize компилирует нужные исходники через `kotlinc` 1.7.20 с JVM-заглушками для `androidx.lifecycle`, `androidx.annotation`, `InstantTaskExecutorRule`, `android.util.Log`, Firebase-аннотаций, `R` и `WriteTemporaryLogExecutor` (заглушка бросает исключение, если её не замокали). Mockito на стенде тот же: mockito-inline 4.11.0. Отличие стенда от устройства: `MutableLiveData.postValue` синхронный сам по себе, а не через правило.

## Как добавить кейс

1. Найти правило в `BUSINESS_RULES.md` или добавить новую карточку.
2. Новый тест-класс: добавить `@get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()` и создавать `Fixture` в `@Before`, а не в инициализаторе поля.
3. Собрать состояние через `Fixture` (`f.hero`, `f.heroStocks(...)`, `f.heroHand(...)`).
4. Вызвать исполнитель и проверить конкретные числа. Для нового кейса на нетронутую логику ожидаемое значение берётся из того, что код делает сейчас. Для исправленных решениями пунктов — из `DECISIONS.md`.
