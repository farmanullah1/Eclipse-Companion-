package com.example.core

fun interface Clock {
    fun currentTimeMillis(): Long

    companion object {
        val SYSTEM: Clock = Clock { System.currentTimeMillis() }
    }
}
