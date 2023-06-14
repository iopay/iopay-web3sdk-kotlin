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

class NFT(
    contract: String,
    web3j: Web3j,
    transactionManager: TransactionManager,
    contractGasProvider: ContractGasProvider
) : Contract(BINARY, contract, web3j, transactionManager, contractGasProvider) {

    fun balanceOf(_address: String, _tokenId: BigInteger): RemoteFunctionCall<BigInteger> {
        val function = Function(
            FUNC_BALANCEOF,
            listOf<Type<*>>(
                Address(160, _address),
                Uint256(_tokenId)
            ),
            listOf<TypeReference<*>>(object : TypeReference<Uint256>() {})
        )
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    fun ownerOf(_tokenId: BigInteger): RemoteFunctionCall<String> {
        val function = Function(
            FUNC_OWNEROF,
            listOf(Uint256(_tokenId)),
            listOf<TypeReference<*>>(object : TypeReference<Address>() {})
        )
        return executeRemoteCallSingleValueReturn(function, String::class.java)
    }

    fun transfer721(
        _from: String,
        _to: String,
        _tokenId: BigInteger
    ): RemoteFunctionCall<TransactionReceipt> {
        val function = Function(
            FUNC_SAFE_TRANSFER_FROM,
            listOf<Type<*>>(
                Address(160, _from),
                Address(160, _to),
                Uint256(_tokenId)
            ), emptyList()
        )
        return executeRemoteCallTransaction(function)
    }

    companion object {
        const val BINARY = "Bin file was not provided"
        const val FUNC_BALANCEOF = "balanceOf"
        const val FUNC_OWNEROF = "ownerOf"
        const val FUNC_SAFE_TRANSFER_FROM = "safeTransferFrom"

        fun load(
            contract: String,
            web3j: Web3j,
            transactionManager: TransactionManager,
            contractGasProvider: ContractGasProvider = DefaultGasProvider()
        ): NFT {
            return NFT(contract, web3j, transactionManager, contractGasProvider)
        }
    }
}