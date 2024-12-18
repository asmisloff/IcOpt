package ru.vniizht.asuterkortes.counter.latticemodel.ic.memoization

data class TrainPosition(
    val coord: Double,
    val activeAmp: Double,
    val lineIndex: Int,
    val massRate: Double,
    val routeIndex: Int,
    val fullAmp: Double?,
    val fullVoltage: Double
) {

//    fun dcCsa() = -activeAmp * massRate
//
//    fun acCsa(): Complex {
//        checkNotNull(fullAmp) {
//            "В результатах одного или нескольких тяговых расчетов не найдены реактивные составляющие токов."
//        }
//        check(fullAmp >= activeAmp) {
//            "В результатах тягового расчета содержится один или несколько элементов, у которых активный ток больше полного. " +
//                    "Проверьте характеристики локомотива. " +
//                    "Точка: $this"
//        }
//        val reactiveAmp = sqrt(fullAmp.square() - activeAmp.square())
//        return Complex(-activeAmp * massRate, reactiveAmp * massRate)
//    }

    /** @return true, если поезд на остановке. */
    fun isHalted() = activeAmp == 0.0 && (fullAmp == null || fullAmp == 0.0)

    override fun toString(): String {
        return "{coord=$coord, activeAmp=$activeAmp, trackNumber=$lineIndex, massRate=$massRate, routeIndex=$routeIndex, fullAmp=$fullAmp}"
    }
}