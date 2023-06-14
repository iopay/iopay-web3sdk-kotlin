package io.iotex.web3.contract

import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.*
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.RemoteFunctionCall
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.tx.Contract
import org.web3j.tx.TransactionManager
import org.web3j.tx.gas.ContractGasProvider
import org.web3j.tx.gas.DefaultGasProvider
import java.math.BigInteger

class Erc20(
    contract: String,
    web3j: Web3j,
    transactionManager: TransactionManager,
    contractGasProvider: ContractGasProvider
) : Contract(BINARY, contract, web3j, transactionManager, contractGasProvider) {

    fun name(): RemoteFunctionCall<String> {
        val function = Function(
            FUNC_NAME,
            emptyList(),
            listOf<TypeReference<*>>(object : TypeReference<Utf8String>() {})
        )
        return executeRemoteCallSingleValueReturn(function, String::class.java)
    }

    fun approve(_spender: String, _value: BigInteger): RemoteFunctionCall<TransactionReceipt> {
        val function = Function(
            FUNC_APPROVE,
            listOf<Type<*>>(
                Address(160, _spender),
                Uint256(_value)
            ), emptyList()
        )
        return executeRemoteCallTransaction(function)
    }

    fun transferFrom(
        _from: String,
        _to: String,
        _value: BigInteger
    ): RemoteFunctionCall<TransactionReceipt> {
        val function = Function(
            FUNC_TRANSFERFROM,
            listOf<Type<*>>(
                Address(160, _from),
                Address(160, _to),
                Uint256(_value)
            ), emptyList()
        )
        return executeRemoteCallTransaction(function)
    }

    fun decimals(): RemoteFunctionCall<BigInteger> {
        val function = Function(
            FUNC_DECIMALS,
            listOf(),
            listOf<TypeReference<*>>(object : TypeReference<Uint256>() {})
        )
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    fun owner(): RemoteFunctionCall<String> {
        val function = Function(
            FUNC_OWNER,
            listOf(),
            listOf<TypeReference<*>>(object : TypeReference<Address>() {})
        )
        return executeRemoteCallSingleValueReturn(function, String::class.java)
    }

    fun symbol(): RemoteFunctionCall<String> {
        val function = Function(
            FUNC_SYMBOL,
            listOf(),
            listOf<TypeReference<*>>(object : TypeReference<Utf8String>() {})
        )
        return executeRemoteCallSingleValueReturn(function, String::class.java)
    }

    fun transfer(_to: String, _value: BigInteger): RemoteFunctionCall<TransactionReceipt> {
        val function = Function(
            FUNC_TRANSFER,
            listOf<Type<*>>(
                Address(160, _to),
                Uint256(_value)
            ), emptyList()
        )
        return executeRemoteCallTransaction(function)
    }

    fun transferOwnership(newOwner: String): RemoteFunctionCall<TransactionReceipt> {
        val function = Function(
            FUNC_TRANSFEROWNERSHIP,
            listOf<Type<*>>(Address(160, newOwner)), emptyList()
        )
        return executeRemoteCallTransaction(function)
    }

    companion object {
        const val BINARY = "Bin file was not provided"
        const val FUNC_NAME = "name"
        const val FUNC_APPROVE = "approve"
        const val FUNC_TRANSFERFROM = "transferFrom"
        const val FUNC_DECIMALS = "decimals"
        const val FUNC_BALANCEOF = "balanceOf"
        const val FUNC_OWNER = "owner"
        const val FUNC_SYMBOL = "symbol"
        const val FUNC_TRANSFER = "transfer"
        const val FUNC_TRANSFEROWNERSHIP = "transferOwnership"

        fun load(
            contract: String,
            web3j: Web3j,
            transactionManager: TransactionManager,
            contractGasProvider: ContractGasProvider = DefaultGasProvider()
        ): Erc20 {
            return Erc20(contract, web3j, transactionManager, contractGasProvider)
        }
    }
}