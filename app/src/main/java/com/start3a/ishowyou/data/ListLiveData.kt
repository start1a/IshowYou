package com.start3a.ishowyou.data

import androidx.lifecycle.MutableLiveData

class ListLiveData<T>(private val list: MutableList<T>): MutableLiveData<MutableList<T>>() {

    init {
        value = list
    }

    fun add(item: T) {
        list.add(item)
        value = list
    }

    fun addAll(items: List<T>) {
        list.addAll(items)
        value = list
    }

    fun removeAt(pos: Int) {
        list.removeAt(pos)
        value = list
    }

    fun findIndex(item : T): Int {
        for (i in 0 until list.size) {
            if (item == list[i])
                return i
        }

        return -1
    }
}