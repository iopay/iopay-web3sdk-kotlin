package io.iotex.web3

import io.iotex.web3.contract.Erc20
import io.iotex.web3.contract.NFT
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicBytes
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.*
import org.web3j.ens.EnsResolver
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthGetBalance
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.tx.RawTransactionManager
import org.web3j.tx.TransactionManager
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.tx.response.PollingTransactionReceiptProcessor
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.math.BigInteger

class RawWeb3Impl(
    private val web3j: Web3j,
    private val chainId: Long,
    private val privateKey: String?
) : Web3Manger {

    private val processor by lazy {
        PollingTransactionReceiptProcessor(
            web3j,
            TransactionManager.DEFAULT_POLLING_FREQUENCY,
            TransactionManager.DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH
        )
    }

    private val transactionManager by lazy {
        val isValid = WalletUtils.isValidPrivateKey(privateKey)
        if (!isValid) throw java.lang.IllegalArgumentException("Private key is invalid")
        val ecKeyPair = ECKeyPair.create(BigInteger(Numeric.cleanHexPrefix(privateKey), 16))
        val credentials = Credentials.create(ecKeyPair)
        RawTransactionManager(web3j, credentials, chainId)
    }

    override fun getCurrencyBalance(vararg address: String): List<BigInteger> {
        val batch = web3j.newBatch()
        address.forEach {
            val request = web3j.ethGetBalance(it, DefaultBlockParameterName.LATEST)
            batch.add(request)
        }
        val batchResponse = runCatching { batch.send() }.getOrNull() ?: return emptyList()
        return batchResponse.responses?.map { response ->
            (response as EthGetBalance).balance
        } ?: emptyList()
    }

    override fun getErc20Balance(vararg contract: String): List<BigInteger> {
        val function = Function(
            "balanceOf",
            listOf<Type<*>>(Address(160, transactionManager.fromAddress)),
            listOf<TypeReference<*>>(object : TypeReference<Uint256>() {})
        )
        val data = FunctionEncoder.encode(function)
        val batch = web3j.newBatch()
        contract.forEach {
            val transaction =
                Transaction.createEthCallTransaction(transactionManager.fromAddress, it, data)
            val request = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST)
            batch.add(request)
        }
        val batchResponse = runCatching { batch.send() }.getOrNull() ?: return emptyList()
        return batchResponse.responses?.map { response ->
            val resultList =
                FunctionReturnDecoder.decode(response.result?.toString(), function.outputParameters)
            if (resultList.isNotEmpty()) {
                resultList[0].value as BigInteger
            } else BigInteger.ZERO
        } ?: return emptyList()
    }

    override fun transferCurrency(
        to: String,
        value: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger
    ): EthSendTransaction? {
        val nonce = transactionNonce()
        val transaction =
            RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, to, value)
        return runCatching {
            transactionManager.signAndSend(transaction)
        }.getOrNull()
    }

    override fun transferErc20(
        contract: String,
        to: String,
        value: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger
    ): EthSendTransaction? {
        val function = Function(
            FUNC_TRANSFER,
            listOf<Type<*>>(
                Address(160, to),
                Uint256(value)
            ), emptyList()
        )
        val `data` = FunctionEncoder.encode(function)
        return runCatching {
            transactionManager
                .sendTransaction(gasPrice, gasLimit, contract, `data`, BigInteger.ZERO)
        }.getOrNull()
    }

    override fun transferErc721(
        contract: String,
        to: String,
        tokenId: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger,
    ): EthSendTransaction? {
        val function = Function(
            FUNC_TRANSFER_NFT,
            listOf<Type<*>>(
                Address(160, transactionManager.fromAddress),
                Address(160, to),
                Uint256(tokenId)
            ), emptyList()
        )
        val `data` = FunctionEncoder.encode(function)
        return runCatching {
            transactionManager.sendTransaction(gasPrice, gasLimit, contract, `data`, BigInteger.ZERO)
        }.getOrNull()
    }

    override fun transferErc1155(
        contract: String,
        to: String,
        tokenId: BigInteger,
        value: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger,
    ): EthSendTransaction? {
        val function = Function(
            FUNC_TRANSFER_NFT,
            listOf<Type<*>>(
                Address(160, transactionManager.fromAddress),
                Address(160, to),
                Uint256(tokenId),
                Uint256(value),
                DynamicBytes("0x".toByteArray())
            ), emptyList()
        )
        val `data` = FunctionEncoder.encode(function)
        return runCatching {
            transactionManager.sendTransaction(gasPrice, gasLimit, contract, `data`, BigInteger.ZERO)
        }.getOrNull()
    }

    override fun executeTransaction(
        contract: String,
        value: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger,
        `data`: String
    ): EthSendTransaction? {
        return runCatching {
            transactionManager.sendTransaction(gasPrice, gasLimit, contract, `data`, value)
        }.getOrNull()
    }

    override fun gasPrice(): BigInteger {
        return runCatching {
            web3j.ethGasPrice().send()?.gasPrice
        }.getOrNull() ?: DefaultGasProvider.GAS_PRICE
    }

    override fun estimate(to: String, `data`: String): BigInteger {
        val transaction =
            Transaction.createEthCallTransaction(transactionManager.fromAddress, to, data)
        return runCatching {
            val result = web3j.ethEstimateGas(transaction).send()?.result
            BigInteger(Numeric.cleanHexPrefix(result), 16)
        }.getOrNull() ?: DefaultGasProvider.GAS_LIMIT
    }

    override fun isContract(contract: String): Boolean {
        return runCatching {
            val code = web3j.ethGetCode(contract, DefaultBlockParameterName.LATEST).send().code
            !code.isNullOrBlank() && code != "0x"
        }.getOrNull() ?: false
    }

    override fun signMessage(message: ByteArray, addPrefix: Boolean): String {
        var `data` = message
        if (addPrefix) {
            val messagePrefix = "\u0019Ethereum Signed Message:\n"
            val prefix = (messagePrefix + message.size).toByteArray()
            val result = ByteArray(prefix.size + message.size)
            System.arraycopy(prefix, 0, result, 0, prefix.size)
            System.arraycopy(message, 0, result, prefix.size, message.size)
            `data` = Hash.sha3(result)
        }

        val credentials = Credentials.create(privateKey)
        val signatureData = Sign.signMessage(`data`, credentials.ecKeyPair, true)
        val rBytes = signatureData.r
        val sBytes = signatureData.s
        val vBytes = signatureData.v
        val signatureBytes = ByteArray(rBytes.size + sBytes.size + vBytes.size)
        System.arraycopy(rBytes, 0, signatureBytes, 0, rBytes.size)
        System.arraycopy(sBytes, 0, signatureBytes, rBytes.size, sBytes.size)
        System.arraycopy(vBytes, 0, signatureBytes, rBytes.size + sBytes.size, vBytes.size)
        return Numeric.toHexString(signatureBytes)
    }

    override fun queryTransactionReceipt(transactionHash: String): TransactionReceipt? {
        return runCatching {
            processor.waitForTransactionReceipt(transactionHash)
        }.getOrNull()
    }

    override fun transactionNonce(): BigInteger? {
        return runCatching {
            web3j.ethGetTransactionCount(
                transactionManager.fromAddress, DefaultBlockParameterName.PENDING
            ).send().transactionCount
        }.getOrNull()
    }

    override fun resolveEns(contractId: String): String {
        return EnsResolver(web3j).resolve(contractId)
    }

    override fun erc20Name(contract: String): String {
        return runCatching {
            Erc20.load(contract, web3j, transactionManager).name().send()
        }.getOrNull() ?: ""
    }

    override fun erc20Symbol(contract: String): String {
        return runCatching {
            Erc20.load(contract, web3j, transactionManager).symbol().send()
        }.getOrNull() ?: ""
    }

    override fun erc20Decimals(contract: String): Int {
        return runCatching {
            Erc20.load(contract, web3j, transactionManager).decimals().send()?.toInt()
        }.getOrNull() ?: Convert.Unit.ETHER.weiFactor.toInt()
    }

    override fun nftOwnerOf(contract: String, tokenId: BigInteger): String? {
        return kotlin.runCatching {
            NFT.load(contract, web3j, transactionManager).ownerOf(tokenId).send()
        }.getOrNull()
    }

    override fun nftBalanceOf(contract: String, tokenId: BigInteger): Int {
        return kotlin.runCatching {
            NFT.load(contract, web3j, transactionManager).balanceOf(
                transactionManager.fromAddress,
                tokenId
            ).send().toInt()
        }.getOrNull() ?: 0
    }

    companion object {
        const val FUNC_TRANSFER = "transfer"
        const val FUNC_TRANSFER_NFT = "safeTransferFrom"
    }
}