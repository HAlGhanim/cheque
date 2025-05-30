package com.cornerstone.cheque.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "transfer")
data class Transfer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "from_user_id", nullable = false)
    val fromUser: User,

    @ManyToOne
    @JoinColumn(name = "to_user_id", nullable = false)
    val toUser: User,

    @Column(nullable = false)
    val amount: BigDecimal,

    @ManyToOne
    @JoinColumn(name = "transaction_id")
    val transaction: Transaction,

    @Column(nullable = false)
    val description: String,

    @JoinColumn(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

)
