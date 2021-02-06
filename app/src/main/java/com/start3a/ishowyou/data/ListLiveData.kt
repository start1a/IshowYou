package com.start3a.ishowyou.data

import androidx.lifecycle.MutableLiveData

class ListLiveData<T>(private val list: MutableList<T>): MutableLiveData<MutableList<T>>() {

    init {
        value = mutableListOf()
    }

    fun add(item: T) {
        list.add(item)
        value = list
    }

    fun addAll(items: List<T>) {
        list.addAll(items)
        value = list
    }
}