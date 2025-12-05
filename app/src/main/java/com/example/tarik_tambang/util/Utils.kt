package com.example.tarik_tambang.util

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

fun generateRoomCode(length: Int = 5): String {
    val chars = ('A'..'Z') + ('0'..'9')
    return (1..length)
        .map { chars.random() }
        .joinToString("")
}

fun generateNewQuestion(roomRef: DatabaseReference) {
    val num1 = (1..20).random()
    val num2 = (1..10).random()
    val question = "$num1 + $num2 = ?"
    val answer = num1 + num2

    val updates = mapOf(
        "currentQuestion" to question,
        "currentAnswer" to answer
    )
    roomRef.updateChildren(updates)
}

fun simpleIntListener(onChange: (Int?) -> Unit): ValueEventListener {
    return object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            onChange(snapshot.getValue(Int::class.java))
        }
        override fun onCancelled(error: DatabaseError) {}
    }
}

fun simpleStringListener(onChange: (String?) -> Unit): ValueEventListener {
    return object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            onChange(snapshot.getValue(String::class.java))
        }
        override fun onCancelled(error: DatabaseError) {}
    }
}