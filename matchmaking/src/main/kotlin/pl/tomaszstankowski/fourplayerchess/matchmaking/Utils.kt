package pl.tomaszstankowski.fourplayerchess.matchmaking

import org.springframework.transaction.support.TransactionOperations

fun <T> TransactionOperations.executeWithResult(callback: () -> T): T =
        this.execute { callback() }!!