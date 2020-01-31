package pl.tomaszstankowski.fourplayerchess.common.data

class TestTransactionExecutor : TransactionExecutor {

    override fun <T> executeTransaction(block: () -> T): T = block()

    override fun executeTransactionWithoutResult(block: () -> Unit) = block()
}