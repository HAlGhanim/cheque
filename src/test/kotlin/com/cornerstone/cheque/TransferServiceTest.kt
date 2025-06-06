package com.cornerstone.cheque

import com.cornerstone.cheque.model.*
import com.cornerstone.cheque.repo.*
import com.cornerstone.cheque.service.TransferRequest
import com.cornerstone.cheque.service.TransferService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.*

class TransferServiceTest {

    private lateinit var transferRepository: TransferRepository
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var userRepository: UserRepository
    private lateinit var accountRepository: AccountRepository
    private lateinit var service: TransferService

    private val user = User(
        id = 1,
        email = "newuser7@gmail.com",
        password = "pass123",
        role = Role.USER
    )

    private lateinit var senderAccount: Account
    private lateinit var receiverUser: User
    private lateinit var receiverAccount: Account

    @BeforeEach
    fun setup() {
        transferRepository = mock(TransferRepository::class.java)
        transactionRepository = mock(TransactionRepository::class.java)
        userRepository = mock(UserRepository::class.java)
        accountRepository = mock(AccountRepository::class.java)
        service = TransferService(transferRepository, transactionRepository, userRepository, accountRepository)

        senderAccount = Account(
            accountNumber = "7738384767373",
            user = user,
            balance = BigDecimal("1000.000"),
            spendingLimit = 500,
            accountType = AccountType.CUSTOMER,
            createdAt = LocalDateTime.now()
        )

        receiverUser = User(id = 2, email = "newuser8@gmail.com", password = "pass123", role = Role.USER)

        receiverAccount = Account(
            accountNumber = "77383847477744",
            user = receiverUser,
            balance = BigDecimal("300.000"),
            spendingLimit = null,
            accountType = AccountType.MERCHANT,
            createdAt = LocalDateTime.now()
        )
    }

    @Test
    fun `should create invoice successfully`() {
        val request = TransferRequest(
            receiverAccount = "77383847477744",
            amount = BigDecimal("150.000"),
            description = "Test invoice"
        )

        val transaction = Transaction(1, senderAccount, receiverAccount, request.amount, LocalDateTime.now())
        val transfer = Transfer(1, user, receiverUser, request.amount, transaction, request.description, LocalDateTime.now())

        `when`(userRepository.findByEmail(user.email)).thenReturn(user)
        `when`(accountRepository.findByAccountNumber("7738384767373")).thenReturn(senderAccount)
        `when`(accountRepository.findByAccountNumber("77383847477744")).thenReturn(receiverAccount)
        `when`(transactionRepository.save(any())).thenReturn(transaction)
        `when`(transferRepository.save(any())).thenReturn(transfer)

        val response = service.create(request, user.email)

        assertEquals("Test invoice", response.description)
        assertEquals(BigDecimal("150.000"), response.amount)
        assertEquals("7738384767373", response.senderAccountNumber)
        assertEquals("77383847477744", response.receiverAccountNumber)
    }

    @Test
    fun `should throw if user not found`() {
        `when`(userRepository.findByEmail("missing@example.com")).thenReturn(null)

        val request = TransferRequest("1111111111111111",  BigDecimal("100.000"), "Invoice")

        val ex = assertFailsWith<IllegalArgumentException> {
            service.create(request, "missing@example.com")
        }

        assertEquals("From user not found", ex.message)
    }

    @Test
    fun `should throw if sender account not found`() {
        `when`(userRepository.findByEmail(user.email)).thenReturn(user)
        `when`(accountRepository.findByAccountNumber("X")).thenReturn(null)

        val request = TransferRequest("Y", BigDecimal("100.000"), "desc")

        val ex = assertFailsWith<IllegalArgumentException> {
            service.create(request, user.email)
        }

        assertEquals("Sender account not found", ex.message)
    }

    @Test
    fun `should throw if account does not belong to user`() {
        val anotherUser = User(id = 99L, email = "notme@example.com", password = "xx", role = Role.USER)
        val otherAccount = senderAccount.copy(user = anotherUser)

        `when`(userRepository.findByEmail(user.email)).thenReturn(user)
        `when`(accountRepository.findByAccountNumber("S")).thenReturn(otherAccount)

        val request = TransferRequest("R", BigDecimal("100.000"), "desc")

        val ex = assertFailsWith<IllegalArgumentException> {
            service.create(request, user.email)
        }

        assertEquals("You do not own the sender account", ex.message)
    }
}