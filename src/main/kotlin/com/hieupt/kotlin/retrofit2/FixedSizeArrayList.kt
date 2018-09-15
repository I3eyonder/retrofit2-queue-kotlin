package com.hieupt.kotlin.retrofit2

import java.util.*

internal class FixedSizeArrayList<E>(maxSize: Int) : ArrayList<E>() {

    var maxSize: Int = 0
        set(maxSize) {
            if (maxSize <= 0) {
                throw IllegalArgumentException("maxSize must be greater than 0")
            }
            field = maxSize
        }

    init {
        this.maxSize = maxSize
    }

    fun canAdd(): Boolean {
        return size < this.maxSize
    }

    override fun add(element: E): Boolean {
        return if (canAdd()) super.add(element) else false
    }

    override fun add(index: Int, element: E) {
        if (canAdd()) {
            super.add(index, element)
        }
    }

    override fun addAll(elements: Collection<E>): Boolean {
        val remain = this.maxSize - size
        if (remain > 0) {
            val subList = elements.take(remain)
            return super.addAll(subList)
        }
        return false
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        val remain = this.maxSize - size
        if (remain > 0) {
            val subList = elements.take(remain)
            return super.addAll(index, subList)
        }
        return false
    }
}
