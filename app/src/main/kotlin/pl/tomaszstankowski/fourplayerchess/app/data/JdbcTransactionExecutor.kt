package pl.tomaszstankowski.fourplayerchess.app.data

import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import pl.tomaszstankowski.fourplayerchess.common.data.TransactionExecutor

@Component
class JdbcTransactionExecutor(private val transactionTemplate: TransactionTemplate) : TransactionExecutor {

    override fun <T> executeTransaction(block: () -> T): T =
        transactionTemplate.execute { block() }!!

    override fun executeTransactionWithoutResult(block: () -> Unit) =
        transactionTemplate.executeWithoutResult { block() }
}