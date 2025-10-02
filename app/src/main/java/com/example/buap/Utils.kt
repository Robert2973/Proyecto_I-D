package com.example.buap

import java.security.MessageDigest

fun hashPassword(password: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    val bytes = md.digest(password.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}
