# CustomThreadPool

## Описание проекта
Для высоконагруженного серверного приложения требуется **гибкий и эффективный пул потоков**, который позволяет управлять распределением задач, очередями и политикой отказа. В этом проекте мы **отказываемся от стандартного ThreadPoolExecutor** в пользу **собственной реализации**, чтобы получить полный контроль над поведением системы и кастомное логирование.

## Основные компоненты
- **ThreadPool** – главный класс, управляющий пулом потоков, распределяющий задачи и обрабатывающий жизненный цикл.
- **Worker** – рабочий поток, который берет задачи из своей очереди и выполняет их.
- **TaskQueue** – очередь задач, привязанная к каждому рабочему потоку (`BlockingQueue`).
- **ThreadFactory** – фабрика потоков для создания потоков с уникальными именами и логированием.
- **RejectedExecutionHandler** – обработчик отклоненных задач при переполнении очередей.

## Параметры настройки
- **corePoolSize** – минимальное количество активных потоков.
- **maxPoolSize** – максимальное количество потоков.
- **keepAliveTime** и **timeUnit** – время простоя, после которого потоки завершаются.
- **queueSize** – максимальный размер очереди задач для каждого потока.
- **minSpareThreads** – минимальное количество резервных потоков.

## Обработка отказов
**Подход**: Если все очереди переполнены и нет свободных потоков, новая задача **отклоняется** через `RejectedExecutionHandler`.

- **Почему так?** Позволяет избежать блокировки вызывающего потока и перегрузки системы. Логирование отклоненных задач помогает отслеживать проблемы.
- **Недостатки**: Возможна потеря задач при высокой нагрузке.
- **Альтернативы**: Расширение пула потоков или выполнение задачи в текущем потоке могут привести к перегрузке.

## Распределение задач
Используем **алгоритм Round Robin** для равномерного распределения задач между потоками. Это помогает:
- Балансировать нагрузку между потоками.
- Предотвращать перегрузку отдельных потоков.

## Логирование
Логируются ключевые события:
- Создание и завершение потоков.
- Постановка задач в очередь.
- Выполнение задач.
- Отклонение задач.
- Таймауты простоя.

## Структура проекта
CustomThreadPool

├── src

│   ├── main

│   │   ├── java

│   │   │   ├── CustomExecutor.java          // Интерфейс для пула потоков

│   │   │   ├── ThreadPool.java              // Реализация пула потоков

│   │   │   ├── Worker.java                  // Класс рабочего потока

│   │   │   ├── CustomThreadFactory.java     // Фабрика для создания потоков

│   │   │   ├── CustomRejectedHandler.java   // Обработчик отклоненных задач

│   │   │   └── Main.java                    // Точка входа для демонстрации

│   │   └── resources

│   │       └── log4j2.xml

│   └── test

│       └── java

├── build.gradle                             // Файл сборки Gradle

└── README.md                                // Документация проекта



## Описание файлов
- **CustomExecutor.java** – Интерфейс пула потоков.
- **ThreadPool.java** – Основной класс, управляющий потоками.
- **Worker.java** – Рабочий поток для обработки задач.
- **CustomThreadFactory.java** – Создание именованных потоков.
- **CustomRejectedHandler.java** – Обработка отказов.
- **Main.java** – Демонстрация работы пула.

## Что происходит в Main.java?
Создается пул потоков с параметрами:
corePoolSize=2
maxPoolSize=4
queueSize=5
keepAliveTime=5 сек
minSpareThreads=1
Отправляются 15 задач, демонстрируя механизм отказа.
Через 10 секунд вызывается shutdown(), и потоки завершаются.

## Итоги
✅ Кастомный пул потоков с настраиваемыми параметрами 
✅ Round Robin для распределения задач 
✅ Логирование отказов через RejectedExecutionHandler 
✅ Подробное логирование событий 
✅ Демонстрация работы пула

## Анализ производительности
Проводилось сравнение с ThreadPoolExecutor, Tomcat и Jetty. Основные метрики:
Время выполнения
Пропускная способность
Использование CPU и памяти

Выводы:
Производительность сопоставима с ThreadPoolExecutor, если правильно настроить параметры.
В серверных задачах (Tomcat, Jetty) стандартные реализации более оптимизированы для HTTP.
Кастомный пул превосходит их в гибкости и адаптивности к нестандартным задачам.

Оптимизация параметров пула
CPU-bound задачи: corePoolSize = кол-во ядер, умеренный queueSize, небольшой keepAliveTime.
I/O-bound задачи: увеличенный queueSize, гибкая настройка maxPoolSize.

Механизм распределения задач
Используется Round Robin, чтобы равномерно распределять задачи по потокам, предотвращая перегрузку отдельных очередей.



⚙️ Мини-исследование параметров пула потоков
Для определения оптимальных значений параметров пула (corePoolSize, maxPoolSize, queueSize, keepAliveTime, minSpareThreads) были проведены эксперименты с различными типами задач и уровнями нагрузки. Ниже представлены основные выводы:

🔢 Параметры и их влияние
corePoolSize

При CPU-bound задачах увеличение числа базовых потоков повышает пропускную способность до определённого предела.

Оптимальное значение — близко к количеству ядер процессора (обычно 4–8).

Слишком большое значение может перегрузить систему и снизить производительность.

maxPoolSize

Полезно при кратковременных пиках нагрузки.

Завышенное значение вызывает конкуренцию за ресурсы и снижение эффективности.

queueSize

При I/O-bound задачах увеличение размера очереди помогает сглаживать нагрузку.

Однако чрезмерная очередь увеличивает время ожидания, что критично для высокоотзывчивых систем.

keepAliveTime

Уменьшение времени жизни неактивных потоков снижает потребление памяти.

Но может привести к дополнительным затратам при частом создании новых потоков.

minSpareThreads

Полезен для быстрого реагирования на всплески задач.

Избыточное значение ведёт к неэффективному использованию ресурсов.

📌 Вывод
Оптимальные настройки зависят от характера задач и конфигурации оборудования:

CPU-bound:
Рекомендуется:

corePoolSize ≈ количество ядер

Умеренный queueSize

Небольшой keepAliveTime

I/O-bound:
Рекомендуется:

Увеличенный queueSize

Выше maxPoolSize для обработки пиков

🔄 Принцип действия механизма распределения задач
Кастомный пул использует алгоритм Round Robin для распределения задач между внутренними очередями. Каждая очередь привязана к своему рабочему потоку.

🧠 Как это работает:
При поступлении новой задачи она направляется в следующую очередь по кругу.

Это обеспечивает равномерное распределение нагрузки, предотвращая переполнение отдельных очередей.

Такой подход помогает:

Сбалансировать загрузку между потоками

Предотвратить ситуацию, когда один поток перегружен, а другие простаивают

Повысить общую отзывчивость и стабильность системы

🏁 Заключение
Кастомный пул потоков, реализующий стратегию распределения задач и гибкую настройку параметров, может быть эффективной альтернативой стандартным реализациям в специфических сценариях, где важна адаптация под конкретные типы нагрузки.

