package pl.tomaszstankowski.fourplayerchess.common.data

interface TransactionExecutor {

    fun <T> executeTransaction(block: () -> T): T

    fun executeTransactionWithoutResult(block: () -> Unit)
}