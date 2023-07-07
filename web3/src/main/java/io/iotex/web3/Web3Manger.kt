package io.iotex.web3

import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService
import java.math.BigInteger

interface Web3Manger {

    fun getCurrencyBalance(vararg address: String): List<BigInteger>

    fun getErc20Balance(vararg contract: String): List<BigInteger>

    fun transferCurrency(
        to: String,
        value: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger,
        privateKey: String
    ): EthSendTransaction?

    fun transferErc20(
        contract: String,
        to: String,
        value: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger,
        privateKey: String
    ): EthSendTransaction?

    fun transferErc721(
        contract: String,
        to: String,
        tokenId: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger,
        privateKey: String
    ): EthSendTransaction?

    fun transferErc1155(
        contract: String,
        to: String,
        tokenId: BigInteger,
        value: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger,
        privateKey: String
    ): EthSendTransaction?

    fun executeTransaction(
        contract: String,
        value: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger,
        `data`: String,
        privateKey: String
    ): EthSendTransaction?

    fun queryTransactionReceipt(transactionHash: String): TransactionReceipt?

    fun gasPrice(): BigInteger

    fun estimate(to: String, `data`: String): BigInteger

    fun signMessage(privateKey: String, message: ByteArray, addPrefix: Boolean = false): String

    fun isContract(contract: String): Boolean

    fun transactionNonce(): BigInteger?

    fun resolveEns(contractId: String): String

    fun erc20Name(contract: String): String

    fun erc20Symbol(contract: String): String

    fun erc20Decimals(contract: String): Int

    fun nftOwnerOf(contract: String, tokenId: BigInteger): String?

    fun nftBalanceOf(contract: String, tokenId: BigInteger): Int

    companion object {

        fun build(rpc: String, chainId: Long, privateKey: String): Web3Manger {
            val web3j = Web3j.build(HttpService(rpc))
            return RawWeb3Impl(web3j, chainId, privateKey)
        }

        fun build(rpc: String, from: String): Web3Manger {
            val web3j = Web3j.build(HttpService(rpc))
            return ReadOnlyWeb3Impl(web3j, from)
        }

    }
}