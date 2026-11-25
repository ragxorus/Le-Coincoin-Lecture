package com.example.coincoinlecture.model

// Une personne du groupe. uid = null tant que personne ne s'est "assigné" à ce nom.
data class Member(
    val id: String,
    val name: String,
    val uid: String? = null
) {
    fun toMap(): Map<String, Any?> = mapOf("id" to id, "name" to name, "uid" to uid)
}

// Un groupe partagé "à la Tricount".
data class Group(
    val id: String = "",
    val name: String,
    val ownerId: String = "",
    val members: List<Member> = emptyList()
)
