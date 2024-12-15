package ru.vniizht.asuterkortes.counter.latticemodel

import org.ejml.data.Complex_F64

class DynamicIntArray(data: IntArray) {

    constructor(capacity: Int) : this(IntArray(capacity)) {
        size = 0
    }

    var capacity: Int = data.size; private set
    var data = data; private set
    var size = data.size
        set(value) {
            ensureCapacity(value)
            field = value
        }

    fun ensureCapacity(capacity: Int) {
        if (capacity > this.capacity) {
            val buffer = data
            this.capacity = capacity
            data = IntArray(capacity)
            System.arraycopy(buffer, 0, data, 0, buffer.size)
        }
    }

    fun append(value: Int) {
        if (size == capacity) {
            ensureCapacity((capacity * 3 / 2).coerceAtLeast(16))
        }
        data[size] = value
        size += 1
    }

    fun get(index: Int): Int {
        return data[index]
    }

    fun set(index: Int, value: Int) {
        data[index] = value
    }

    fun copyTo(dest: DynamicIntArray) {
        dest.size = this.size
        System.arraycopy(data, 0, dest.data, 0, size)
    }

}

class DynamicDoubleArray(data: DoubleArray) {

    constructor(capacity: Int) : this(DoubleArray(capacity)) {
        size = 0
    }

    var capacity: Int = data.size; private set
    var data = data; private set
    var size = data.size
        set(value) {
            ensureCapacity(value)
            field = value
        }

    fun ensureCapacity(capacity: Int) {
        if (capacity > this.capacity) {
            val buffer = data
            this.capacity = capacity
            data = DoubleArray(capacity)
            System.arraycopy(buffer, 0, data, 0, size)
        }
    }

    fun append(value: Double) {
        if (size == capacity) {
            ensureCapacity((capacity * 3 / 2).coerceAtLeast(16))
        }
        data[size] = value
        size += 1
    }

    fun get(index: Int): Double {
        return data[index]
    }

    fun set(index: Int, value: Double) {
        data[index] = value
    }

    fun copyTo(dest: DynamicDoubleArray) {
        dest.size = this.size
        System.arraycopy(data, 0, dest.data, 0, size)
    }
}


class DynamicComplexArray(dataRe: DoubleArray, dataIm: DoubleArray) {

    constructor(capacity: Int) : this(DoubleArray(capacity), DoubleArray(capacity)) {
        size = 0
    }

    init {
        require(dataRe.size == dataIm.size)
    }

    var capacity: Int = dataRe.size; private set
    var dataRe = dataRe; private set
    var dataIm = dataIm; private set
    var size = dataRe.size
        set(value) {
            ensureCapacity(value)
            field = value
        }

    fun ensureCapacity(capacity: Int) {
        if (capacity > this.capacity) {
            val oldIm = dataIm
            val oldRe = dataRe
            this.capacity = capacity
            dataIm = DoubleArray(capacity)
            System.arraycopy(oldIm, 0, dataIm, 0, oldIm.size)
            dataRe = DoubleArray(capacity)
            System.arraycopy(oldRe, 0, dataRe, 0, oldRe.size)
        }
    }

    fun append(value: Complex_F64) = append(value.real, value.imaginary)

    fun append(re: Double, im: Double) {
        if (size == capacity) {
            ensureCapacity((capacity * 3 / 2).coerceAtLeast(16))
        }
        dataRe[size] = re
        dataIm[size] = im
        ++size
    }

    fun get(index: Int, result: Complex_F64): Complex_F64 {
        result.imaginary = dataIm[index]
        result.real = dataRe[index]
        return result
    }

    fun getRe(index: Int) = dataRe[index]
    fun getIm(index: Int) = dataIm[index]

    fun set(index: Int, re: Double, im: Double) {
        this.dataRe[index] = re
        this.dataIm[index] = im
    }

    fun copyTo(dest: DynamicComplexArray) {
        dest.size = this.size
        System.arraycopy(dataRe, 0, dest.dataRe, 0, size)
        System.arraycopy(dataIm, 0, dest.dataIm, 0, size)
    }
}